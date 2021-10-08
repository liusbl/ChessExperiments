package generation.models

sealed class Piece {
    abstract val color: Color
    abstract val letter: Char
    abstract val movement: Movement

    override fun toString() = letter.toString()

    data class Empty(
        override val color: Color = Color.WHITE,
        override val letter: Char = '-',
        override val movement: Movement = Movement.None
    ) : Piece()

    data class King(
        override val color: Color,
        override val letter: Char = if (color == Color.WHITE) 'K' else 'k',
        override val movement: Movement = Movement.Basic(
            Movement.Basic.Distance.ONE,
            Movement.Basic.Direction.OrthogonalAndDiagonal
        )
    ) : Piece()

    data class Queen(
        override val color: Color,
        override val letter: Char = if (color == Color.WHITE) 'Q' else 'q',
        override val movement: Movement = Movement.Basic(
            Movement.Basic.Distance.N,
            Movement.Basic.Direction.OrthogonalAndDiagonal
        )
    ) : Piece()

//    class Rook(color: Color) : Piece(if (color == Color.WHITE) 'R' else 'r', Movement.None, color)
//
//    class Bishop(color: Color) : Piece(if (color == Color.WHITE) 'B' else 'b', Movement.None, color)
//
//    class Knight(color: Color) : Piece(if (color == Color.WHITE) 'N' else 'n', Movement.None, color)
//
//    class Pawn(color: Color) : Piece(if (color == Color.WHITE) 'P' else 'p', Movement.None, color)

    enum class Color { WHITE, BLACK }
}