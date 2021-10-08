package generation

import generation.models.Boo
import generation.models.CheckState
import generation.models.Move
import generation.models.Piece.*
import generation.models.setPiece

fun appendNextBoardLists(boardList: List<Boo.WithCheckState>): List<Boo.WithNextBoardList> {
    return boardList.map { board ->
        when (board.legalityWithCheckState) {
            is Boo.WithCheckState.LegalityWithCheckState.Illegal -> {
                Boo.WithNextBoardList(
                    board.size,
                    board.tileList,
                    board.move,
                    board.index,
                    Boo.WithNextBoardList.LegalityWithCheckState.Illegal(board.legalityWithCheckState.legality)
                )
            }
            is Boo.WithCheckState.LegalityWithCheckState.Legal -> {
                when (board.legalityWithCheckState.checkState) {
                    CheckState.DRAW,
                    CheckState.STALEMATE,
                    CheckState.BLACK_IN_CHECKMATE,
                    CheckState.WHITE_IN_CHECKMATE -> {
                        Boo.WithNextBoardList(
                            board.size,
                            board.tileList,
                            board.move,
                            board.index,
                            Boo.WithNextBoardList.LegalityWithCheckState.Legal(
                                checkState = board.legalityWithCheckState.checkState,
                                nextBoardList = emptyList()
                            )
                        )
                    }
                    else -> {
                        val nextBoardList = when (board.move) {
                            Move.WHITE -> {
                                val whiteKing = board.tileList.first { tile -> tile.piece == King(Color.WHITE) }
                                val nextWhiteKingTiles = getNextKingTiles(whiteKing, board)

                                val queen = board.tileList.first { tile -> tile.piece == Queen(Color.WHITE) }
                                val nextQueenTiles = getNextQueenTiles(queen, board)

                                nextWhiteKingTiles.map { tile -> tile.location }
                                    .map { nextKingLocation ->
                                        board.setPiece(nextKingLocation, King(Color.WHITE))
                                            .setPiece(whiteKing.location, Empty())
                                    } + nextQueenTiles.map { tile -> tile.location }
                                    .map { nextQueenLocation ->
                                        board.setPiece(nextQueenLocation, Queen(Color.WHITE))
                                            .setPiece(queen.location, Empty())
                                    }
                            }
                            Move.BLACK -> {
                                val blackKing = board.tileList.first { tile -> tile.piece == King(Color.BLACK) }
                                val nextBlackKingTiles = getNextKingTiles(blackKing, board)

                                nextBlackKingTiles.map { tile -> tile.location }
                                    .map { nextKingLocation ->
                                        board.setPiece(nextKingLocation, King(Color.BLACK))
                                            .setPiece(blackKing.location, Empty())
                                    }
                            }
                        }

                        Boo.WithNextBoardList(
                            board.size,
                            board.tileList,
                            board.move,
                            board.index,
                            // TODO incorrect check state
                            Boo.WithNextBoardList.LegalityWithCheckState.Legal(
                                checkState = board.legalityWithCheckState.checkState,
                                nextBoardList = nextBoardList
                            )
                        )
                    }
                }
            }
        }
    }
}