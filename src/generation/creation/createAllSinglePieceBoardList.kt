package generation.creation

import generation.models.Board
import generation.models.Piece
import generation.models.Tile

fun createAllSinglePieceBoardList(emptyBoard: Board.Initial, pieceList: List<Piece>): List<List<Board.Initial>> {
    return pieceList.map { piece ->
        emptyBoard.tileList.map { filledTile ->
            Board.Initial(emptyBoard.size, emptyBoard.tileList.map { emptyTile ->
                if (emptyTile.location == filledTile.location) {
                    Tile(emptyTile.location, piece)
                } else {
                    Tile(emptyTile.location, Piece.Empty())
                }
            })
        }
    }
}
