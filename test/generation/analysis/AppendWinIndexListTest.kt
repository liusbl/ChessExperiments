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
        val list = createList(
            mateIndexList = listOf(1),
            mapOf(
                0 to listOf(1),
                1 to emptyList()
            )
        )
        val expected = list.deepCopy()
        expected.winIndexList(0).add(WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 1))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     */
    @Test
    fun twoMoveMate() {
        val list = createList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(2),
                2 to listOf(3),
                3 to emptyList()
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).add(WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 3))
        expected.winIndexList(1).add(WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 2))
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B -> 4W -> 5B#
     */
    @Test
    fun threeMoveMate() {
        val list = createList(
            mateIndexList = listOf(5),
            mapOf(
                0 to listOf(1),
                1 to listOf(2),
                2 to listOf(3),
                3 to listOf(4),
                4 to listOf(5),
                5 to emptyList()
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).add(WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 5))
        expected.winIndexList(1).add(WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 4))
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 3))
        expected.winIndexList(3).add(WinIndex.Forced(nextIndex = 4, pliesUntilCheckmate = 2))
        expected.winIndexList(4).add(WinIndex.Forced(nextIndex = 5, pliesUntilCheckmate = 1))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       1B -> 4W -> 3B#
     */
    @Test
    fun twoMoveTwoBranchMate() {
        val list = createList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(2, 4),
                2 to listOf(3),
                3 to emptyList(),
                4 to listOf(3)
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).add(WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 3))
        expected.winIndexList(1).addAll(
            listOf(
                WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 2),
                WinIndex.Forced(nextIndex = 4, pliesUntilCheckmate = 2)
            )
        )
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))
        expected.winIndexList(4).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       5B -> 0W
     *       5B -> 2W
     */
    @Test
    fun twoMoveOneBadBranchMate() {
        val list = createList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(2),
                2 to listOf(3),
                3 to emptyList(),
                5 to listOf(0, 2)
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).add(WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 3))
        expected.winIndexList(1).add(WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 2))
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))
        expected.winIndexList(5).addAll(
            listOf(
                WinIndex.Forced(nextIndex = 0, pliesUntilCheckmate = 4),
                WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 2)
            )
        )

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     * 0W -> 5B -> 2W
     *       5B -> 4W -> 3B#
     */
    @Test
    fun twoMoveThreeBranchMate() {
        val list = createList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1, 5),
                1 to listOf(2),
                3 to emptyList(),
                2 to listOf(3),
                5 to listOf(2, 4),
                4 to listOf(3)
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).addAll(
            listOf(
                WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 3),
                WinIndex.Forced(nextIndex = 5, pliesUntilCheckmate = 3)
            )
        )
        expected.winIndexList(1).add(WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 2))
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))
        expected.winIndexList(5).addAll(
            listOf(
                WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 2),
                WinIndex.Forced(nextIndex = 4, pliesUntilCheckmate = 2)
            )
        )
        expected.winIndexList(4).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       1B -> 0W
     */
    @Test
    fun twoMoveAvoidableMate() {
        val list = createList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(0, 2),
                2 to listOf(3),
                3 to emptyList()
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).add(WinIndex.Avoidable(nextIndex = 1))
        expected.winIndexList(1).addAll(
            listOf(
                WinIndex.Avoidable(nextIndex = 0),
                WinIndex.Avoidable(nextIndex = 2)
            )
        )
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     * 0W -> 3B#
     *
     */
    @Test
    fun oneMoveMateWithOptions() {
        val list = createList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1, 3),
                1 to listOf(2),
                2 to listOf(3),
                3 to emptyList()
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).addAll(
            listOf(
                WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 3),
                WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1)
            )
        )
        expected.winIndexList(1).add(WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 2))
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 1))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B -> 4W -> 5B -> 6W -> 7B -> 8W -> 9B#
     * 0W -> 11B -> 10W -> 13B#  // White should avoid this branch, even though it seemingly leads to mate. It doesn't due to 9B -> 0W loop. TODO same but inverted
     *       11B -> 12W -> 15B -> 0W
     */
    @Test
    fun quickerButShortAvoidableMateShouldBeIgnored() {
        val list = createList(
            mateIndexList = listOf(9, 13),
            nextIndexMap = mapOf(
                0 to listOf(1, 11),
                1 to listOf(2),
                2 to listOf(3),
                3 to listOf(4),
                4 to listOf(5),
                5 to listOf(6),
                6 to listOf(7),
                7 to listOf(8),
                8 to listOf(9),
                9 to emptyList(),
                11 to listOf(10, 12),
                10 to listOf(13),
                12 to listOf(15),
                13 to emptyList(),
                15 to listOf(0)
            )
        )

        val expected = list.deepCopy()
        expected.winIndexList(0).addAll(
            listOf(
                WinIndex.Forced(nextIndex = 1, pliesUntilCheckmate = 9),
                WinIndex.Avoidable(nextIndex = 11)
            )
        )
        expected.winIndexList(1).add(WinIndex.Forced(nextIndex = 2, pliesUntilCheckmate = 8))
        expected.winIndexList(2).add(WinIndex.Forced(nextIndex = 3, pliesUntilCheckmate = 7))
        expected.winIndexList(3).add(WinIndex.Forced(nextIndex = 4, pliesUntilCheckmate = 6))
        expected.winIndexList(4).add(WinIndex.Forced(nextIndex = 5, pliesUntilCheckmate = 5))
        expected.winIndexList(5).add(WinIndex.Forced(nextIndex = 6, pliesUntilCheckmate = 4))
        expected.winIndexList(6).add(WinIndex.Forced(nextIndex = 7, pliesUntilCheckmate = 3))
        expected.winIndexList(7).add(WinIndex.Forced(nextIndex = 8, pliesUntilCheckmate = 2))
        expected.winIndexList(8).add(WinIndex.Forced(nextIndex = 9, pliesUntilCheckmate = 1))
        expected.winIndexList(11).addAll(
            listOf(
                WinIndex.Forced(nextIndex = 10, pliesUntilCheckmate = 1),
                WinIndex.Avoidable(nextIndex = 12)
            )
        )
        expected.winIndexList(10).add(WinIndex.Forced(nextIndex = 13, pliesUntilCheckmate = 1))
        expected.winIndexList(12).add(WinIndex.Avoidable(nextIndex = 15))
        expected.winIndexList(15).add(WinIndex.Avoidable(nextIndex = 0))

        appendWinIndexList(list)

        assertEquals(expected, list)
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       1B -> 4W -> 5B -> 0W
     *                   5B -> 6W
     * 0W -> 7B -> 6W -> 9B -> 8W -> 11B -> 10W -> 13B -> 12W -> 15B#
     */
    @Test
    fun quickerButShortAndCloseAvoidableMateShouldBeIgnored() {
        val list = createWinList(
            mateIndexList = listOf(3, 15),
            nextIndexMap = mapOf(
                0 to listOf(1, 7),
                1 to listOf(2, 4),
                2 to listOf(3),
                3 to emptyList(),
                4 to listOf(5),
                5 to listOf(0, 6),
                7 to listOf(6),
                6 to listOf(9),
                9 to listOf(8),
                8 to listOf(11),
                11 to listOf(10),
                10 to listOf(13),
                13 to listOf(12),
                12 to listOf(15),
                15 to emptyList()
            )
        )

        assertEquals(WinIndex.Forced(7, 9), list[0].winIndexList.toList()[0])
    }

    private fun createWinList(
        mateIndexList: List<Int>,
        nextIndexMap: Map<Int, List<Int>>
    ): List<IndexGraph> = createList(mateIndexList, nextIndexMap).also { appendWinIndexList(it) }

    private fun createList(mateIndexList: List<Int>, nextIndexMap: Map<Int, List<Int>>) =
        nextIndexMap.map { (index, nextIndexList) ->
            IndexGraph(
                index = index,
                move = if (index % 2 == 0) Move.WHITE else Move.BLACK,
                checkState = if (mateIndexList.contains(index)) CheckState.BLACK_IN_CHECKMATE else CheckState.NONE,
                nextIndexList = nextIndexList
            )
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
        winIndexList = mutableSetOf(), // Doesn't matter
    )
}

private fun List<IndexGraph>.winIndexList(index: Int) = find { it.index == index }!!.winIndexList

private fun List<IndexGraph>.deepCopy(): List<IndexGraph> =
    map { it.copy(winIndexList = it.winIndexList.toMutableSet()) }