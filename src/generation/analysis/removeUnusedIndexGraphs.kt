package generation.analysis

import generation.models.CheckState
import generation.models.IndexGraph

fun removeUnusedIndexGraphs(graphList: List<IndexGraph>): List<IndexGraph> =
    graphList.filterNot { graph ->
        !graph.isLegal ||
            graph.checkState == CheckState.DRAW ||
            graph.parentIndexList.isEmpty() // Check for Unreachable state, which cannot be set during creation state.
    }