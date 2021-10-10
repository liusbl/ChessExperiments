package generation.analysis

import generation.models.IndexBoard
import generation.models.IndexGraph

fun createIndexGraphList(indexBoardList: List<IndexBoard>): List<IndexGraph> {
    val graphList = indexBoardList.map(::IndexGraph)
    graphList.forEach { graph ->
        if (graph.isLegal) {
            graph.nextGraphList = graph.nextIndexList.mapNotNull(graphList::getOrNull)
        }
    }
    return graphList
}