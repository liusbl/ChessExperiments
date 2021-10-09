package generation.creation

import generation.models.Board
import generation.models.Piece

fun combineSinglePieceBoardLists(singlePieceBoardList: List<List<Board.Initial>>): List<Board.Initial> {
    return singlePieceBoardList.reduce { acc: List<Board.Initial>, next: List<Board.Initial> ->
        acc.map { combinedBoard ->
            next.map { nextBoard ->
                val updatedTileList = combinedBoard.tileList.zip(nextBoard.tileList) { oldTile, nextTile ->
                    if (oldTile.piece == Piece.Empty()) {
                        oldTile.copy(piece = nextTile.piece)
                    } else {
                        oldTile
                    }
                }
                nextBoard.copy(tileList = updatedTileList)
            }
        }.flatten()
    }.filter { board ->
        board.tileList.count { tile ->
            tile.piece !is Piece.Empty
        } == singlePieceBoardList.size
    }
}
