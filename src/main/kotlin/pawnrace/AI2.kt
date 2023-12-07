package pawnrace

import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.pow

fun evaluate(game: Game, me: Piece): Int {
    val ot = me.opposite()
    var score = 0
    val posMe = game.board.positionsOf(me)
    val posOt = game.board.positionsOf(ot)

    // number of pawns
    score += 200 * (posMe.size - posOt.size)

    // doubled
    score -= 30 * posMe.map { a ->
        if (posMe.map { it.file.file }.contains(a.file.file)) {
            1
        } else {
            0
        }
    }.sum()

    score += 30 * posOt.map { a ->
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
    score += 15 * posMe.sumOf {
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

    score -= 15 * posOt.sumOf {
        if (game.board.isPassedPawn(it, ot)) {
            val rank = if (me == Piece.W) {
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
    score += 3 * posMe.sumOf {
        val rank = if (me == Piece.W) {
            it.rank.rank
        } else {
            7 - it.rank.rank
        }
        2.0.pow(rank).toInt()
    }

    score -= 3 * posOt.sumOf {
        val rank = if (me == Piece.W) {
            it.rank.rank
        } else {
            7 - it.rank.rank
        }
        2.0.pow(rank).toInt()
    }

    return score
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
            else -> 1
        }
    }

    var moves = game.moves(player)
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

    // IID
    if (depth > 3) {
        val m = findBestMoveN(game, 3, player, hash)
        if (m != null) {
            moves = listOf(m).plus(moves)
        }
    }

    var alpha = a
    var score: Int

    val firstMove = moves.firstOrNull()
    score = if (firstMove != null) {
        -negaScout(
            game.applyMove(firstMove),
            depth - 1,
            -beta,
            -alpha,
            player.opposite(),
            me,
            hash,
        )
    } else {
        evaluate(game, me)
    }

    alpha = maxOf(alpha, score)

    for (move in moves.drop(0)) {
        val newGame = game.applyMove(move)
        score = -negaScout(
            newGame,
            depth - 1,
            -alpha - 1,
            -alpha,
            player.opposite(),
            me,
            hash,
        )

        if (score in alpha..beta) {
            score = -negaScout(
                newGame,
                depth - 1,
                -beta,
                -score,
                player.opposite(),
                me,
                hash,
            )

            alpha = maxOf(alpha, score)
            if (alpha >= beta) {
                break
            }
        }
    }
    return alpha
}

fun findBestMoveN(
    game: Game,
    depth: Int,
    player: Piece,
    hash: HashMap<Game, Pair<Int, Int>>,
): Move? {
    val moves = game.moves(player)
    var bestMove: Move? = null
    var bestValue = Int.MIN_VALUE

    for (move in moves) {
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

        if (value > bestValue) {
            bestValue = value
            bestMove = move
        }
    }
    return bestMove
}

fun itDeepN(
    game: Game,
    maxDepth:
    Int,
    timeLimitMillis: Long,
    player: Piece,
    hash: HashMap<Game, Pair<Int, Int>>,
    executor: ExecutorService,
): Move? {
    var bestMove: Move? = null
    var depth = 3

    try {
        val future = executor.submit {
            while (true) {
                bestMove = findBestMoveN(game, depth, player, hash)
                depth++
            }
        }
        future[timeLimitMillis, TimeUnit.MILLISECONDS]
    } catch (_: Exception) {
    } finally {
        executor.shutdown()
    }

    return bestMove
}
