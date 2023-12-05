package pawnrace

import kotlin.math.abs

class Game(var board: Board, var player: Piece, val moves: MutableStack = MutableStack(mutableListOf())) {
    fun applyMove(move: Move): Game =
        Game(board.copy(), player, moves.copy())

    fun unapplyMove() {
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
        move = if (san.length == 2) {
            Move(piece, pos!!, pos2, MoveType.PEACEFUL)
        } else {
            Move(piece, pos!!, pos2, type)
        }
        return move
    }
}
