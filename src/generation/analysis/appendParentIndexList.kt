package generation.analysis

import generation.models.IndexGraph

fun appendParentIndexList(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
        graph.nextIndexList.forEach { nextIndex ->
            graphList[nextIndex].parentIndexList.add(graph.index)
        }
    }
}