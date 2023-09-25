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
            mateIndexList = listOf(1),
            mapOf(
                0 to listOf(1),
                1 to emptyList()
            )
        )

        assertEquals(WinIndex.Forced(1, 1), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     */
    @Test
    fun twoMoveMate() {
        val list = createWinList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(2),
                2 to listOf(3),
                3 to emptyList()
            )
        )

        assertEquals(WinIndex.Forced(1, 3), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B -> 4W -> 5B#
     */
    @Test
    fun threeMoveMate() {
        val list = createWinList(
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

        assertEquals(WinIndex.Forced(1, 5), list[0].winIndexList.toList()[0])
    }

    /**
     * 0W -> 1B -> 2W -> 3B#
     *       1B -> 4W -> 3B#
     */
    @Test
    fun twoMoveTwoBranchMate() {
        val list = createWinList(
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(2, 4),
                2 to listOf(3),
                3 to emptyList(),
                4 to listOf(3)
            )
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
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(2, 4),
                2 to listOf(3),
                3 to emptyList(),
                4 to listOf(3),
                5 to listOf(2, 4)
            )
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
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(2, 4),
                2 to listOf(3),
                3 to emptyList(),
                4 to listOf(3),
                5 to listOf(0, 2)
            )
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
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1),
                1 to listOf(0, 2),
                2 to listOf(3),
                3 to emptyList()
            )
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
            mateIndexList = listOf(3),
            mapOf(
                0 to listOf(1, 3),
                1 to listOf(2),
                2 to listOf(3),
                3 to emptyList()
            )
        )

        assertEquals(WinIndex.Forced(3, 1), list[0].winIndexList.toList()[0])
    }

//    /**
//    TODO
//     * 0W -> 1B -> 2W -> 3B -> 4W -> 1B
//     *       1B -> 6W -> 5B#
//     */
//    @Test
//    fun threeMoveAvoidableMate() {
//        val list = createWinList(
//            mateIndexList = listOf(7),
//            listOf(1),
//            listOf(2, 6),
//            listOf(3),
//            listOf(4),
//            listOf(1),
//            emptyList()
//        )
//
//        assertEquals(WinIndex.Forced(1, 5), list[0].winIndexList.toList()[0])
//    }

    /**
     * 0W -> 1B -> 2W -> 3B -> 4W -> 5B -> 6W -> 7B -> 8W -> 9B#
     * 0W -> 11B -> 10W -> 13B#  // White should avoid this branch, even though it seemingly leads to mate. It doesn't due to 9B -> 0W loop. TODO same but inverted
     *       11B -> 12W -> 15B -> 0W
     */
    @Test
    fun quickerButShortAvoidableMateShouldBeIgnored() {
        val list = createWinList(
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

        assertEquals(WinIndex.Forced(1, 9), list[0].winIndexList.toList()[0])
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
    ): List<IndexGraph> = nextIndexMap.map { (index, nextIndexList) ->
        IndexGraph(
            index = index,
            move = if (index % 2 == 0) Move.WHITE else Move.BLACK,
            checkState = if (mateIndexList.contains(index)) CheckState.BLACK_IN_CHECKMATE else CheckState.NONE,
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