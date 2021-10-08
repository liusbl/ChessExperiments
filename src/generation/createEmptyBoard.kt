package generation

import Board
import generation.models.Location
import generation.models.Piece
import generation.models.Tile

fun createEmptyBoard(size: Int): Board.Partial {
    val tileList = (0 until size).map { tileY ->
        (0 until size).map { tileX ->
            Tile(Location(tileX, tileY), Piece.Empty)
        }
    }.flatten()
    return Board.Partial(size, tileList)
}
