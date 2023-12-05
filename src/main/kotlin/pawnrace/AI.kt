package pawnrace

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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
                value = max(value, minimax(it, depth - 1, false))
            }
        }
        return value
    } else {
        var value = Int.MAX_VALUE
        node.getChildren().forEach {
            executor.submit {
                value = min(value, minimax(it, depth - 1, false))
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
            value = max(value, minimax(it, depth - 1, false))
        }
        return value
    } else {
        var value = Int.MAX_VALUE
        node.getChildren().forEach {
            value = min(value, minimax(it, depth - 1, false))
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

fun main() {
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val timeP = measureTimeMillis {
        createTreeP(executor, Piece.W, 2, Game(Board(File(0), File(7)), Piece.W))
    }
    val time = measureTimeMillis {
        createTree(Piece.W, 5, Game(Board(File(0), File(7)), Piece.W))
    }

    executor.shutdown()

    println("Parallell time: $timeP | Iterative Time: $time")
}
