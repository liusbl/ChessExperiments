package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph

fun appendWinIndexList(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
         graph.nextIndexList.forEach loop@{ nextIndex ->
            val nextGraph = graphList.find { it.index == nextIndex } ?: return@loop
            if (nextGraph.checkState == CheckState.BLACK_IN_CHECKMATE) {
                graph.winIndexList.add(IndexGraph.WinIndex(nextIndex = nextIndex, pliesUntilCheckmate = 1))
            }
        }
    }
    // TODO iterate again and treat the win condition not as checkmate but as WinIndex = 1. Then WindIndex = 2.
    //  Keep increasing until no more WinIndexes show up.
}