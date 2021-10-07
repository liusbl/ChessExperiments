import Piece.*

/**
 * Process of generation:
 *  1. Create empty board
 *  2. Create all single piece boards
 *  3. Combine all single piece boards
 *  4. Filter out illegal boards
 *  5.
 */

fun main() {
    println("Starting")
    val emptyBoard = createEmptyBoard(4)
    println(emptyBoard.getPrintableBoard())

    val singlePieceBoardList = createAllSinglePieceBoardList(
        emptyBoard,
        listOf(
            King(Color.WHITE),
            King(Color.BLACK),
            Queen(Color.WHITE),
        )
    )
    println(singlePieceBoardList)

    val allCombinedPieceBoardList = combineAllSinglePieceBoardList(singlePieceBoardList)

    val allCombinedPieceBoardListWithMoves = setMoves(allCombinedPieceBoardList)

    val boardListWithIllegalNextBoardList = createWithIllegalNextBoardList(allCombinedPieceBoardListWithMoves)

    println("Finished")
}

fun createEmptyBoard(size: Int): Board.Partial {
    val tileList = (0 until size).map { tileY ->
        (0 until size).map { tileX ->
            Tile(Location(tileX, tileY), Empty)
        }
    }.flatten()
    return Board.Partial(size, tileList)
}

fun createAllSinglePieceBoardList(emptyBoard: Board.Partial, pieceList: List<Piece>): List<List<Board.Partial>> {
    return pieceList.map { piece ->
        emptyBoard.tileList.map { filledTile ->
            Board.Partial(emptyBoard.size, emptyBoard.tileList.map { emptyTile ->
                if (emptyTile.location == filledTile.location) {
                    Tile(emptyTile.location, piece)
                } else {
                    Tile(emptyTile.location, Empty)
                }
            })
        }
    }
}

