package generation

import generation.models.Board
import generation.models.Legality
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

    // TODO cleanup
    val godMap = boardListWithCheckState.associate { board -> board.tileList to board.index }

    println("Step #5: Append next board lists. ${LocalDateTime.now()}")
    val boardListWithNextBoardLists = appendNextBoardLists(boardListWithCheckState)

    println("Step #6: Filter legal next board lists. ${LocalDateTime.now()}")
    val boardListWithLegalNextBoardLists = filterLegalNextBoardLists(boardListWithNextBoardLists)

    // TODO cleanup
    val boardListWithNextIndexes = boardListWithLegalNextBoardLists.map { board ->
        when {
            board.legalityWithCheckState !is Board.WithNextBoardList.LegalityWithCheckState.Legal -> {
                Board.WithNextBoardIndexList(
                    board.size, board.tileList, board.move, board.index,
                    Board.WithNextBoardIndexList.LegalityWithCheckState.Illegal(legality = board.legalityWithCheckState.legality as Legality.Illegal)
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

    println("Step #7: Create FEN representations. ${LocalDateTime.now()}")
    val final = boardListWithNextIndexes.map(BoardFenMapper::getFen)

    println("Step #8: Print results to file. ${LocalDateTime.now()}")
    File("out.txt").writeText(final.joinToString(separator = "\n"))

    println("Finished ${LocalDateTime.now()}")
}