sealed class Piece(private val text: String) {
    override fun toString() = text

    object Empty : Piece("- ")

    class King(color: Color) : Piece(if (color == Color.WHITE) "K " else "k ")

    class Queen(color: Color) : Piece(if (color == Color.WHITE) "Q " else "q ")

    class Rook(color: Color) : Piece(if (color == Color.WHITE) "R " else "r ")

    class Bishop(color: Color) : Piece(if (color == Color.WHITE) "B " else "b ")

    class Knight(color: Color) : Piece(if (color == Color.WHITE) "N " else "n ")

    class Pawn(color: Color) : Piece(if (color == Color.WHITE) "P " else "p ")

    enum class Color { WHITE, BLACK }
}