fun combineAllSinglePieceBoardList(singlePieceBoardList: List<List<Board.Partial>>): List<Board.Partial> {
    return singlePieceBoardList.reduce { acc: List<Board.Partial>, next: List<Board.Partial> ->
        acc.map { combinedBoard ->
            next.map { nextBoard ->
                val updatedTileList = combinedBoard.tileList.zip(nextBoard.tileList) { oldTile, nextTile ->
                    if (oldTile.piece == Empty) {
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
            tile.piece !is Empty
        } == singlePieceBoardList.size
    }
}

fun setMoves(boardList: List<Board.Partial>): List<Board.WithMove> {
    return boardList.flatMap { board ->
        listOf(
            Board.WithMove(board, Move.WHITE),
            Board.WithMove(board, Move.BLACK),
        )
    }
}

fun createWithIllegalNextBoardList(boardList: List<Board.WithMove>): List<Board.Final> {
    return boardList.map { board ->
        // Get possible moves
        val queen = board.tileList.first { tile -> tile.piece is Queen }
        val possibleQueenTiles = getPossibleQueenMoves(queen, board)

        val whiteKing = board.tileList.first { tile -> tile.piece is King && tile.piece.color == Color.WHITE }
        val possibleWhiteKingTiles = possibleKingTiles(whiteKing, board)

        val blackKing = board.tileList.first { tile -> tile.piece is King && tile.piece.color == Color.BLACK }
        val possibleBlackKingTiles = possibleKingTiles(blackKing, board)

        // Try to test for legality
        //  Kings near each other:
        val twoKingsNearEachOther = possibleWhiteKingTiles.any { tile -> tile.piece is King }
                || possibleBlackKingTiles.any { tile -> tile.piece is King }
        if (twoKingsNearEachOther) {
            return@map Board.Final.Illegal(board, Legality.Illegal.KingsAdjacent)
        }

        // King in check making needs to make a move:
        //  First need to check for checkmates
        val inCheck = possibleQueenTiles.any { tile -> tile.piece is King }
        val checkButWrongMove = inCheck && board.move == Move.WHITE // TODO only works for kQK
        if (checkButWrongMove) {
            return@map Board.Final.Illegal(board, Legality.Illegal.CheckButWrongMove)
        }

        // Create CheckState // TODO only works for kQK
        val checkState = if (inCheck) {
            val nextBlackKingMoves = possibleBlackKingTiles.filter { tile ->
                !possibleQueenTiles.contains(tile) && !possibleWhiteKingTiles.contains(tile)
            }
            if (nextBlackKingMoves.isEmpty()) {
                CheckState.BLACK_IN_CHECKMATE
            } else {
                CheckState.BLACK_IN_CHECK
            }
        } else {
            CheckState.NONE
        }

        val legalBoard = Board.Final.Legal(board, checkState, emptyList())

        return@map getNextPossibleBoards(
            legalBoard,
            possibleBlackKingTiles,
            blackKing,
            possibleQueenTiles,
            queen,
            possibleWhiteKingTiles,
            whiteKing
        )
    }
}

private fun getNextPossibleBoards(
    legalBoard: Board.Final.Legal,
    possibleBlackKingTiles: List<Tile>,
    blackKing: Tile,
    possibleQueenTiles: List<Tile>,
    queen: Tile,
    possibleWhiteKingTiles: List<Tile>,
    whiteKing: Tile
) = if (legalBoard.withMoveBoard.move == Move.BLACK) {
    // Create nextBoardList
    //  White King's nextBoardList:
    val nextBlackKingBoardList = possibleBlackKingTiles
        .map { tile ->
            val tileList = legalBoard.tileList
            val replacingTileIndex = tileList.indexOfFirst { tile.location == it.location }
            val blackKingTileIndex = tileList.indexOf(blackKing)
            val newTileList = tileList.toMutableList()
                .apply {
                    set(replacingTileIndex, tile.copy(piece = blackKing.piece))
                    set(blackKingTileIndex, tile.copy(piece = Empty))
                }
                .toList()
            newTileList
        }
        .filter { newTileList -> newTileList.count { tile -> tile.piece is King } == 2 }
        .map { newTileList ->
            Board.WithMove(Board.Partial(legalBoard.size, newTileList), move = Move.WHITE)
        }

    legalBoard.copy(nextBoardList = nextBlackKingBoardList)
} else {
    // Create nextBoardList
    //  Queen's nextBoardList:
    val nextQueenBoardList = possibleQueenTiles
        .map { tile ->
            val tileList = legalBoard.tileList
            val replacingTileIndex = tileList.indexOfFirst { tile.location == it.location }
            val queenTileIndex = tileList.indexOf(queen)
            val newTileList = tileList.toMutableList()
                .apply {
                    set(replacingTileIndex, tile.copy(piece = queen.piece))
                    set(queenTileIndex, tile.copy(piece = Empty))
                }
                .toList()
            newTileList
        }
        .filter { newTileList -> newTileList.count { tile -> tile.piece is King } == 2 }
        .map { newTileList ->
            Board.WithMove(Board.Partial(legalBoard.size, newTileList), move = Move.BLACK)
        }

    // Create nextBoardList
    //  White King's nextBoardList:
    val nextWhiteKingBoardList = possibleWhiteKingTiles
        .map { tile ->
            val tileList = legalBoard.tileList
            val replacingTileIndex = tileList.indexOfFirst { tile.location == it.location }
            val whiteKingTileIndex = tileList.indexOf(whiteKing)
            val newTileList = tileList.toMutableList()
                .apply {
                    set(replacingTileIndex, tile.copy(piece = whiteKing.piece))
                    set(whiteKingTileIndex, tile.copy(piece = Empty))
                }
                .toList()
            newTileList
        }
        .filter { newTileList -> newTileList.count { tile -> tile.piece is King } == 2 }
        .map { newTileList ->
            Board.WithMove(Board.Partial(legalBoard.size, newTileList), move = Move.BLACK)
        }

    legalBoard.copy(nextBoardList = nextQueenBoardList + nextWhiteKingBoardList)
}

private fun possibleKingTiles(king: Tile, board: Board.WithMove): List<Tile> {
    val leftMove = board.tileAt(king.location.x - 1, king.location.y)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }
    val rightMove = board.tileAt(king.location.x + 1, king.location.y)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }
    val topMove = board.tileAt(king.location.x, king.location.y - 1)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }
    val bottomMove = board.tileAt(king.location.x, king.location.y + 1)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }

    val topLeftMove = board.tileAt(king.location.x - 1, king.location.y - 1)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }
    val topRightMove = board.tileAt(king.location.x + 1, king.location.y - 1)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }
    val bottomLeftMove = board.tileAt(king.location.x - 1, king.location.y + 1)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }
    val bottomRightMove = board.tileAt(king.location.x + 1, king.location.y + 1)
        ?.takeIf { tile -> tile.piece is Empty || tile.piece.color != king.piece.color }

    return listOfNotNull(
        leftMove,
        rightMove,
        topMove,
        bottomMove,
        topLeftMove,
        topRightMove,
        bottomLeftMove,
        bottomRightMove
    )
}

