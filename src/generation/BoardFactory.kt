package generation

import generation.models.Piece.*
import java.io.File
import java.time.LocalDateTime

private const val BOARD_SIZE = 4

fun main() {
    println("Step #0: Create empty board of size $BOARD_SIZE. ${LocalDateTime.now()}")
    val emptyBoard = createEmptyBoard(BOARD_SIZE)

    println("Step #1: Create all single piece boards. ${LocalDateTime.now()}")
    val singlePieceBoardList = createAllSinglePieceBoardList(
        emptyBoard,
        listOf(
            King(Color.WHITE),
            King(Color.BLACK),
            Queen(Color.WHITE),
        )
    )

    println("Step #2: Combine all single piece boards. ${LocalDateTime.now()}")
    val combinedPieceBoardList = combineSinglePieceBoardLists(singlePieceBoardList)

    println("Step #3: Append moves to boards. ${LocalDateTime.now()}")
    val allCombinedPieceBoardListWithMoves = appendMovesToBoardList(combinedPieceBoardList)

    println("Step #4: Append legalities to boards. ${LocalDateTime.now()}")
    val boardListWithCheckState = appendCheckStateToBoardList(allCombinedPieceBoardListWithMoves)

    println("Step #5: Append next board lists. ${LocalDateTime.now()}")
    val boardListWithNextBoardLists = appendNextBoardLists(boardListWithCheckState)

    println("Step #6: Filter legal next board lists. ${LocalDateTime.now()}")
    val boardListWithLegalNextBoardLists = filterLegalNextBoardLists(boardListWithNextBoardLists)

    println("Step #7: Create FEN representations. ${LocalDateTime.now()}")
    val final = boardListWithLegalNextBoardLists.map(BoardFenMapper::getFen)

    println("Step #8: Print results to file. ${LocalDateTime.now()}")
    File("out.txt").writeText(final.joinToString(separator = "\n"))

    println("Finished ${LocalDateTime.now()}")
}