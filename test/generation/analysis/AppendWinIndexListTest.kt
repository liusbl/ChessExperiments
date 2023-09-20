package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph
import generation.models.Move
import org.junit.jupiter.api.Test

class AppendWinIndexListTest {
    @Test
    fun appendWinIndexList() {
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
        println(list)
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