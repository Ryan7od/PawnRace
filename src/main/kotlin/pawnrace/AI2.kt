package pawnrace

import kotlin.system.measureTimeMillis

fun evaluate(game: Game, player: Piece): Int {
    return 0
}

fun abMinimaxP(game: Game, depth: Int, a: Int, b: Int, player: Piece, maxPlayer: Boolean = true): Int {
    if (depth == 0 || game.over()) {
        return evaluate(game)
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

fun negaScout(game: Game, depth: Int, a: Int, b: Int, player: Piece, me: Piece): Int {
    var alpha = a
    var beta = b
    if(depth == 0 || game.over()) {
        evaluate(game, me)
    }
    var score: Int
    val moves = game.moves(player)
    for(move in moves) {
        if (move == moves.first()) {
            score = -negaScout(game.applyMove(move), depth - 1, -beta, -alpha, player.opposite(), me)
        } else {
            score = -negaScout(game.applyMove(move), depth - 1, -alpha - 1, -alpha, player.opposite(), me)
            if (score in alpha..beta) {
                score = -negaScout(game.applyMove(move), depth - 1, -beta, -alpha, player.opposite(), me)
            }
        }
        alpha = maxOf(alpha, score)
        if(alpha >= beta) {
            break
        }
    }
    return alpha
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

fun findBestMoveN(game: Game, depth: Int, player: Piece, best: Move? = null): Move? {
    TODO(/*somehow order so best from previous search is first in negamax call*/)
    val moves = game.moves(player)
    var bestMove: Move? = null
    var bestValue = Int.MIN_VALUE

    for (move in moves) {
        val newGame = game.applyMove(move)
        val value = -negaScout(newGame, depth - 1, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, player.opposite(), player)

        if (value > bestValue) {
            bestValue = value
            bestMove = move
        }
    }
    return bestMove
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

fun itDeepN(game: Game, maxDepth: Int, timeLimitMillis: Long, player: Piece): Move? {
    var bestMove: Move? = null
    var depth = 4
    var elapsedTime: Long = 0

    while (depth <= maxDepth && elapsedTime < timeLimitMillis) {
        val timeTaken = measureTimeMillis {
            bestMove = findBestMoveN(game, depth, player)
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
