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

    fun peek(): T = list.last()
}
