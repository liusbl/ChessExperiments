package generation.analysis

import generation.BoardFenMapper
import generation.models.IndexGraph
import java.io.File
import java.time.LocalDateTime

fun main() {
    println("Step #0: Read file. ${LocalDateTime.now()}")
    val fileLines = File("out_fen_8.txt").readLines()

    println("Step #1: Parse fen information to board list. ${LocalDateTime.now()}")
    val indexBoardList = fileLines.mapNotNull(BoardFenMapper::getBoard)

    println("Step #2: Create graph structure TODO. ${LocalDateTime.now()}")
    val graphList = indexBoardList.map { board ->
        IndexGraph(board.index, board.nextBoardIndexList, null)
    }

    val thing = graphList.size / 1000
    graphList.forEachIndexed { index, graph ->
        if (index % thing == 0) {
            println("Progress: ${index.toFloat() / graphList.size}")
        }
        val nextIndexGraphList = graphList.filter { graph.nextIndexList.contains(it.index) }
        graph.nextGraphList = nextIndexGraphList
    }

    println("Finished ${LocalDateTime.now()}")
}