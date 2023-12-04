package pawnrace

enum class Piece { B, W, N }

enum class MoveType { PEACEFUL, CAPTURE, EN_PASSANT }

data class Move(
    val piece: Piece,
    val from: Position,
    val to: Position,
    val type: MoveType,
) {
    override fun toString(): String {
        return if (type == MoveType.PEACEFUL) {
            to.toString()
        } else {
            "${from.file}x$to"
        }
    }
}

class Board(val whiteGap: Rank, val blackGap: Rank) {
    private var board: Array<Array<Piece>> = Array(8) { i ->
        if (i == 1) {
            Array(8) { j ->
                if (j == whiteGap.rank) Piece.N else Piece.W
            }
        } else if (i == 6) {
            Array(8) { j ->
                if (j == blackGap.rank) Piece.N else Piece.B
            }
        } else {
            Array(8) { j -> Piece.N }
        }
    }

    fun pieceAt(pos: Position): Piece? =
        when (board[pos.rank.rank][pos.file.file]) {
            Piece.B -> Piece.B
            Piece.W -> Piece.W
            else -> null
        }

    fun positionsOf(piece: Piece): List<Position> {
        val out: MutableList<Position> = mutableListOf()
        board.forEachIndexed { r, e ->
            e.forEachIndexed { f, ee ->
                if (ee == piece) {
                    out.add(
                        Position(
                            (f + 'a'.code).toChar().uppercase() +
                                "${r + 1}",
                        ),
                    )
                }
            }
        }
        return out.toList()
    }
}

class File(val file: Int) {
    override fun toString(): String {
        return (file + 'a'.code).toChar().uppercase()
    }

    override fun equals(other: Any?): Boolean {
        return other is File && other.file == file
    }

    override fun hashCode(): Int {
        return file
    }
}

class Rank(val rank: Int) {
    override fun toString(): String {
        return (rank + 1).toString()
    }

    override fun equals(other: Any?): Boolean {
        return other is Rank && other.rank == rank
    }

    override fun hashCode(): Int {
        return rank
    }
}

data class Position(val pos: String) {
    var file = File(pos.lowercase()[0].code - 'a'.code)
    var rank = Rank(pos[1].digitToInt())

    override fun toString(): String {
        return "$file$rank"
    }
}
