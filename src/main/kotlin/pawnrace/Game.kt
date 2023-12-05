package pawnrace

import kotlin.math.abs

class Game(var board: Board, var player: Piece, val moves: MutableStack<Move> = MutableStack(mutableListOf())) {
    fun applyMove(move: Move) {
        board.move(move)
        player = player.opposite()
        moves.push(move)
    }
    fun unnaplyMove() {
        val lastMove = moves.pop()
        board.board[lastMove.from.rank.rank][lastMove.from.file.file] = lastMove.piece
        if (lastMove.type == MoveType.PEACEFUL) {
            board.board[lastMove.to.rank.rank][lastMove.to.file.file] = Piece.N
        }
        if (lastMove.type == MoveType.CAPTURE) {
            board.board[lastMove.to.rank.rank][lastMove.to.file.file] = lastMove.piece.opposite()
        } else {
            if (lastMove.piece == Piece.B) {
                board.board[lastMove.to.rank.rank + 1][lastMove.to.file.file] = Piece.W
            } else {
                board.board[lastMove.to.rank.rank - 1][lastMove.to.file.file] = Piece.B
            }
        }
    }

    fun moves(piece: Piece): List<Move> {
        val lastMove: Move? = if (moves.isEmpty()) {
            null
        } else {
            moves.peek()
        }
        val forward: Int = if (piece == Piece.B) {
            -1
        } else {
            1
        }
        val list: MutableList<Move> = mutableListOf()
        board.positionsOf(piece).forEach {
            if (board.pieceAt(it.move(forward, 0)) == Piece.N) {
                list.add(Move(piece, it, it.move(forward, 0), MoveType.PEACEFUL))
            }
            if (it.file.file < 7 && board.pieceAt(it.move(forward, 1)) == piece.opposite()) {
                list.add(Move(piece, it, it.move(forward, 1), MoveType.CAPTURE))
            }
            if (it.file.file > 0 && board.pieceAt(it.move(forward, -1)) == piece.opposite()) {
                list.add(Move(piece, it, it.move(forward, -1), MoveType.CAPTURE))
            }
            if (lastMove != null) {
                if (it.file.file < 7 &&
                    it.rank.rank < 6 && it.rank.rank > 1 &&
                    board.pieceAt(it.move(0, 1)) == piece.opposite() &&
                    lastMove.to == it.move(0, 1) &&
                    lastMove.from == it.move(2 * forward, 1)
                ) {
                    list.add(Move(piece, it, it.move(forward, 1), MoveType.EN_PASSANT))
                }
                if (it.file.file > 0 &&
                    it.rank.rank < 6 && it.rank.rank > 1 &&
                    board.pieceAt(it.move(0, -1)) == piece.opposite() &&
                    lastMove.to == it.move(0, -1) &&
                    lastMove.from == it.move(2 * forward, -1)
                ) {
                    list.add(Move(piece, it, it.move(forward, -1), MoveType.EN_PASSANT))
                }
            }
        }
        return list.toList()
    }

    fun over(): Boolean {
        if (board.positionsOf(Piece.B).isEmpty() ||
            board.positionsOf(Piece.W).isEmpty()
        ) {
            return true
        }
        board.positionsOf(Piece.B).forEach { if (it.rank.rank == 0) return true }
        board.positionsOf(Piece.W).forEach { if (it.rank.rank == 7) return true }
        return false
    }

    fun winner(): Piece? {
        if (board.positionsOf(Piece.B).isEmpty()) return Piece.W
        if (board.positionsOf(Piece.W).isEmpty()) return Piece.B
        board.positionsOf(Piece.B).forEach { if (it.rank.rank == 0) return Piece.B }
        board.positionsOf(Piece.W).forEach { if (it.rank.rank == 7) return Piece.W }
        return null
    }

    fun parseMove(san: String): Move? {
        val move: Move
        val file = san[0].uppercase()
        val rank = san.last().digitToInt()
        var pos: Position? = null
        var piece: Piece = Piece.N
        var type = MoveType.CAPTURE
        board.positionsOf(Piece.B).forEach {
            if (it.file.toString() == file) {
                if (abs(it.rank.rank - rank) <= 1) {
                    pos = it
                    piece = Piece.B
                }
            }
        }
        board.positionsOf(Piece.W).forEach {
            if (it.file.toString() == file) {
                if (abs(it.rank.rank - rank) == 1) {
                    pos = it
                    piece = Piece.W
                }
                if (it.rank.rank == rank) {
                    pos = it
                    piece = Piece.W
                    type = MoveType.EN_PASSANT
                }
            }
        }
        if (pos == null) {
            return null
        }
        val pos2 = Position(file + rank.toString())
        if (san.length == 2) {
            move = Move(piece, pos!!, pos2, MoveType.PEACEFUL)
        } else {
            move = Move(piece, pos!!, pos2, type)
        }
        return move
    }
}

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

class Board(val whiteGap: File, val blackGap: File) {
    var board: Array<Array<Piece>> = Array(8) { i ->
        if (i == 1) {
            Array(8) { j ->
                if (j == whiteGap.file) Piece.N else Piece.W
            }
        } else if (i == 6) {
            Array(8) { j ->
                if (j == blackGap.file) Piece.N else Piece.B
            }
        } else {
            Array(8) { j -> Piece.N }
        }
    }

    fun copy(): Board = this

    fun pieceAt(pos: Position): Piece =
        when (board[pos.rank.rank][pos.file.file]) {
            Piece.B -> Piece.B
            Piece.W -> Piece.W
            else -> Piece.N
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
                    pieceAt(move.to) == pieceAt(move.from).opposite()
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
                        move.to == move.from.move(-2, 0)
                    )
            ) {
                return true
            }
            if (pieceAt(move.from) == Piece.W &&
                pieceAt(move.to) == Piece.N &&
                (
                    move.to == move.from.move(1, 0) ||
                        move.to == move.from.move(2, 0)
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
                    pieceAt(move.from) == pieceAt(move.to).opposite() ||
                        (
                            pieceAt(move.to.move(1, 0)) == Piece.W &&
                                (lastMove?.to == lastMove?.from?.move(2, 0))
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
                                (lastMove?.to == lastMove?.from?.move(-2, 0))
                            )
                    )
            ) {
                return true
            }
        }
        return false
    }

    fun move(m: Move) {
        if (isValidMove(m)) {
            board[m.to.rank.rank][m.to.file.file] = board[m.from.rank.rank][m.from.file.file]
            board[m.from.rank.rank][m.from.file.file] = Piece.N
            if (m.type == MoveType.EN_PASSANT) {
                if (m.piece == Piece.B) {
                    board[m.to.rank.rank + 1][m.to.file.file] = Piece.N
                } else {
                    board[m.to.rank.rank - 1][m.to.file.file] = Piece.N
                }
            }
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
                if (board[x][y] == Piece.N) {
                    sb.append(".")
                } else {
                    sb.append(board[x][y])
                }
            }
            sb.append("  ${y + 1}\n")
        }
        sb.append("\n   ABCDEFGH   \n")
        return sb.toString()
    }
}
