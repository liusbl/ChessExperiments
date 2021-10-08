package generation

import generation.models.*
import generation.models.Piece.*
import java.io.File
import java.time.Instant
import java.util.*

fun main() {
    println("Starting. ${Instant.now()}")
    val emptyBoard = createEmptyBoard(8)

    println("Step #1: Create all single piece board list. ${Instant.now()}")
    val singlePieceBoardList = createAllSinglePieceBoardList(
        emptyBoard,
        listOf(
            King(Color.WHITE),
            King(Color.BLACK),
            Queen(Color.WHITE),
        )
    )

    println("Step #2: Combine all single piece board lists. ${Instant.now()}")
    val combinedPieceBoardList = combineSinglePieceBoardLists(singlePieceBoardList)

    println("Step #3: Append moves to board lists. ${Instant.now()}")
    val allCombinedPieceBoardListWithMoves = appendMovesToBoardList(combinedPieceBoardList)

    println("Step #4: FIX THIS. ${Instant.now()}")
    val boardListWithIllegalNextBoardList = createWithIllegalNextBoardList(allCombinedPieceBoardListWithMoves)

    println("Step #5. ${Instant.now()}")
    val final = filterOnlyLegalNextBoards(boardListWithIllegalNextBoardList)

    println("Step #6. ${Instant.now()}")
    val finalized = finalizeIndexes(final)

    println("Step #7. ${Instant.now()}")
    val fin = finalized.map(BoardFenMapper::getFen)

    File("out.txt").writeText(fin.joinToString(separator = "\n"))

    println("Finished")
}

// TODO USE map of index to tileList string, mapOf("414" to "k--Q\n----..")

fun finalizeIndexes(boardList: List<Board.Final>): List<Board.Final> {
    // TODO figure out why location x is wrong, for now just fix those
    var chunkSize = boardList.size / 5
    return boardList.mapIndexed { index, board ->
        if (index == chunkSize) {
            println("Finalizing indexes: ${chunkSize.toFloat() / boardList.size}")
            chunkSize += boardList.size / 5
        }
//        val tileList = thing.key
//        val (board, index) = thing.value
        when (board) {
            is Board.Final.Illegal -> {
                board
            }
            is Board.Final.Legal -> {
                val indexes = board.nextBoardList
                    .mapNotNull { nextBoard ->
//                        val value = boardList[nextBoard.tileList]
//                        value
                        boardList.find { listBoard ->
                            listBoard is Board.Final.Legal && listBoard.tileList == nextBoard.tileList
                        }
//                    }.map { nextBoard -> nextBoard.second }
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

fun createWithIllegalNextBoardList(boardList: List<Board.WithMove>): List<Board.Final> {
//fun createWithIllegalNextBoardList(boardList: List<Board.WithMove>): Map<List<generation.models.Tile>, Pair<Board.Final, Int>> {
    var chunkSize = boardList.size / 5
    return boardList.mapIndexed { index, board ->
        if (index == chunkSize) {
            println("Creating illegal next board lists: ${chunkSize.toFloat() / boardList.size}")
            chunkSize += boardList.size / 5
        }
        // Get possible moves
        return@mapIndexed createSingleBoardWithIllegalNextBoardList(index, board)
    }.also {
        chunkSize = boardList.size / 30
    }
        .mapIndexed { index, board ->
            if (index == chunkSize) {
                println("Fixing board generation.models.Location X: ${chunkSize.toFloat() / boardList.size}")
                chunkSize += boardList.size / 30
            }
            when (board) {
                is Board.Final.Illegal -> {
                    board
                }
                is Board.Final.Legal -> {
                    val nextBoardList = board.nextBoardList
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
                    board.copy(nextBoardList = nextBoardList)
                }
                else -> TODO()
            }
        }
//                .associate { board ->
//                    board.tileList to (board to board.index)
//                }
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

    // Create generation.models.CheckState // TODO only works for kQK
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

    // Create generation.models.CheckState // TODO only works for kQK
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