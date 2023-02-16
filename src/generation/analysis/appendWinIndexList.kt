package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph

fun appendWinIndexList(graphList: List<IndexGraph>) {
    // White to move
    graphList.forEach { graph ->
         graph.nextIndexList.forEach loop@{ nextIndex ->
            val nextGraph = graphList.find { it.index == nextIndex } ?: return@loop
            if (nextGraph.checkState == CheckState.BLACK_IN_CHECKMATE) {
                graph.winIndexList.add(IndexGraph.WinIndex(nextIndex = nextIndex, pliesUntilCheckmate = 1))
            }
        }
    }

    // Black to move
    // "Stupid" simple approach - if black has a single move and it leads to WinIndex=1, then treat it as "winIndex=2".
    // TODO Next perhaps check multiple
    graphList.forEach { graph ->
        if (graph.nextIndexList.size == 1) {
            val nextIndex = graph.nextIndexList[0]
            val nextGraph = graphList.find { it.index == nextIndex } ?: return@forEach
            if (nextGraph.winIndexList.size == 1 && nextGraph.winIndexList[0].pliesUntilCheckmate == 1) {
                graph.winIndexList.add(IndexGraph.WinIndex(nextIndex = nextIndex, pliesUntilCheckmate = 2))
            }
        }
    }
}