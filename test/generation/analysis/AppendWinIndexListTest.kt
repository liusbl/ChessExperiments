package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph
import generation.models.IndexGraph.WinIndex
import generation.models.Move
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppendWinIndexListTest {
    /**
     * 0W -> 1B#
     */
    @Test
    fun singleMoveMate() {
        val list = createWinList(
            mateIndex = 1,
            listOf(1),
            emptyList()
        )

        assertEquals(WinIndex.Forced(1, 1), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     */
    @Test
    fun twoMoveMate() {
        val list = createWinList(
            mateIndex = 3,
            listOf(1),
            listOf(2),
            listOf(3),
            emptyList()
        )

        assertEquals(WinIndex.Forced(1, 3), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B -> 4W -> 5B#
     */
    @Test
    fun threeMoveMate() {
        val list = createWinList(
            mateIndex = 5,
            listOf(1),
            listOf(2),
            listOf(3),
            listOf(4),
            listOf(5),
            emptyList()
        )

        assertEquals(WinIndex.Forced(1, 5), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       1B -> 4W -> 3B#
     */
    @Test
    fun twoMoveTwoBranchMate() {
        val list = createWinList(
            mateIndex = 3,
            listOf(1),
            listOf(2, 4),
            listOf(3),
            emptyList(),
            listOf(3)
        )

        assertEquals(WinIndex.Forced(1, 3), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       5B -> 2W -> 3B#
     *       5B -> 4W -> 3B#
     */
    @Test
    fun twoMoveThreeBranchMate() {
        val list = createWinList(
            mateIndex = 3,
            listOf(1),
            listOf(2, 4),
            listOf(3),
            emptyList(),
            listOf(3),
            listOf(2, 4)
        )

        assertEquals(WinIndex.Forced(1, 3), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       5B -> 0W
     *       5B -> 2W -> 3B#
     */
    @Test
    fun twoMoveOneBadBranchMate() {
        val list = createWinList(
            mateIndex = 3,
            listOf(1),
            listOf(2, 4),
            listOf(3),
            emptyList(),
            listOf(3),
            listOf(0, 2)
        )

        assertEquals(WinIndex.Forced(1, 3), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       1B -> 0W
     */
    @Test
    fun twoMoveAvoidableMate() {
        val list = createWinList(
            mateIndex = 3,
            listOf(1),
            listOf(0, 2),
            listOf(3),
            emptyList()
        )

        assertEquals(WinIndex.Avoidable(nextIndex = 1), list[0].winIndexList.toList()[0])
        assertEquals(WinIndex.Avoidable(nextIndex = 0), list[1].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     * 0W -> 3B#
     *
     */
    @Test
    fun oneMoveMateWithOptions() {
        val list = createWinList(
            mateIndex = 3,
            listOf(1, 3),
            listOf(2),
            listOf(3),
            emptyList()
        )

        assertEquals(WinIndex.Forced(3, 1), list[0].winIndexList.toList()[0])
    }

    private fun createWinList(
        mateIndex: Int,
        vararg eachGraphNextIndexList: List<Int>
    ): List<IndexGraph> = eachGraphNextIndexList.mapIndexed { index, nextIndexList ->
        IndexGraph(
            index = index,
            move = if (index % 2 == 0) Move.WHITE else Move.BLACK,
            checkState = if (index == mateIndex) CheckState.BLACK_IN_CHECKMATE else CheckState.NONE,
            nextIndexList = nextIndexList
        )
    }.also { appendWinIndexList(it) }

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
        winIndexList = mutableSetOf(), // Doesn't matter
    )
}