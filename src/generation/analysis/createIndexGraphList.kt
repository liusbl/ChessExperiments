package generation.analysis

import generation.models.IndexBoard
import generation.models.IndexGraph

fun createIndexGraphList(indexBoardList: List<IndexBoard>) {
    val graphList = indexBoardList.map { board ->
        IndexGraph(board.index, board.nextBoardIndexList, null)
    }
    graphList.forEach { graph ->
        graph.nextGraphList = graph.nextIndexList.mapNotNull { index -> graphList.getOrNull(index) }
    }
}