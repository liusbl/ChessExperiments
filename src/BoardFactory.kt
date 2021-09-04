object BoardFactory {
    fun createAllPieceBoards(size: Int, pieces: List<Piece>): List<Board> {
        val emptyBoard = Board.createEmpty(size)

        val singlePieceBoards = pieces.map { piece ->
            emptyBoard.tileList.map { filledTile ->
                Board(size, emptyBoard.tileList.map { emptyTile ->
                    if (emptyTile.location == filledTile.location) {
                        Tile(emptyTile.location, piece)
                    } else {
                        Tile(emptyTile.location, Piece.Empty)
                    }
                }, Move.WHITE, Legality.Legal)
            }
        }

        val result = singlePieceBoards.reduce { acc, next ->
            acc.flatMap { combinedBoard ->
                next.flatMap { nextBoard ->
                    val updatedTileList = combinedBoard.tileList.zip(nextBoard.tileList) { oldTile, nextTile ->
                        if (oldTile.piece == Piece.Empty) {
                            oldTile.copy(piece = nextTile.piece)
                        } else {
                            oldTile
                        }
                    }
                    listOf(
                        Board(size, updatedTileList, Move.WHITE, Legality.Legal),
                        Board(size, updatedTileList, Move.BLACK, Legality.Legal)
                    )
                }
            }
        }

        return result.filter { board -> board.tileList.count { tile -> tile.piece != Piece.Empty } == pieces.size }
    }
}