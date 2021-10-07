import Piece.*
import java.io.File

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

    val singlePieceBoardList = createAllSinglePieceBoardList(
        emptyBoard,
        listOf(
            King(Color.WHITE),
            King(Color.BLACK),
            Queen(Color.WHITE),
        )
    )

    val allCombinedPieceBoardList = combineAllSinglePieceBoardList(singlePieceBoardList)

    val allCombinedPieceBoardListWithMoves = setMoves(allCombinedPieceBoardList)

    val boardListWithIllegalNextBoardList = createWithIllegalNextBoardList(allCombinedPieceBoardListWithMoves)

    val final = filterOnlyLegalNextBoards(boardListWithIllegalNextBoardList)

    val finalized = finalizeIndexes(final)

    val fin = finalized.map(BoardFenMapper::getFen)

    File("out.txt").writeText(fin.joinToString(separator = "\n"))

    println("Finished")
}

fun finalizeIndexes(boardList: List<Board.Final>): List<Board.Final> {
    // TODO figure out why location x is wrong, for now just fix those

    return boardList.map { board ->
        when (board) {
            is Board.Final.Illegal -> {
                board
            }
            is Board.Final.Legal -> {
                val indexes = board.nextBoardList
                    .map { nextBoard ->
                        // TODO THIS IS HACK FIX FOR WRONG LOCATION X
                        val emptyBoard = createEmptyBoard(board.size)
                        val tileList = nextBoard.partialBoard.tileList.toMutableList()
                            .zip(emptyBoard.tileList) { tile, empty ->
                                Tile(empty.location, tile.piece)
                            }
                        val partialBoard = nextBoard.partialBoard.copy(tileList = tileList)
                        nextBoard.copy(partialBoard = partialBoard)
                    }
                    .mapNotNull { nextBoard ->
                        boardList.find { listBoard ->
                            listBoard is Board.Final.Legal && listBoard.tileList == nextBoard.tileList
                        }
                    }.map { nextBoard -> nextBoard.index }
                Board.Final.LegalFinalV2(board.index, board.withMoveBoard, board.checkState, indexes)
            }
            else -> {
                throw Exception("no")
            }
        }
    }
}

fun filterOnlyLegalNextBoards(boardListWithIllegalNextBoardList: List<Board.Final>): List<Board.Final> {
    return boardListWithIllegalNextBoardList.map { board ->
        if (board !is Board.Final.Legal) return@map board

        val onlyLegalNextBoardList = board.nextBoardList.mapIndexed { index, bored ->
            getBoardWithLegalityAndCheckState(index, bored)
        }.filterIsInstance<Board.Final.Legal>()
            .map { bored ->
                Board.WithMove(
                    partialBoard = Board.Partial(bored.size, bored.tileList),
                    move = bored.withMoveBoard.move
                )
            }
        board.copy(nextBoardList = onlyLegalNextBoardList)
    }
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
    return boardList.mapIndexed { index, board ->
        // Get possible moves
        return@mapIndexed createSingleBoardWithIllegalNextBoardList(index, board)
    }
}

