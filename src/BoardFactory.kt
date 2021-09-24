import kotlin.math.hypot
import kotlin.math.sqrt

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
                }, Move.WHITE, Legality.Legal, listOf())
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

                    // Check legality
                    //  Two kings:
                    val kings = updatedTileList.filter { tile -> tile.piece is Piece.King }

                    val distance = if (kings.size < 2) 3.toDouble() else hypot(
                        kings[1].location.x - kings[0].location.x.toDouble(),
                        kings[1].location.y - kings[0].location.y.toDouble()
                    )

                    if (distance < 2) {
                        listOf(
                            Board(size, updatedTileList, Move.WHITE, Legality.Illegal.KingsAdjacent, listOf()),
                            Board(size, updatedTileList, Move.BLACK, Legality.Illegal.KingsAdjacent, listOf())
                        )
                    } else {
                        val board1 = Board(size, updatedTileList, Move.WHITE, Legality.Legal, listOf())
                        val a = board1.tileList.flatMap { tile ->
                            tile.piece.movement.getNextBoardList(tile, board1)
                        }

                        val board2 = Board(size, updatedTileList, Move.BLACK, Legality.Legal, listOf())
                        val b = board2.tileList.flatMap { tile ->
                            tile.piece.movement.getNextBoardList(tile, board2)
                        }

                        listOf(
                            board1.copy(nextBoardList = a),
                            board2.copy(nextBoardList = b)
                        )
                    }
                }
            }
        }

        return result.filter { board -> board.tileList.count { tile -> tile.piece != Piece.Empty } == pieces.size }
    }
}