package generation.creation

import generation.models.Board
import generation.models.Move

fun appendMovesToBoardList(boardList: List<Board.Initial>): List<Board.WithMove> {
    return boardList.flatMap { board ->
        listOf(
            Board.WithMove(board.size, board.tileList, Move.WHITE),
            Board.WithMove(board.size, board.tileList, Move.BLACK),
        )
    }
}
