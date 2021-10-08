package generation

import generation.models.Board
import generation.models.Piece

fun combineSinglePieceBoardLists(singlePieceBoardList: List<List<Board.Partial>>): List<Board.Partial> {
    return singlePieceBoardList.reduce { acc: List<Board.Partial>, next: List<Board.Partial> ->
        acc.map { combinedBoard ->
            next.map { nextBoard ->
                val updatedTileList = combinedBoard.tileList.zip(nextBoard.tileList) { oldTile, nextTile ->
                    if (oldTile.piece == Piece.Empty) {
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
