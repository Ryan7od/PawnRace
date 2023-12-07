package pawnrace

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

fun minimaxP(
    executor: ExecutorService,
    node: MoveTree,
    depth: Int,
    maxPlayer: Boolean,
): Int {
    if (depth == 0 || node.isTerminal()) {
        return 1
    }
    if (maxPlayer) {
        var value = Int.MIN_VALUE
        node.getChildren().forEach {
            executor.submit {
                value = maxOf(value, minimax(it, depth - 1, false))
            }
        }
        return value
    } else {
        var value = Int.MAX_VALUE
        node.getChildren().forEach {
            executor.submit {
                value = minOf(value, minimax(it, depth - 1, false))
            }
        }
        return value
    }
}

fun minimax(node: MoveTree, depth: Int, maxPlayer: Boolean): Int {
    if (depth == 0 || node.isTerminal()) {
        return 1
    }
    if (maxPlayer) {
        var value = Int.MIN_VALUE
        node.getChildren().forEach {
            value = maxOf(value, minimax(it, depth - 1, false))
        }
        return value
    } else {
        var value = Int.MAX_VALUE
        node.getChildren().forEach {
            value = minOf(value, minimax(it, depth - 1, false))
        }
        return value
    }
}

fun createTreeP(
    executor: ExecutorService,
    player: Piece,
    depth: Int,
    gm: Game,
    m: Move? = null,
): MoveTree {
    val tree = MoveTree(gm, m)

    if (depth == 0) {
        return tree
    }

    // val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    gm.moves(player).forEach {
        executor.submit {
            tree.add(createTree(player.opposite(), depth - 1, gm.applyMove(it), it))
        }
    }

    return tree
}

fun createTreePtr(
    executor: ExecutorService,
    player: Piece,
    depth: Int,
    gm: Game,
    m: Move? = null,
    tree: MoveTree = MoveTree(gm, m),
): MoveTree {
    if (depth == 0) {
        return tree
    }

    // val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    gm.moves(player).forEach {
        executor.submit {
            val newGame = gm.applyMove(it)
            val newTree = MoveTree(newGame, m)
            tree.add(createTreeTR(player.opposite(), depth - 1, newGame, it, newTree))
        }
    }

    return tree
}

fun itDeepM(game: Game, maxDepth: Int, timeLimitMillis: Long, player: Piece): Move? {
    var bestMove: Move? = null
    var depth = 4
    var elapsedTime: Long = 0

    while (depth <= maxDepth && elapsedTime < timeLimitMillis) {
        val timeTaken = measureTimeMillis {
            bestMove = findBestMoveM(game, depth, player)
        }
        elapsedTime += timeTaken

        val remainingTime = timeLimitMillis - elapsedTime
        val estimatedTimeForNextDepth = timeTaken*4
        if (remainingTime < estimatedTimeForNextDepth) {
            break
        }

        depth++
    }

    return bestMove
}

fun findBestMoveM(game: Game, depth: Int, player: Piece): Move? {
    val moves = game.moves(player)
    var bestMove: Move? = null
    var bestValue = Int.MIN_VALUE

    for (move in moves) {
        val newGame = game.applyMove(move)
        val value = abMinimaxP(newGame, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, player.opposite(), false)

        if (value > bestValue) {
            bestValue = value
            bestMove = move
        }
    }
    return bestMove
}

fun abMinimaxP(game: Game, depth: Int, a: Int, b: Int, player: Piece, maxPlayer: Boolean = true): Int {
    if (depth == 0 || game.over()) {
        return evaluate(game, player)
    }

    var alpha = a
    var beta = b

    val moves = game.moves(player)
    var value = if (maxPlayer) Int.MIN_VALUE else Int.MAX_VALUE

    for (move in moves) {
        val newGame = game.applyMove(move)
        val newValue = abMinimaxP(newGame, depth - 1, alpha, beta, player.opposite(), !maxPlayer)

        if (maxPlayer) {
            value = maxOf(value, newValue)
            alpha = maxOf(alpha, value)
        } else {
            value = minOf(value, newValue)
            beta = minOf(beta, value)
        }

        if (beta <= alpha) {
            break // Alpha-Beta Pruning
        }
    }
    return value
}

fun createTree(player: Piece, depth: Int, gm: Game, m: Move? = null): MoveTree {
    val tree = MoveTree(gm, m)

    if (depth == 0) {
        return tree
    }

    gm.moves(player).forEach {
        tree.add(createTree(player.opposite(), depth - 1, gm.applyMove(it), it))
    }

    return tree
}

fun createTreeTR(player: Piece, depth: Int, gm: Game, m: Move? = null, tree: MoveTree = MoveTree(gm, m)): MoveTree {
    if (depth == 0) {
        return tree
    }

    gm.moves(player).forEach {
        val newGame = gm.applyMove(it)
        val newTree = MoveTree(newGame, it)
        tree.add(createTreeTR(player.opposite(), depth - 1, newGame, it, newTree))
    }

    return tree
}

fun main() {
    for (i in 0..15) {
        val startTime = System.currentTimeMillis()
        val tree: MoveTree? = null

//        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
//
//        tree = createTreeP(executor, Piece.W, 7, Game(Board(File(0), File(7)), Piece.W))
//        executor.shutdown()
//        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
//            println("timeout")
//        }
//
//        val executor = Executors.newFixedThreadPool(
//            Runtime.getRuntime().availableProcessors()
//        )
//
//        tree = createTreePtr(
//            executor,
//            Piece.W,
//            7,
//            Game(Board(File(0), File(7)), Piece.W)
//        )
//        executor.shutdown()
//        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
//            println("timeout")
//        }

//    tree = createTree(Piece.W, 7, Game(Board(File(0), File(7)), Piece.W))
//    tree = createTreeTR(Piece.W, 7, Game(Board(File(0), File(7)), Piece.W))

        println(tree?.size())
        println("Time: ${System.currentTimeMillis() - startTime}")
    }
}
