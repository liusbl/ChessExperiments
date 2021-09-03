sealed class Piece(
    val letter: Char,
    val movement: Movement
) {
    override fun toString() = letter.toString()

    object Empty : Piece('-', Movement.None)

    class King(color: Color) : Piece(if (color == Color.WHITE) 'K' else 'k', Movement.None)

    class Queen(color: Color) : Piece(if (color == Color.WHITE) 'Q' else 'q', Movement.None)

    class Rook(color: Color) : Piece(if (color == Color.WHITE) 'R' else 'r', Movement.None)

    class Bishop(color: Color) : Piece(if (color == Color.WHITE) 'B' else 'b', Movement.None)

    class Knight(color: Color) : Piece(if (color == Color.WHITE) 'N' else 'n', Movement.None)

    class Pawn(color: Color) : Piece(if (color == Color.WHITE) 'P' else 'p', Movement.None)

    enum class Color { WHITE, BLACK }
}