private fun createSingleBoardWithIllegalNextBoardList(index: Int, board: Board.WithMove): Board.Final {
    val whiteKing = board.tileList.first { tile -> tile.piece is King && tile.piece.color == Color.WHITE }
    val possibleWhiteKingTiles = possibleKingTiles(whiteKing, board)

    val blackKing = board.tileList.first { tile -> tile.piece is King && tile.piece.color == Color.BLACK }
    val possibleBlackKingTiles = possibleKingTiles(blackKing, board)

    // Try to test for legality
    //  Kings near each other:
    val twoKingsNearEachOther = possibleWhiteKingTiles.any { tile -> tile.piece is King }
            || possibleBlackKingTiles.any { tile -> tile.piece is King }
    if (twoKingsNearEachOther) {
        return Board.Final.Illegal(index, board, Legality.Illegal.KingsAdjacent)
    }

    val queen = board.tileList.find { tile -> tile.piece is Queen } ?: return Board.Final.Legal(
        index,
        board,
        CheckState.NONE,
        emptyList()
    )
    val possibleQueenTiles = getPossibleQueenMoves(queen, board)

    // King in check making needs to make a move:
    //  First need to check for checkmates
    val inCheck = possibleQueenTiles.any { tile -> tile.piece is King }
    val checkButWrongMove = inCheck && board.move == Move.WHITE // TODO only works for kQK
    if (checkButWrongMove) {
        return Board.Final.Illegal(index, board, Legality.Illegal.CheckButWrongMove)
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
        val nextBlackKingMoves = possibleBlackKingTiles.filter { tile ->
            !possibleQueenTiles.contains(tile) && !possibleWhiteKingTiles.contains(tile)
        }
        if (board.move == Move.BLACK && !possibleQueenTiles.contains(blackKing) && nextBlackKingMoves.isEmpty()) {
            CheckState.STALEMATE
        } else {
            CheckState.NONE
        }
    }

    val legalBoard = Board.Final.Legal(index, board, checkState, emptyList())

    return getNextPossibleBoards(
        legalBoard,
        possibleBlackKingTiles,
        blackKing,
        possibleQueenTiles,
        queen,
        possibleWhiteKingTiles,
        whiteKing
    )
}

fun getBoardWithLegalityAndCheckState(index: Int, board: Board.WithMove): Board.Final {
    val whiteKing = board.tileList.first { tile -> tile.piece is King && tile.piece.color == Color.WHITE }
    val possibleWhiteKingTiles = possibleKingTiles(whiteKing, board)

    val blackKing = board.tileList.first { tile -> tile.piece is King && tile.piece.color == Color.BLACK }
    val possibleBlackKingTiles = possibleKingTiles(blackKing, board)

    // Try to test for legality
    //  Kings near each other:
    val twoKingsNearEachOther = possibleWhiteKingTiles.any { tile -> tile.piece is King }
            || possibleBlackKingTiles.any { tile -> tile.piece is King }
    if (twoKingsNearEachOther) {
        return Board.Final.Illegal(index, board, Legality.Illegal.KingsAdjacent)
    }

    val queen = board.tileList.find { tile -> tile.piece is Queen } ?: return Board.Final.Legal(
        index,
        board,
        CheckState.NONE,
        emptyList()
    )
    val possibleQueenTiles = getPossibleQueenMoves(queen, board)

    // King in check making needs to make a move:
    //  First need to check for checkmates
    val inCheck = possibleQueenTiles.any { tile -> tile.piece is King }
    val checkButWrongMove = inCheck && board.move == Move.WHITE // TODO only works for kQK
    if (checkButWrongMove) {
        return Board.Final.Illegal(index, board, Legality.Illegal.CheckButWrongMove)
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

    return Board.Final.Legal(index, board, checkState, emptyList())
}

private fun getNextPossibleBoards(
    legalBoard: Board.Final.Legal,
    possibleBlackKingTiles: List<Tile>,
    blackKing: Tile,
    possibleQueenTiles: List<Tile>,
    queen: Tile,
    possibleWhiteKingTiles: List<Tile>,
    whiteKing: Tile
): Board.Final.Legal {
    return if (legalBoard.withMoveBoard.move == Move.BLACK) {
        // Create nextBoardList
        //  White King's nextBoardList:
        val nextBlackKingBoardList = possibleBlackKingTiles
            .map { tile ->
                val tileList = legalBoard.tileList
                val replacingTileIndex = tileList.indexOfFirst { tile.location == it.location }
                val blackKingTileIndex = tileList.indexOf(blackKing)
                val newTileList = tileList.toMutableList()
                    .apply {
                        set(
                            replacingTileIndex,
                            tile.copy(location = tileList[replacingTileIndex].location, piece = blackKing.piece)
                        )
                        set(
                            blackKingTileIndex,
                            tile.copy(location = tileList[blackKingTileIndex].location, piece = Empty)
                        )
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