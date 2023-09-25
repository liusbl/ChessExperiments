package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph
import generation.models.IndexGraph.WinIndex
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
    initializeWinIndexList(graphList)
    appendImmediateCheckmate(graphList)
    calculateMoves(graphList)
    replaceUnknownWithAvoidable(graphList)
}

private fun initializeWinIndexList(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
        graph.nextIndexList.forEach { nextIndex ->
            graph.winIndexList.add(WinIndex.Unknown(nextIndex))
        }
    }
}

private fun appendImmediateCheckmate(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
        graph.nextIndexList.forEach loop@{ nextIndex ->
            val nextGraph = graphList.find { it.index == nextIndex } ?: return@loop
            if (nextGraph.checkState == CheckState.BLACK_IN_CHECKMATE) {
                graph.winIndexList.set(WinIndex.Forced(nextIndex = nextIndex, pliesUntilCheckmate = 1))
            }
        }
    }
}

private fun calculateMoves(graphList: List<IndexGraph>) {
    var lastWinIndexList = graphList.map { it.winIndexList.toList() }
    while (true) {
        appendForcingMoves(graphList)

        val newWinIndexList = graphList.map { it.winIndexList.toList() }
        if (lastWinIndexList == newWinIndexList) {
            break
        } else {
            lastWinIndexList = newWinIndexList
        }
    }
}

private fun appendForcingMoves(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
        val nextGraphList = graph.nextIndexList.mapNotNull { nextIndex -> graphList.find { it.index == nextIndex } }
        when (graph.move) {
            Move.BLACK -> {
                val allForced = nextGraphList.flatMap { it.winIndexList }.all { it is WinIndex.Forced }
                if (allForced) {
                    graph.nextIndexList.forEach { nextIndex ->
                        val nextGraph = graphList.find { it.index == nextIndex } ?: return
                        val winIndex = WinIndex.Forced(nextIndex = nextIndex, pliesUntilCheckmate = nextGraph.minimumForcedPlies() + 1)
                        graph.winIndexList.set(winIndex)
                    }
                }
            }

            Move.WHITE -> {
                nextGraphList.forEach { nextGraph ->
                    nextGraph.winIndexList.filterIsInstance<WinIndex.Forced>().forEach { winIndex ->
                        val newWinIndex = WinIndex.Forced(nextIndex = nextGraph.index, pliesUntilCheckmate = winIndex.pliesUntilCheckmate + 1)
                        graph.winIndexList.set(newWinIndex)
                    }
                }
            }
        }

        // Prioritize Forced indexed, then sort by plies
        val sortedWinIndexList = graph.winIndexList.sortedWith(compareBy({ it !is WinIndex.Forced }, { (it as? WinIndex.Forced)?.pliesUntilCheckmate }))
        graph.winIndexList.clear()
        graph.winIndexList.addAll(sortedWinIndexList)
    }
}

fun IndexGraph.minimumForcedPlies(): Int =
    winIndexList.filterIsInstance<WinIndex.Forced>().minByOrNull { it.pliesUntilCheckmate }?.pliesUntilCheckmate ?: 0

private fun replaceUnknownWithAvoidable(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
        val newWinIndexList = graph.winIndexList.map { winIndex ->
            if (winIndex is WinIndex.Unknown) {
                WinIndex.Avoidable(winIndex.nextIndex)
            } else {
                winIndex
            }
        }
        graph.winIndexList.clear()
        graph.winIndexList.addAll(newWinIndexList)
    }
}

private fun MutableSet<WinIndex>.set(winIndex: WinIndex) {
    val matchingIndex = indexOfFirst { it.nextIndex == winIndex.nextIndex }
    if (matchingIndex != -1) {
        remove(toList()[matchingIndex])
    }
    add(winIndex)
}