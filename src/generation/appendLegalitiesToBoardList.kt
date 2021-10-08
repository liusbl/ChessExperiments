package generation

import generation.models.*
import generation.models.Piece.*

fun appendCheckStateToBoardList(boardList: List<Boo.WithMove>): List<Boo.WithCheckState> {
    return boardList.mapIndexedNotNull { index, board ->
        val whiteKing = board.tileList.find { tile -> tile.piece == King(Color.WHITE) }
            ?: return@mapIndexedNotNull null
        val nextWhiteKingTiles = getNextKingTiles(whiteKing, board)

        val blackKing = board.tileList.find { tile -> tile.piece == King(Color.BLACK) }
            ?: return@mapIndexedNotNull null
        val nextBlackKingTiles = getNextKingTiles(blackKing, board)

        // Try to test for legality
        //  Kings near each other:
        val twoKingsNearEachOther = nextWhiteKingTiles.any { tile -> tile.piece is King }
                || nextBlackKingTiles.any { tile -> tile.piece is King }
        if (twoKingsNearEachOther) {
            return@mapIndexedNotNull Boo.WithCheckState(
                board.size,
                board.tileList,
                board.move,
                index,
                Boo.WithCheckState.LegalityWithCheckState.Illegal(Legality.Illegal.KingsAdjacent)
            )
        }

        val queen = board.tileList.find { tile -> tile.piece is Queen } ?: return@mapIndexedNotNull Boo.WithCheckState(
            board.size,
            board.tileList,
            board.move,
            index,
            Boo.WithCheckState.LegalityWithCheckState.Legal(checkState = CheckState.DRAW),
        )
        val nextQueenTiles = getNextQueenTiles(queen, board)

        // King in check making needs to make a move:
        //  First need to check for checkmates
        val inCheck = nextQueenTiles.any { tile -> tile.piece is King }
        val checkButWrongMove = inCheck && board.move == Move.WHITE // TODO only works for kQK
        if (checkButWrongMove) {
            return@mapIndexedNotNull Boo.WithCheckState(
                board.size,
                board.tileList,
                board.move,
                index,
                Boo.WithCheckState.LegalityWithCheckState.Illegal(Legality.Illegal.CheckButWrongMove),
            )
        }

        // Create generation.models.CheckState // TODO only works for kQK
        val checkState = if (inCheck) {
            val nextBlackKingMoves = nextBlackKingTiles.filter { tile ->
                !nextQueenTiles.contains(tile) && !nextWhiteKingTiles.contains(tile)
            }
            if (nextBlackKingMoves.isEmpty()) {
                CheckState.BLACK_IN_CHECKMATE
            } else {
                CheckState.BLACK_IN_CHECK
            }
        } else {
            CheckState.NONE
        }

        return@mapIndexedNotNull Boo.WithCheckState(
            board.size,
            board.tileList,
            board.move,
            index,
            Boo.WithCheckState.LegalityWithCheckState.Legal(checkState = checkState),
        )
    }
}

// TODO sealed class nesting could possibly solve this
fun getNextKingTiles(king: Tile, board: Boo.WithCheckState): List<Tile> {
    return getNextKingTiles(king, Boo.WithMove(board.size, board.tileList, board.move))
}

private fun getNextKingTiles(king: Tile, board: Boo.WithMove): List<Tile> {
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

// TODO sealed class nesting could possibly solve this
fun getNextQueenTiles(queen: Tile, board: Boo.WithCheckState): List<Tile> {
    return getNextQueenTiles(queen, Boo.WithMove(board.size, board.tileList, board.move))
}

private fun getNextQueenTiles(queen: Tile, board: Boo.WithMove): List<Tile> {
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
