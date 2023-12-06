package pawnrace

fun max(x: Int, y: Int): Int =
    when {
        x > y -> x
        else -> y
    }

fun min(x: Int, y: Int): Int =
    when {
        x > y -> y
        else -> x
    }

enum class Piece { B, W, N;
    fun opposite() =
        when (this) {
            B -> W
            W -> B
            else -> N
        }
}

enum class MoveType { PEACEFUL, CAPTURE, EN_PASSANT }

class MutableStack(private var list: MutableList<Move> = mutableListOf()) {
    fun copy(): MutableStack {
        val copyList: MutableList<Move> = mutableListOf()
        list.forEach {
            copyList.add(it.copy())
        }
        return MutableStack(copyList)
    }

    fun isEmpty(): Boolean = list.size == 0

    fun pop(): Move {
        val temp = list.last()
        list.remove(temp)
        return temp
    }

    fun push(elem: Move): MutableStack {
        list.add(elem)
        return this
    }

    fun peek(): Move = list.last()
}

class MoveTree(val game: Game, val move: Move? = null, private var children: List<MoveTree> = listOf()) {
    fun add(g: Game, m: Move) {
        children = children.plus(MoveTree(g, m))
    }

    fun add(m: MoveTree) {
        children = children.plus(m)
    }

    fun getChildren(): List<MoveTree> = children

    fun isTerminal(): Boolean = children.isEmpty()

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$game\n")
        children.forEach { sb.append("$it\n") }
        return sb.toString()
    }
}
