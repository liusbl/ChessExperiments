sealed class Piece(
    val letter: Char,
    val movement: Movement,
    val color: Color
) {
    override fun toString() = letter.toString()

    object Empty : Piece('-', Movement.None, color = Color.WHITE)

    class King(color: Color) : Piece(
        if (color == Color.WHITE) 'K' else 'k',
        Movement.Basic(Movement.Basic.Distance.ONE, Movement.Basic.Direction.OrthogonalAndDiagonal),
        color
    )

    class Queen(color: Color) : Piece(
        if (color == Color.WHITE) 'Q' else 'q',
        Movement.Basic(Movement.Basic.Distance.N, Movement.Basic.Direction.OrthogonalAndDiagonal),
        color
    )

    class Rook(color: Color) : Piece(if (color == Color.WHITE) 'R' else 'r', Movement.None, color)

    class Bishop(color: Color) : Piece(if (color == Color.WHITE) 'B' else 'b', Movement.None, color)

    class Knight(color: Color) : Piece(if (color == Color.WHITE) 'N' else 'n', Movement.None, color)

    class Pawn(color: Color) : Piece(if (color == Color.WHITE) 'P' else 'p', Movement.None, color)

    enum class Color { WHITE, BLACK }
}