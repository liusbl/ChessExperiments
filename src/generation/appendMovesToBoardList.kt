package generation

import generation.models.Boo
import generation.models.Move

fun appendMovesToBoardList(boardList: List<Boo.Initial>): List<Boo.WithMove> {
    return boardList.flatMap { board ->
        listOf(
            Boo.WithMove(board.size, board.tileList, Move.WHITE),
            Boo.WithMove(board.size, board.tileList, Move.BLACK),
        )
    }
}
