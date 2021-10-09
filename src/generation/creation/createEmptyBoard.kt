package generation.creation

import generation.models.*

fun createEmptyBoard(size: Int): Board.Initial {
    val tileList = (0 until size).map { tileY ->
        (0 until size).map { tileX ->
            Tile(Location(tileX, tileY), Piece.Empty())
        }
    }.flatten()
    return Board.Initial(size, tileList)
}