private fun getPossibleQueenMoves(queen: Tile, board: Board.WithMove): List<Tile> {
    val leftMoves = (queen.location.x - 1 downTo 0)
        .takeWhile { x -> board.tileAt(x, queen.location.y)?.piece is Empty }
        .mapNotNull { x -> board.tileAt(x, queen.location.y) }
        .let { tileList ->
            val x = tileList.lastOrNull()?.location?.x ?: queen.location.x
            val nextTile = board.tileAt(x - 1, queen.location.y)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    val rightMoves = (queen.location.x + 1 until board.size)
        .takeWhile { x -> board.tileAt(x, queen.location.y)?.piece is Empty }
        .mapNotNull { x -> board.tileAt(x, queen.location.y) }
        .let { tileList ->
            val x = tileList.lastOrNull()?.location?.x ?: queen.location.x
            val nextTile = board.tileAt(x + 1, queen.location.y)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    val topMoves = (queen.location.y - 1 downTo 0)
        .takeWhile { y -> board.tileAt(queen.location.x, y)?.piece is Empty }
        .mapNotNull { y -> board.tileAt(queen.location.x, y) }
        .let { tileList ->
            val y = tileList.lastOrNull()?.location?.y ?: queen.location.y
            val nextTile = board.tileAt(queen.location.x, y - 1)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    val bottomMoves = (queen.location.y + 1 until board.size)
        .takeWhile { y -> board.tileAt(queen.location.x, y)?.piece is Empty }
        .mapNotNull { y -> board.tileAt(queen.location.x, y) }
        .let { tileList ->
            val y = tileList.lastOrNull()?.location?.y ?: queen.location.y
            val nextTile = board.tileAt(queen.location.x, y + 1)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    val topLeftMoves = (queen.location.x - 1 downTo 0).zip(queen.location.y - 1 downTo 0)
        .takeWhile { (x, y) -> board.tileAt(x, y)?.piece is Empty }
        .mapNotNull { (x, y) -> board.tileAt(x, y) }
        .let { tileList ->
            val x = tileList.lastOrNull()?.location?.x ?: queen.location.x
            val y = tileList.lastOrNull()?.location?.y ?: queen.location.y
            val nextTile = board.tileAt(x - 1, y - 1)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    val topRightMoves = (queen.location.x + 1 until board.size).zip(queen.location.y - 1 downTo 0)
        .takeWhile { (x, y) -> board.tileAt(x, y)?.piece is Empty }
        .mapNotNull { (x, y) -> board.tileAt(x, y) }
        .let { tileList ->
            val x = tileList.lastOrNull()?.location?.x ?: queen.location.x
            val y = tileList.lastOrNull()?.location?.y ?: queen.location.y
            val nextTile = board.tileAt(x + 1, y - 1)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    val bottomLeftMoves = (queen.location.x - 1 downTo 0).zip(queen.location.y + 1 until board.size)
        .takeWhile { (x, y) -> board.tileAt(x, y)?.piece is Empty }
        .mapNotNull { (x, y) -> board.tileAt(x, y) }
        .let { tileList ->
            val x = tileList.lastOrNull()?.location?.x ?: queen.location.x
            val y = tileList.lastOrNull()?.location?.y ?: queen.location.y
            val nextTile = board.tileAt(x - 1, y + 1)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    val bottomRightMoves = (queen.location.x + 1 until board.size).zip(queen.location.y + 1 until board.size)
        .takeWhile { (x, y) -> board.tileAt(x, y)?.piece is Empty }
        .mapNotNull { (x, y) -> board.tileAt(x, y) }
        .let { tileList ->
            val x = tileList.lastOrNull()?.location?.x ?: queen.location.x
            val y = tileList.lastOrNull()?.location?.y ?: queen.location.y
            val nextTile = board.tileAt(x + 1, y + 1)
            if (nextTile != null && nextTile.piece.color != queen.piece.color) {
                tileList + nextTile
            } else {
                tileList
            }
        }

    return leftMoves + rightMoves + topMoves + bottomMoves +
            topLeftMoves + topRightMoves + bottomLeftMoves + bottomRightMoves
}

//object BoardFactory {
//    fun createAllPieceBoards(size: Int, pieces: List<Piece>): List<Board> {
//        val emptyBoard = Board.createEmpty(size)
//
//        val singlePieceBoards = pieces.map { piece ->
//            emptyBoard.tileList.map { filledTile ->
//                Board(size, emptyBoard.tileList.map { emptyTile ->
//                    if (emptyTile.location == filledTile.location) {
//                        Tile(emptyTile.location, piece)
//                    } else {
//                        Tile(emptyTile.location, Piece.Empty)
//                    }
//                }, Move.WHITE, Legality.Legal, listOf())
//            }
//        }
//
//        val result = singlePieceBoards.reduce { acc, next ->
//            acc.flatMap { combinedBoard ->
//                next.flatMap { nextBoard ->
//                    val updatedTileList = combinedBoard.tileList.zip(nextBoard.tileList) { oldTile, nextTile ->
//                        if (oldTile.piece == Piece.Empty) {
//                            oldTile.copy(piece = nextTile.piece)
//                        } else {
//                            oldTile
//                        }
//                    }
//
//                    // Check legality
//                    //  Two kings:
//                    val kings = updatedTileList.filter { tile -> tile.piece is Piece.King }
//
//                    val distance = if (kings.size < 2) 3.toDouble() else hypot(
//                        kings[1].location.x - kings[0].location.x.toDouble(),
//                        kings[1].location.y - kings[0].location.y.toDouble()
//                    )
//
//                    //  Checking side has to move (TODO only checking for queen now)
//                    val queens = updatedTileList.filter { tile -> tile.piece is Piece.Queen }
//                    val isValid = queens.isEmpty() || kings.size < 2
//
//                    val inCheck = b(queens, kings, isValid, updatedTileList)
//
//                    if (distance < 2 && isValid) {
//                        listOf(
//                            Board(size, updatedTileList, Move.WHITE, Legality.Illegal.KingsAdjacent, listOf()),
//                            Board(size, updatedTileList, Move.BLACK, Legality.Illegal.KingsAdjacent, listOf())
//                        )
//                    } else if (isInCheck && isValid) {
//                        if (distance < 2) {
//                            val legalityList = Legality.Illegal.Combined(
//                                listOf(Legality.Illegal.KingsAdjacent, Legality.Illegal.CheckButWrongMove)
//                            )
//                            listOf(
//                                Board(size, updatedTileList, Move.WHITE, legalityList, listOf()),
//                                Board(size, updatedTileList, Move.BLACK, legalityList, listOf())
//                            )
//                        } else {
//                            listOf(
//                                Board(size, updatedTileList, Move.WHITE, Legality.Illegal.CheckButWrongMove, listOf()),
//                                Board(size, updatedTileList, Move.BLACK, Legality.Illegal.CheckButWrongMove, listOf())
//                            )
//                        }
//                    } else {
//                        val board1 = Board(size, updatedTileList, Move.WHITE, Legality.Legal, listOf())
//                        val a = board1.tileList.flatMap { tile ->
//                            tile.piece.movement.getNextBoardList(tile, board1)
//                        }
//
//                        val board2 = Board(size, updatedTileList, Move.BLACK, Legality.Legal, listOf())
//                        val b = board2.tileList.flatMap { tile ->
//                            tile.piece.movement.getNextBoardList(tile, board2)
//                        }
//
//                        listOf(
//                            board1.copy(nextBoardList = a),
//                            board2.copy(nextBoardList = b)
//                        )
//                    }
//                }
//            }
//        }
//
//        return result.filter { board -> board.tileList.count { tile -> tile.piece != Piece.Empty } == pieces.size }
//    }
//
//    private fun b(
//        queens: List<Tile>,
//        kings: List<Tile>,
//        isValid: Boolean,
//        updatedTileList: List<Tile>
//    ): Boolean {
//        try {
//            val queen = queens[0]
//            val king = kings.first { queen.piece.color != it.piece.color }
//            val isInCheck = isValid &&
//                    if (queen.location.x == king.location.x) {
//                        val rangeBetweenPieces = if (queen.location.y < king.location.y) {
//                            queen.location.y + 1 until king.location.y
//                        } else {
//                            king.location.y + 1 until queen.location.y
//                        }
//                        val allEmpty = rangeBetweenPieces.map { y ->
//                            updatedTileList.first { tile ->
//                                tile.location.x == queen.location.x && tile.location.y == y
//                            }
//                        }.all { tile -> tile.piece is Piece.Empty }
//                        allEmpty
//                    } else if (queen.location.y == king.location.y) {
//                        val rangeBetweenPieces = if (queen.location.x < king.location.x) {
//                            queen.location.x + 1 until king.location.x
//                        } else {
//                            king.location.x + 1 until queen.location.x
//                        }
//                        val allEmpty = rangeBetweenPieces.map { x ->
//                            updatedTileList.first { tile ->
//                                tile.location.y == queen.location.y && tile.location.x == x
//                            }
//                        }.all { tile -> tile.piece is Piece.Empty }
//                        allEmpty
//                    } else if (queen.location.x - queen.location.y == king.location.x - king.location.y) {
//                        val rangeBetweenPiecesX = if (queen.location.x < king.location.x) {
//                            queen.location.x + 1 until king.location.x
//                        } else {
//                            king.location.x + 1 until queen.location.x
//                        }
//
//                        val rangeBetweenPiecesY = if (queen.location.y < king.location.y) {
//                            queen.location.y + 1 until king.location.y
//                        } else {
//                            king.location.y + 1 until queen.location.y
//                        }
//
//                        rangeBetweenPiecesX.zip(rangeBetweenPiecesY).all { (x, y) ->
//                            val first = updatedTileList.first { tile ->
//                                tile.location.x == x && tile.location.y == y
//                            }
//                            first.piece is Piece.Empty
//                        }
//                    } else {
//                        false
//                    }
//            return isInCheck
//        } catch (e: Exception) {
//            return false
//        }
//    }
//}