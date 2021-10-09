package generation.creation

import generation.models.Board
import generation.models.next

fun filterLegalNextBoardLists(boardList: List<Board.WithNextBoardList>): List<Board.WithNextBoardList> {
    return boardList.map { board ->
        if (board.legalityWithCheckState !is Board.WithNextBoardList.LegalityWithCheckState.Legal ||
            board.legalityWithCheckState.nextBoardList.isEmpty()
        ) {
            return@map board
        }

        val nextBoardList = board.legalityWithCheckState.nextBoardList.map { nextBoard ->
            Board.WithMove(nextBoard.size, nextBoard.tileList, nextBoard.move.next())
        }
        val nextBoardListWithLegalities = appendCheckStateToBoardList(nextBoardList)
        val legalNextBoardList = nextBoardListWithLegalities.filter { nextBoard ->
            nextBoard.legalityWithCheckState is Board.WithCheckState.LegalityWithCheckState.Legal
        }

        board.copy(legalityWithCheckState = board.legalityWithCheckState.copy(nextBoardList = legalNextBoardList))
    }
}