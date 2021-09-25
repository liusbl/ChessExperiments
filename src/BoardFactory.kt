import kotlin.math.hypot

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

                    //  Checking side has to move (TODO only checking for queen now)
                    val queens = updatedTileList.filter { tile -> tile.piece is Piece.Queen }
                    val isValid = queens.isEmpty() || kings.size < 2

                    val isInCheck = b(queens, kings, isValid, updatedTileList)

                    if (distance < 2 && isValid) {
                        listOf(
                            Board(size, updatedTileList, Move.WHITE, Legality.Illegal.KingsAdjacent, listOf()),
                            Board(size, updatedTileList, Move.BLACK, Legality.Illegal.KingsAdjacent, listOf())
                        )
                    } else if (isInCheck && isValid) {
                        if (distance < 2) {
                            val legalityList = Legality.Illegal.Combined(
                                listOf(Legality.Illegal.KingsAdjacent, Legality.Illegal.CheckButWrongMove)
                            )
                            listOf(
                                Board(size, updatedTileList, Move.WHITE, legalityList, listOf()),
                                Board(size, updatedTileList, Move.BLACK, legalityList, listOf())
                            )
                        } else {
                            listOf(
                                Board(size, updatedTileList, Move.WHITE, Legality.Illegal.CheckButWrongMove, listOf()),
                                Board(size, updatedTileList, Move.BLACK, Legality.Illegal.CheckButWrongMove, listOf())
                            )
                        }
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

    private fun b(
        queens: List<Tile>,
        kings: List<Tile>,
        isValid: Boolean,
        updatedTileList: List<Tile>
    ): Boolean {
        try {
            val queen = queens[0]
            val king = kings.first { queen.piece.color != it.piece.color }
            val isInCheck = isValid &&
                    if (queen.location.x == king.location.x) {
                        val rangeBetweenPieces = if (queen.location.y < king.location.y) {
                            queen.location.y + 1 until king.location.y
                        } else {
                            king.location.y + 1 until queen.location.y
                        }
                        val allEmpty = rangeBetweenPieces.map { y ->
                            updatedTileList.first { tile ->
                                tile.location.x == queen.location.x && tile.location.y == y
                            }
                        }.all { tile -> tile.piece is Piece.Empty }
                        allEmpty
                    } else if (queen.location.y == king.location.y) {
                        val rangeBetweenPieces = if (queen.location.x < king.location.x) {
                            queen.location.x + 1 until king.location.x
                        } else {
                            king.location.x + 1 until queen.location.x
                        }
                        val allEmpty = rangeBetweenPieces.map { x ->
                            updatedTileList.first { tile ->
                                tile.location.y == queen.location.y && tile.location.x == x
                            }
                        }.all { tile -> tile.piece is Piece.Empty }
                        allEmpty
                    } else if (queen.location.x - queen.location.y == king.location.x - king.location.y) {
                        val rangeBetweenPiecesX = if (queen.location.x < king.location.x) {
                            queen.location.x + 1 until king.location.x
                        } else {
                            king.location.x + 1 until queen.location.x
                        }

                        val rangeBetweenPiecesY = if (queen.location.y < king.location.y) {
                            queen.location.y + 1 until king.location.y
                        } else {
                            king.location.y + 1 until queen.location.y
                        }

                        rangeBetweenPiecesX.zip(rangeBetweenPiecesY).all { (x, y) ->
                            val first = updatedTileList.first { tile ->
                                tile.location.x == x && tile.location.y == y
                            }
                            first.piece is Piece.Empty
                        }
                    } else {
                        false
                    }
            return isInCheck
        } catch (e: Exception) {
            return false
        }
    }
}