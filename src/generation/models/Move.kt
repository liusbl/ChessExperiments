package generation.models

enum class Move(val letter: Char) {
    WHITE('w'),
    BLACK('b')
}

fun Move.next() = if (this == Move.WHITE) Move.BLACK else Move.WHITE