package generation

import generation.models.Board
import generation.models.Legality

fun appendNextBoardIndexes(boardList: List<Board.WithNextBoardList>): List<Board.WithNextBoardIndexList> {
    val godMap = boardList.associate { board -> board.tileList to board.index }
    // TODO cleanup
    val boardListWithNextIndexes = boardList.map { board ->
        when {
            board.legalityWithCheckState !is Board.WithNextBoardList.LegalityWithCheckState.Legal -> {
                Board.WithNextBoardIndexList(
                    board.size, board.tileList, board.move, board.index,
                    Board.WithNextBoardIndexList.LegalityWithCheckState.Illegal(
                        legality = board.legalityWithCheckState.legality as Legality.Illegal
                    )
                )
            }
            board.legalityWithCheckState.nextBoardList.isEmpty() -> {
                Board.WithNextBoardIndexList(
                    board.size, board.tileList, board.move, board.index,
                    Board.WithNextBoardIndexList.LegalityWithCheckState.Legal(
                        checkState = board.legalityWithCheckState.checkState,
                        nextBoardIndexList = emptyList()
                    )
                )
            }
            else -> {
                val nextBoardIndexList = board.legalityWithCheckState.nextBoardList.mapNotNull { nextBoard ->
                    godMap[nextBoard.tileList]
                }

                Board.WithNextBoardIndexList(
                    board.size, board.tileList, board.move, board.index,
                    Board.WithNextBoardIndexList.LegalityWithCheckState.Legal(
                        checkState = board.legalityWithCheckState.checkState,
                        nextBoardIndexList = nextBoardIndexList
                    )
                )
            }
        }
    }
    return boardListWithNextIndexes
}