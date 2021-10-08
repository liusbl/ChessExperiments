package generation

import generation.models.*

fun createEmptyBoard(size: Int): Boo.Initial {
    val tileList = (0 until size).map { tileY ->
        (0 until size).map { tileX ->
            Tile(Location(tileX, tileY), Piece.Empty())
        }
    }.flatten()
    return Boo.Initial(size, tileList)
}
