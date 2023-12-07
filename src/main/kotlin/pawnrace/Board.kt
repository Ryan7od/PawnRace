package pawnrace

class Board {
    var board: Array<Array<Piece>>

    constructor(whiteGap: File, blackGap: File) {
        board = Array(8) { i ->
            when (i) {
                1 -> {
                    Array(8) { j ->
                        if (j == whiteGap.file) Piece.N else Piece.W
                    }
                }
                6 -> {
                    Array(8) { j ->
                        if (j == blackGap.file) Piece.N else Piece.B
                    }
                }
                else -> {
                    Array(8) { Piece.N }
                }
            }
        }
    }

    constructor(b: Array<Array<Piece>>) {
        board = b
    }

    fun copy(): Board {
        val copiedArray = Array(board.size) { Array(board[0].size) { Piece.N } }

        // Iterate through the original array and copy its contents
        for (i in board.indices) {
            for (j in board[i].indices) {
                copiedArray[i][j] = board[i][j]
            }
        }

        return Board(copiedArray)
    }

    fun pieceAt(pos: Position): Piece =
        when (board[pos.rank.rank][pos.file.file]) {
            Piece.B -> Piece.B
            Piece.W -> Piece.W
            else -> Piece.N
        }

    fun positionsOf(piece: Piece): List<Position> {
        val out: MutableList<Position> = mutableListOf()
        board.forEachIndexed { r, e ->
            e.forEachIndexed { f, _ ->
                val pos = Position("${File(f)}${Rank(r)}")
                if (pieceAt(pos) == piece) {
                    out.add(pos)
                }
            }
        }
        return out.toList()
    }

    fun isValidMove(move: Move, lastMove: Move? = null): Boolean {
        if ( // Not a piece at from
            pieceAt(move.from) == Piece.N ||
            // Moving to outside the board
            move.to.rank.rank !in 0..7 ||
            move.to.file.file !in 0..7 ||
            // Moving to a square that already has the same piece
            pieceAt(move.to) == pieceAt(move.from) ||
            // Peaceful move attacking
            (
                move.type == MoveType.PEACEFUL &&
                    (
                        pieceAt(move.to) == pieceAt(move.from).opposite() ||
                            move.to.file.file != move.from.file.file
                        )
                )
        ) {
            return false
        }
        // Peaceful
        if (move.type == MoveType.PEACEFUL) {
            if (pieceAt(move.from) == Piece.B &&
                pieceAt(move.to) == Piece.N &&
                (
                    move.to == move.from.move(-1, 0) ||
                        (
                            move.to == move.from.move(-2, 0) &&
                                move.from.rank.rank == 6
                            )
                    )
            ) {
                return true
            }
            if (pieceAt(move.from) == Piece.W &&
                pieceAt(move.to) == Piece.N &&
                (
                    move.to == move.from.move(1, 0) ||
                        (
                            move.to == move.from.move(2, 0) &&
                                move.from.rank.rank == 1
                            )
                    )
            ) {
                return true
            }
        }
        // Attack
        if (move.type == MoveType.CAPTURE ||
            move.type == MoveType.EN_PASSANT
        ) {
            if (pieceAt(move.from) == Piece.B &&
                (
                    move.to == move.from.move(-1, -1) ||
                        move.to == move.from.move(-1, 1)
                    ) &&
                (
                    pieceAt(move.to) == Piece.W ||
                        (
                            pieceAt(move.to.move(1, 0)) == Piece.W &&
                                (
                                    lastMove != null &&
                                        lastMove.to == lastMove.from.move(2, 0)
                                    )
                            )
                    )
            ) {
                return true
            }
            if (pieceAt(move.from) == Piece.W &&
                (
                    move.to == move.from.move(1, -1) ||
                        move.to == move.from.move(1, 1)
                    ) &&
                (
                    pieceAt(move.from) == pieceAt(move.to).opposite() ||
                        (
                            pieceAt(move.to.move(-1, 0)) == Piece.B &&
                                (
                                    lastMove != null &&
                                        lastMove.to == lastMove.from.move(-2, 0)
                                    )
                            )
                    )
            ) {
                return true
            }
        }
        return false
    }

    fun move(m: Move, lastMove: Move? = null) {
        if (isValidMove(m, lastMove)) {
            board[m.to.rank.rank][m.to.file.file] = board[m.from.rank.rank][m.from.file.file]
            board[m.from.rank.rank][m.from.file.file] = Piece.N
            if (m.type == MoveType.EN_PASSANT) {
                if (m.piece == Piece.B) {
                    board[m.to.rank.rank + 1][m.to.file.file] = Piece.N
                } else {
                    board[m.to.rank.rank - 1][m.to.file.file] = Piece.N
                }
            }
        } else {
            println("Not valid")
        }
    }

    fun isPassedPawn(pos: Position, player: Piece): Boolean {
        val forward: (Int, Int) -> Boolean = when (player) {
            Piece.B -> {
                    x, y ->
                x < y
            }
            else -> {
                    x, y ->
                x > y
            }
        }
        if (pieceAt(pos) == player) {
            positionsOf(player.opposite()).forEach {
                if (it.file.file in pos.file.file - 1..pos.file.file + 1 &&
                    forward(it.rank.rank, pos.rank.rank)
                ) {
                    return false
                }
            }
            return true
        }
        return false
    }

    override fun toString(): String {
        val sb = StringBuilder("   ABCDEFGH   \n\n")
        for (y in 0..7) {
            sb.append("${y + 1}  ")
            for (x in 0..7) {
                if (board[y][x] == Piece.N) {
                    sb.append(".")
                } else {
                    sb.append(board[y][x])
                }
            }
            sb.append("  ${y + 1}\n")
        }
        sb.append("\n   ABCDEFGH   \n")
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean =
        other is Board && other.board.contentEquals(board)

    override fun hashCode(): Int {
        var result = 17
        board.forEachIndexed {
                i, e ->
            e.forEachIndexed {
                    j, _ ->
                result = (i + j) * board[i][j].hashCode() + 7 * result
            }
        }
        return result
    }
}
