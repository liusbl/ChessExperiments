package generation

import generation.models.Board
import generation.models.Move

fun appendMovesToBoardList(boardList: List<Board.Partial>): List<Board.WithMove> {
    return boardList.flatMap { board ->
        listOf(
            Board.WithMove(board, Move.WHITE),
            Board.WithMove(board, Move.BLACK),
        )
    }
}
