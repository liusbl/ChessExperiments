package generation.creation

import generation.BoardFenMapper
import generation.models.Piece.*
import java.io.File
import java.time.LocalDateTime

private const val BOARD_SIZE = 3

// TODO Consider possible statistics
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


    println("Step #3: Create boards with only kings. ${LocalDateTime.now()}")
    val kingBoardList = createAllSinglePieceBoardList(
        emptyBoard,
        listOf(
            King(Color.WHITE),
            King(Color.BLACK),
        )
    )

    println("Step #4: Combine board with only kings. ${LocalDateTime.now()}")
    val combinedKingBoardList = combineSinglePieceBoardLists(kingBoardList)

    println("Step #5: Append moves to boards. ${LocalDateTime.now()}")
    val allCombinedPieceBoardListWithMoves = appendMovesToBoardList(combinedPieceBoardList + combinedKingBoardList)

    println("Step #6: Append legalities to boards. ${LocalDateTime.now()}")
    val boardListWithCheckState = appendCheckStateToBoardList(allCombinedPieceBoardListWithMoves)

    println("Step #7: Append next board lists. ${LocalDateTime.now()}")
    val boardListWithNextBoardLists = appendNextBoardLists(boardListWithCheckState)

    println("Step #8: Filter legal next board lists. ${LocalDateTime.now()}")
    val boardListWithLegalNextBoardLists = filterLegalNextBoardLists(boardListWithNextBoardLists)

    println("Step #9: Append next board indexes. ${LocalDateTime.now()}")
    val boardListWithNextIndexes = appendNextBoardIndexes(boardListWithLegalNextBoardLists)

    println("Step #10: Create FEN representations. ${LocalDateTime.now()}")
    val boardFenList = boardListWithNextIndexes.map(BoardFenMapper::getFen)

    println("Step #11: Print results to files. ${LocalDateTime.now()}")
//    File("out_boards.txt").writeText(boardListWithNextIndexes.joinToString(separator = "\n"))
    File("out_fen.txt").writeText(boardFenList.joinToString(separator = "\n"))

    println("Finished ${LocalDateTime.now()}")
}