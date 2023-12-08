package pawnrace

import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.pow

fun evaluate(game: Game, me: Piece): Int {
    val ot = me.opposite()
    var score: Int = 0
    val posMe = game.board.positionsOf(me)
    val posOt = game.board.positionsOf(ot)

    // Winning case
    posMe.forEach {
        if (it.rank.rank >= 6 && me == Piece.W) {
            return Int.MAX_VALUE
        }
        if (it.rank.rank <= 1 && me == Piece.B) {
            return Int.MAX_VALUE
        }
    }
    posOt.forEach {
        if (it.rank.rank >= 6 && me == Piece.B) {
            return Int.MIN_VALUE
        }
        if (it.rank.rank <= 1 && me == Piece.W) {
            return Int.MIN_VALUE
        }
    }

    // Pawn support
    score += 50 * posMe.sumOf {
        game.board.supported(it, me)
    }
    score -= 50 * posOt.sumOf {
        game.board.supported(it, ot)
    }

    // Isolated pawns
    score -= 200 * posMe.sumOf { a ->
        if ((a.file.file < 7 && posMe.map { it.file.file }.contains(a.file.file + 1)) ||
            (a.file.file > 0 && posMe.map { it.file.file }.contains(a.file.file - 1))
        ) {
            0.toInt()
        } else {
            1.toInt()
        }
    }
    score += 200 * posOt.sumOf { a ->
        if ((a.file.file < 7 && posOt.map { it.file.file }.contains(a.file.file + 1)) ||
            (a.file.file > 0 && posOt.map { it.file.file }.contains(a.file.file - 1))
        ) {
            0.toInt()
        } else {
            1.toInt()
        }
    }

    // number of pawns
    score += 450 * (posMe.size - posOt.size)

    // doubled
    score -= 50 * posMe.map { a ->
        if (posMe.map { it.file.file }.contains(a.file.file)) {
            1
        } else {
            0
        }
    }.sum()

    score += 50 * posOt.map { a ->
        if (posOt.map { it.file.file }.contains(a.file.file)) {
            1
        } else {
            0
        }
    }.sum()

    // blocked
    // advancement when protected
    // control centre??
    // chains - 3 > 2*2
    // passed
    score += 50 * posMe.sumOf {
        if (game.board.isPassedPawn(it, me)) {
            val rank = if (me == Piece.W) {
                it.rank.rank
            } else {
                7 - it.rank.rank
            }
            2.0.pow(rank).toInt()
        } else {
            0
        }
    }
    score -= 50 * posOt.sumOf {
        if (game.board.isPassedPawn(it, ot)) {
            val rank = if (ot == Piece.W) {
                it.rank.rank
            } else {
                7 - it.rank.rank
            }
            2.0.pow(it.rank.rank).toInt()
        } else {
            0
        }
    }

    // rank of pawns - 4 >> 3
    score += 2 * posMe.sumOf {
        val rank = if (me == Piece.W) {
            it.rank.rank
        } else {
            7 - it.rank.rank
        }
        2.0.pow(rank).toInt()
    }

    score -= 2 * posOt.sumOf {
        val rank = if (me == Piece.W) {
            it.rank.rank
        } else {
            7 - it.rank.rank
        }
        2.0.pow(rank).toInt()
    }

    return score
}

fun order(list: List<Move>): List<Move> {
    val listOut: MutableList<Move> = mutableListOf()
    list.forEach {
        val move = it
        if (move.toString().length != 2) {
            listOut.add(it)
        }
    }
    listOut += list.minus(listOut.toSet())
    return listOut.toList()
}

fun negaScout(
    game: Game,
    depth: Int,
    a: Int,
    beta: Int,
    player: Piece,
    me: Piece,
    hash: HashMap<Game, Pair<Int, Int>>,
): Int {
    val cached = hash[game]
    if (cached != null && cached.second >= depth) {
        return cached.first
    }

    if (game.over()) {
        return when (game.winner()) {
            me -> Int.MAX_VALUE
            me.opposite() -> Int.MIN_VALUE
            else -> -1
        }
    }

    val moves = order(game.moves(player))
    if (depth <= 0) {
        var value = Int.MIN_VALUE
        // Capture Quiescence Search
        moves.filter { it.type == MoveType.CAPTURE }
            .forEach {
                value = maxOf(
                    value,
                    -negaScout(
                        game.applyMove(it),
                        depth - 1,
                        -beta,
                        -a,
                        player.opposite(),
                        me,
                        hash,
                    ),
                )
            }
        return maxOf(value, evaluate(game, me))
    }

    var alpha = a
    var score: Int

    for (move in moves) {
        val newGame = game.applyMove(move)
        score = -negaScout(
            newGame,
            depth - 1,
            -beta,
            -alpha,
            player.opposite(),
            me,
            hash,
        )
        alpha = maxOf(alpha, score)
        if (alpha >= beta) {
            break
        }
    }
    hash[game] = Pair(alpha, depth)
    return alpha
}

fun findBestMoveN(
    game: Game,
    depth: Int,
    player: Piece,
    hash: HashMap<Game, Pair<Int, Int>>,
    startTime: Long,
    timeLimit: Long,
): Move? {
    var totalTime = System.currentTimeMillis() - startTime
    val moves = game.moves(player)
    println(moves)
    var bestMove: Move? = null
    var bestValue = Int.MIN_VALUE

    // Instant win push

    for (move in moves) {
        if (totalTime > timeLimit) {
            break
        }
        if (player == Piece.W &&
            move.from.rank.rank >= 5
        ) {
            return move
        } else if (player == Piece.B &&
            move.from.rank.rank <= 2
        ) {
            return move
        }
        val newGame = game.applyMove(move)
        val value = -negaScout(
            newGame,
            depth - 1,
            Int.MIN_VALUE,
            Int.MAX_VALUE,
            player.opposite(),
            player,
            hash,
        )
        println("$move - $value")
        if (value > bestValue) {
            bestValue = value
            bestMove = move
        }
        totalTime = System.currentTimeMillis() - startTime
    }
    return bestMove
}

fun itDeepN(
    game: Game,
    timeLimitMillis: Long,
    player: Piece,
    hash: HashMap<Game, Pair<Int, Int>>,
    executor: ExecutorService,
): Move? {
    var bestMove: Move? = null
    var depth = 2

    try {
        val future = executor.submit {
            val startTime = System.currentTimeMillis()
            var totalTime = startTime - System.currentTimeMillis()
            while (totalTime <= timeLimitMillis) {
                bestMove = findBestMoveN(game, depth, player, hash, startTime, timeLimitMillis)
                depth++
                totalTime = System.currentTimeMillis() - startTime
            }
        }
        val result = future.get(timeLimitMillis, TimeUnit.MILLISECONDS)

        future.cancel(true)
        executor.shutdownNow()
    } catch (_: Exception) {
    }

    return bestMove
}
