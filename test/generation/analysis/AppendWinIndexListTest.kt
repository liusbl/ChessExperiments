package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph
import generation.models.IndexGraph.WinIndex
import generation.models.Move
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppendWinIndexListTest {
    // 0W -> 1B#
    @Test
    fun singleMoveMate() {
        val list = listOf(
            IndexGraph(
                index = 0,
                move = Move.WHITE,
                checkState = CheckState.NONE,
                nextIndexList = listOf(1),
            ),
            IndexGraph(
                index = 1,
                move = Move.BLACK,
                checkState = CheckState.BLACK_IN_CHECKMATE,
                nextIndexList = emptyList(),
            )
        )
        appendWinIndexList(list)

        assertEquals(WinIndex(1, 1), list[0].winIndexList[0])
    }

    // 0W -> 1B -> 2W -> 3B#
    @Test
    fun twoMoveMate() {
        val list = listOf(
            IndexGraph(
                index = 0,
                move = Move.WHITE,
                checkState = CheckState.NONE,
                nextIndexList = listOf(1),
            ),
            IndexGraph(
                index = 1,
                move = Move.BLACK,
                checkState = CheckState.NONE,
                nextIndexList = listOf(2),
            ),
            IndexGraph(
                index = 2,
                move = Move.WHITE,
                checkState = CheckState.NONE,
                nextIndexList = listOf(3),
            ),
            IndexGraph(
                index = 3,
                move = Move.BLACK,
                checkState = CheckState.BLACK_IN_CHECKMATE,
                nextIndexList = emptyList(),
            )
        )
        appendWinIndexList(list)

        assertEquals(WinIndex(1, 3), list[0].winIndexList[0])
    }

    private fun IndexGraph(
        index: Int,
        move: Move,
        checkState: CheckState,
        nextIndexList: List<Int>
    ): IndexGraph = IndexGraph(
        index = index,
        usualFen = "", // Doesn't matter
        fullFen = "", // Doesn't matter
        isLegal = true,
        move = move,
        checkState = checkState,
        parentIndexList = mutableListOf(), // Doesn't matter
        nextIndexList = nextIndexList,
        nextGraphList = mutableListOf(), // Doesn't matter
        winIndexList = mutableListOf(), // Doesn't matter
    )
}