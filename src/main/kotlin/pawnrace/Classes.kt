package pawnrace

enum class Piece { B, W, N;
    fun opposite() =
        when (this) {
            B -> W
            W -> B
            else -> N
        }
}

enum class MoveType { PEACEFUL, CAPTURE, EN_PASSANT }

class MutableStack<T>(var list: MutableList<T>) {
    fun isEmpty(): Boolean = list.size == 0

    fun pop(): T {
        val temp = list.last()
        list.remove(temp)
        return temp
    }

    fun push(elem: T): MutableStack<T> {
        list.add(elem)
        return this
    }

    fun peep(): T = list.last()
}

class Game(var board: Board, var player: Piece, val moves: MutableStack<Move> = MutableStack(mutableListOf())){
    fun applyMove(move: Move) {
        board.move(move)
        player = player.opposite()
        moves.push(move)
    }
    fun unnaplyMove() {
        val lastMove = moves.pop()
        board.board[lastMove.from.rank.rank][lastMove.from.file.file] = lastMove.piece
        if (lastMove.type == MoveType.PEACEFUL)
            board.board[lastMove.to.rank.rank][lastMove.to.file.file] = Piece.N
        if(lastMove.type == MoveType.CAPTURE)
            board.board[lastMove.to.rank.rank][lastMove.to.file.file] = lastMove.piece.opposite()
        else{
            if (lastMove.piece == Piece.B)
                board.board[lastMove.to.rank.rank+1][lastMove.to.file.file] = Piece.W
            else
                board.board[lastMove.to.rank.rank-1][lastMove.to.file.file] = Piece.B
        }

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
                move.to == move.from.move(-1, 0)
            ) {
                return true
            }
            if (pieceAt(move.from) == Piece.W &&
                pieceAt(move.to) == Piece.N &&
                move.to == move.from.move(1, 0)
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
            if(m.type == MoveType.EN_PASSANT){
                if (m.piece == Piece.B)
                    board[m.to.rank.rank+1][m.to.file.file] = Piece.N
                else
                    board[m.to.rank.rank-1][m.to.file.file] = Piece.N
            }
        }
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

    fun move(x: Int, y: Int): Position = Position("${File(file.file + x)}${Rank(rank.rank + y)}")
}
