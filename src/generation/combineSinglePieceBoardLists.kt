package generation

import generation.models.Boo
import generation.models.Piece

fun combineSinglePieceBoardLists(singlePieceBoardList: List<List<Boo.Initial>>): List<Boo.Initial> {
    return singlePieceBoardList.reduce { acc: List<Boo.Initial>, next: List<Boo.Initial> ->
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
