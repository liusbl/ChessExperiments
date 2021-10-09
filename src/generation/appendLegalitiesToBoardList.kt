package generation

import generation.models.*
import generation.models.Piece.*

fun appendCheckStateToBoardList(boardList: List<Board.WithMove>): List<Board.WithCheckState> {
    return boardList.mapIndexed { index, board ->
        val whiteKing = board.tileList.first { tile -> tile.piece == King(Color.WHITE) }
        val nextWhiteKingTiles = getNextKingTiles(whiteKing, board)

        val blackKing = board.tileList.first { tile -> tile.piece == King(Color.BLACK) }
        val nextBlackKingTiles = getNextKingTiles(blackKing, board)

        // Try to test for legality
        //  Kings near each other:
        val twoKingsNearEachOther = nextWhiteKingTiles.any { tile -> tile.piece is King }
                || nextBlackKingTiles.any { tile -> tile.piece is King }
        if (twoKingsNearEachOther) {
            return@mapIndexed Board.WithCheckState(
                board.size,
                board.tileList,
                board.move,
                index,
                Board.WithCheckState.LegalityWithCheckState.Illegal(Legality.Illegal.KingsAdjacent)
            )
        }

        val queen = board.tileList.find { tile -> tile.piece is Queen } ?: return@mapIndexed Board.WithCheckState(
            board.size,
            board.tileList,
            board.move,
            index,
            Board.WithCheckState.LegalityWithCheckState.Legal(checkState = CheckState.DRAW),
        )
        val nextQueenTiles = getNextQueenTiles(queen, board)

        // King in check making needs to make a move:
        //  First need to check for checkmates
        val inCheck = nextQueenTiles.any { tile -> tile.piece is King }
        val checkButWrongMove = inCheck && board.move == Move.WHITE // TODO only works for kQK
        if (checkButWrongMove) {
            return@mapIndexed Board.WithCheckState(
                board.size,
                board.tileList,
                board.move,
                index,
                Board.WithCheckState.LegalityWithCheckState.Illegal(Legality.Illegal.CheckButWrongMove),
            )
        }

        // Create generation.models.CheckState // TODO only works for kQK
        val checkState = if (inCheck) {
            val whiteKingQueenDistance = whiteKing.location.getDistance(queen.location)
            val nextBlackKingMoves = nextBlackKingTiles.filter { tile ->
                (!nextQueenTiles.contains(tile) && !nextWhiteKingTiles.contains(tile))
                        && !(tile.location == queen.location && whiteKingQueenDistance < 1.5)
                // Top conditions black king cannot walk on defended tiles.
                // Bottom conditions means king is protecting queen.
            }
            if (nextBlackKingMoves.isEmpty()) {
                CheckState.BLACK_IN_CHECKMATE
            } else {
                CheckState.BLACK_IN_CHECK
            }
        } else {
            CheckState.NONE
        }

        Board.WithCheckState(
            board.size,
            board.tileList,
            board.move,
            index,
            Board.WithCheckState.LegalityWithCheckState.Legal(checkState = checkState),
        )
    }
}

// TODO sealed class nesting could possibly solve this
fun getNextKingTiles(king: Tile, board: Board.WithCheckState): List<Tile> {
    return getNextKingTiles(king, Board.WithMove(board.size, board.tileList, board.move))
}

private fun getNextKingTiles(king: Tile, board: Board.WithMove, withWhiteQueen: Boolean = false): List<Tile> {
    val leftMove = board.tileAt(king.location.x - 1, king.location.y)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }
    val rightMove = board.tileAt(king.location.x + 1, king.location.y)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }
    val topMove = board.tileAt(king.location.x, king.location.y - 1)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }
    val bottomMove = board.tileAt(king.location.x, king.location.y + 1)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }

    val topLeftMove = board.tileAt(king.location.x - 1, king.location.y - 1)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }
    val topRightMove = board.tileAt(king.location.x + 1, king.location.y - 1)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }
    val bottomLeftMove = board.tileAt(king.location.x - 1, king.location.y + 1)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }
    val bottomRightMove = board.tileAt(king.location.x + 1, king.location.y + 1)
        ?.takeIf { tile ->
            tile.piece is Empty || tile.piece.color != king.piece.color ||
                    if (withWhiteQueen && king.piece.color == Color.WHITE) {
                        tile.piece is Queen
                    } else false
        }

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
fun getNextQueenTiles(queen: Tile, board: Board.WithCheckState): List<Tile> {
    return getNextQueenTiles(queen, Board.WithMove(board.size, board.tileList, board.move))
}

private fun getNextQueenTiles(queen: Tile, board: Board.WithMove): List<Tile> {
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
