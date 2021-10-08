package generation

import generation.models.Boo
import generation.models.Piece
import generation.models.Tile

fun createAllSinglePieceBoardList(emptyBoard: Boo.Initial, pieceList: List<Piece>): List<List<Boo.Initial>> {
    return pieceList.map { piece ->
        emptyBoard.tileList.map { filledTile ->
            Boo.Initial(emptyBoard.size, emptyBoard.tileList.map { emptyTile ->
                if (emptyTile.location == filledTile.location) {
                    Tile(emptyTile.location, piece)
                } else {
                    Tile(emptyTile.location, Piece.Empty())
                }
            })
        }
    }
}
