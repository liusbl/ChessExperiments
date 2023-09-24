package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph
import generation.models.Move

/**
 * What do I want to see at the end of this operation?
 * At first, I'm not analysing premoves, so I would just want to see the best moves.
 * - For all White moves, I want to see each winning move and how many moves until checkmate
 * - For all Black moves, I want to see the best next move and how many moves until checkmate.
 *      Best moves are the ones that take the longest until checkmate.
 *      The next best move is also a move if it's a draw on the next turn.
 *
 * First steps: manually create the solution for 3x3 board, so that later it could be used as an integration test.
 *
 * Don't forget to handle STALEMATES!
 */
fun appendWinIndexList(graphList: List<IndexGraph>) {
    var lastWinIndexList = graphList.map { it.winIndexList.toList() }
    while (true) {
        whiteMoves(graphList)

        val newWinIndexList = graphList.map { it.winIndexList.toList() }
        if (lastWinIndexList == newWinIndexList) {
            break
        } else {
            lastWinIndexList = newWinIndexList
        }
    }
}

private fun whiteMoves(graphList: List<IndexGraph>) {
    graphList
        .forEach { graph ->
            graph.nextIndexList.forEach loop@{ nextIndex ->
                val nextGraph = graphList.find { it.index == nextIndex } ?: return@loop
                val winIndex = if (nextGraph.checkState == CheckState.BLACK_IN_CHECKMATE) {
                    IndexGraph.WinIndex.Forced(nextIndex = nextIndex, pliesUntilCheckmate = 1)
                } else if (graph.move == Move.BLACK && nextGraph.winIndexList.isNotEmpty() && nextGraph.winIndexList.all { it is IndexGraph.WinIndex.Forced }) { // For Black
                    val minPlies = nextGraph.winIndexList.filterIsInstance<IndexGraph.WinIndex.Forced>().minBy { it.pliesUntilCheckmate }.pliesUntilCheckmate
                    IndexGraph.WinIndex.Forced(nextIndex = nextIndex, pliesUntilCheckmate = minPlies + 1)
                } else if (graph.move == Move.WHITE && nextGraph.winIndexList.isNotEmpty() && nextGraph.winIndexList.any { it is IndexGraph.WinIndex.Forced }) { // For White
                    val minPlies = nextGraph.winIndexList.filterIsInstance<IndexGraph.WinIndex.Forced>().minBy { it.pliesUntilCheckmate }.pliesUntilCheckmate
                    IndexGraph.WinIndex.Forced(nextIndex = nextIndex, pliesUntilCheckmate = minPlies + 1)
                } else {
                    IndexGraph.WinIndex.Unknown(nextIndex)
                }
                graph.winIndexList.set(winIndex)
            }
        }
}

private fun MutableSet<IndexGraph.WinIndex>.set(winIndex: IndexGraph.WinIndex) {
    val matchingIndex = indexOfFirst { it.nextIndex == winIndex.nextIndex }
    if (matchingIndex != -1) {
        remove(toList()[matchingIndex])
    }
    add(winIndex)
}