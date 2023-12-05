package pawnrace

class Player(val piece: Piece, val opponent: Player? = null)

fun parseColour(col: Char): Piece =
    when (col) {
        'W' -> Piece.W
        else -> Piece.B
    }
