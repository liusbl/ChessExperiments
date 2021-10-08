package generation

import generation.models.Board
import generation.models.Piece
import generation.models.Tile

fun createAllSinglePieceBoardList(emptyBoard: Board.Partial, pieceList: List<Piece>): List<List<Board.Partial>> {
    return pieceList.map { piece ->
        emptyBoard.tileList.map { filledTile ->
            Board.Partial(emptyBoard.size, emptyBoard.tileList.map { emptyTile ->
                if (emptyTile.location == filledTile.location) {
                    Tile(emptyTile.location, piece)
                } else {
                    Tile(emptyTile.location, Piece.Empty)
                }
            })
        }
    }
}
