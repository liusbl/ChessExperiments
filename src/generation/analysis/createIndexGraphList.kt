package generation.analysis

import generation.models.IndexBoard
import generation.models.IndexGraph

fun createIndexGraphList(indexBoardList: List<IndexBoard>): List<IndexGraph> {
    val graphList = indexBoardList.map(::IndexGraph)
    graphList.forEach { graph ->
        if (graph.isLegal) {
            val nextGraphList = graph.nextIndexList.mapNotNull(graphList::getOrNull)
            if (nextGraphList.isNotEmpty()) {
                graph.nextGraphList.addAll(nextGraphList)
            }
        }
    }
    return graphList
}