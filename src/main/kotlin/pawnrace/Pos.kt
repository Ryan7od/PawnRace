package pawnrace

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
