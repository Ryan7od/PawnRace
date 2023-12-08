package pawnrace

import java.util.concurrent.Executors
import kotlin.random.Random

class Game(var board: Board, var player: Piece, val moves: MutableStack = MutableStack(mutableListOf())) {
    fun applyMove(move: Move): Game {
        val newBoard = board.copy()
        if (moves.isEmpty()) {
            newBoard.move(move)
        } else {
            newBoard.move(move, moves.peek())
        }
        return Game(newBoard, player.opposite(), moves.copy().push(move))
    }

    fun unapplyMove() {
        val lastMove = moves.pop()
        player = player.opposite()
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

    fun moves(piece: Piece = player): List<Move> {
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
            if (it.rank.rank + forward in 0..7) {
                if (it.rank.rank == 1 && piece == Piece.W &&
                    board.pieceAt(it.move(forward, 0)) == Piece.N &&
                    board.pieceAt(it.move(2 * forward, 0)) == Piece.N
                ) {
                    list.add(Move(piece, it, it.move(2 * forward, 0), MoveType.PEACEFUL))
                }
                if (it.rank.rank == 6 && piece == Piece.B &&
                    board.pieceAt(it.move(forward, 0)) == Piece.N &&
                    board.pieceAt(it.move(2 * forward, 0)) == Piece.N
                ) {
                    list.add(Move(piece, it, it.move(2 * forward, 0), MoveType.PEACEFUL))
                }
                if (board.pieceAt(it.move(forward, 0)) == Piece.N) {
                    list.add(Move(piece, it, it.move(forward, 0), MoveType.PEACEFUL))
                }
                if (it.file.file < 7 &&
                    board.pieceAt(it.move(forward, 1)) == piece.opposite()
                ) {
                    list.add(Move(piece, it, it.move(forward, 1), MoveType.CAPTURE))
                }
                if (it.file.file > 0 &&
                    board.pieceAt(it.move(forward, -1)) == piece.opposite()
                ) {
                    list.add(Move(piece, it, it.move(forward, -1), MoveType.CAPTURE))
                }
            }
            if (lastMove != null && (it.rank.rank == 3 || it.rank.rank == 5)) {
                if (it.file.file < 7 &&
                    board.pieceAt(it.move(0, 1)) == piece.opposite() &&
                    lastMove.to == it.move(0, 1) &&
                    lastMove.from == it.move(2 * forward, 1)
                ) {
                    list.add(Move(piece, it, it.move(forward, 1), MoveType.EN_PASSANT))
                }
                if (it.file.file > 0 &&
                    board.pieceAt(it.move(0, -1)) == piece.opposite() &&
                    lastMove.to == it.move(0, -1) &&
                    lastMove.from == it.move(2 * forward, -1)
                ) {
                    list.add(Move(piece, it, it.move(forward, -1), MoveType.EN_PASSANT))
                }
            }
        }
        return list.toList().filter { board.isValidMove(it, lastMove) }
    }

    fun randomMove(p: Piece): Move =
        moves(p)[Random.nextInt(0, moves(p).size)]

    fun over(): Boolean {
        if (board.positionsOf(Piece.B).isEmpty() ||
            board.positionsOf(Piece.W).isEmpty() ||
            moves(player).isEmpty()
        ) {
            return true
        }
        board.positionsOf(Piece.B).forEach { if (it.rank.rank == 0) return true }
        board.positionsOf(Piece.W).forEach { if (it.rank.rank == 7) return true }
        return false
    }

    fun winner(): Piece {
        if (board.positionsOf(Piece.B).isEmpty()) return Piece.W
        if (board.positionsOf(Piece.W).isEmpty()) return Piece.B
        board.positionsOf(Piece.B)
            .forEach { if (it.rank.rank == 0) return Piece.B }
        board.positionsOf(Piece.W)
            .forEach { if (it.rank.rank == 7) return Piece.W }
        return Piece.N
    }

    fun parseMove(san: String, piece: Piece): Move? {
        val file = san.lowercase()[0].code - 'a'.code
        val rank = san[san.length - 1].digitToInt() - 1
        val peace = san[1] != 'x'
        var pos: Position = Position("A1")
        val pos2: Position = Position(san.substring(san.length - 2))
        var type: MoveType = MoveType.PEACEFUL
        val forward = when (piece) {
            Piece.B -> -1
            else -> 1
        }
        if (peace) {
            board.positionsOf(piece).forEach {
                if (it.file.file == file) {
                    if (it.move(forward, 0) == pos2) {
                        pos = it
                    } else if (it.move(forward * 2, 0) == pos2) {
                        pos = it
                    }
                }
            }
        } else {
            board.positionsOf(piece).forEach {
                if (it.file.file == file &&
                    (
                        it == pos2.move(-forward, 1) ||
                            it == pos2.move(-forward, -1)
                        )
                ) {
                    pos = it
                    type = if (board.pieceAt(pos2) == piece.opposite()) {
                        MoveType.CAPTURE
                    } else {
                        MoveType.EN_PASSANT
                    }
                }
            }
        }
        return Move(piece, pos, pos2, type)
    }

    override fun toString() = board.toString()

    override fun equals(other: Any?): Boolean =
        other is Game && other.board == board

    override fun hashCode(): Int {
        return board.hashCode()
    }
}

fun main() {
    var game = Game(Board(File(3), File(4)), Piece.W)
//    val move = game.parseMove("d3", Piece.W)!!
//    println(game.board.positionsOf(Piece.W))
//    println("${ move.from } ${move.to} ${move.piece} ${move.type}")
//    println(game.board.isValidMove(move))
    game = game.applyMove(game.parseMove("b3", Piece.W)!!)
    println(game)
    game = game.applyMove(game.parseMove("a5", Piece.B)!!)
    println(game)
    game = game.applyMove(game.parseMove("a4", Piece.W)!!)
    println(game)
    game = game.applyMove(game.parseMove("e6", Piece.B)!!)
    println(game)
    game = game.applyMove(game.parseMove("d3", Piece.W)!!)
    println(game)
    game = game.applyMove(game.parseMove("d5", Piece.B)!!)
    println(game)
    game = game.applyMove(game.parseMove("e3", Piece.W)!!)
    println(game)
    game = game.applyMove(game.parseMove("d4", Piece.B)!!)
    println(game)
    game = game.applyMove(game.parseMove("exd4", Piece.W)!!)
    println(game)
    game = game.applyMove(game.parseMove("g6", Piece.B)!!)
    println(game)
    game = game.applyMove(game.parseMove("b4", Piece.W)!!)
    println(game)
//    game = game.applyMove(game.parseMove("f6", Piece.B)!!)
//    println(game)
//    game = game.applyMove(game.parseMove("bxa5", Piece.W)!!)
//    println(game)

    val executor = Executors.newSingleThreadExecutor()
//    println(findBestMoveN(game, 5, Piece.W, hash = HashMap<Game, Pair<Int, Int>>()))
    println(itDeepN(game, 5, 4500, Piece.B, hash = HashMap<Game, Pair<Int, Int>>(), executor))
    executor.shutdown()
}
