package generation.creation

import generation.models.Board
import generation.models.Legality

fun appendNextBoardIndexes(boardList: List<Board.WithNextBoardList>): List<Board.WithNextBoardIndexList> {
    val godMap = boardList.associate { board -> (board.tileList to board.move) to board.index }
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
                        nextBoardList = board.legalityWithCheckState.nextBoardList,
                        nextBoardIndexList = emptyList()
                    )
                )
            }
            else -> {
                val nextBoardIndexList = board.legalityWithCheckState.nextBoardList.mapNotNull { nextBoard ->
                    godMap[nextBoard.tileList to nextBoard.move]
                }

                Board.WithNextBoardIndexList(
                    board.size, board.tileList, board.move, board.index,
                    Board.WithNextBoardIndexList.LegalityWithCheckState.Legal(
                        checkState = board.legalityWithCheckState.checkState,
                        nextBoardList = board.legalityWithCheckState.nextBoardList,
                        nextBoardIndexList = nextBoardIndexList
                    )
                )
            }
        }
    }
    return boardListWithNextIndexes
}