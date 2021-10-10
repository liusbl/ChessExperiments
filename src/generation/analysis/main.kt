package generation.analysis

import generation.BoardFenMapper
import java.io.File
import java.time.LocalDateTime

/**
 *
 * Option 3:
 * Add parentIndexes list.
 * -----------
 *
1 w -> 2, 4, 6
2 b, #b
3 w -> 4, 6
4 b -> 1, 5
5 w -> 4, 6
6 b -> 3, 5

--------------
Option 2:
1, 2
1, 4, 1
1, 4, 5, 4, 5, 4, 5, 4...

Make a list of all the graphs.



--------------
Step #1
Einama per visus black. jei #b, pazymimas raide F

Step #2
Einama per visus white.
Jei tarp nextIndexList yra F, tuomet tas white pazymimas, raide G

Step #3
Einama per visus black.
Ignoruojami F.
Raide H pazymimas pirmas nextIndex, kuris nera G.
o gal sitaip???: bestIndex paimamas pirmas nextIndex, kuris nera G.

Step #4
Einama per visus white.


 */

fun main() {
    println("Step #0: Read file. ${LocalDateTime.now()}")
    val fileLines = File("out_fen_4.txt").readLines()

    println("Step #1: Parse fen information to board list. ${LocalDateTime.now()}")
    val indexBoardList = fileLines.mapNotNull(BoardFenMapper::getBoard)

    println("Step #2: Create graph structure. ${LocalDateTime.now()}")
    val indexGraphList = createIndexGraphList(indexBoardList)

    println("Step #3: Append parent index list. ${LocalDateTime.now()}")
    appendParentIndexList(indexGraphList)

    println("Finished ${LocalDateTime.now()}")
}

//    File("res.txt").writeText(indexGraphList.joinToString(separator = "\n"))

//fun searchV3(graphList: List<IndexGraph>) {
//    val array = Array(graphList.size) { mutableListOf<Int>() }
//
//    fun thing(index: Int, graph: IndexGraph) {
//        if (!graph.isSearched) {
//            graph.isSearched = true
//            graph.nextGraphList?.forEach {
//                thing(index, it)
//            }
//        }
//    }
//
//    graphList.forEachIndexed { index, graph ->
//        thing(index, graph)
//    }
//
//    val a = array
//}

//fun searchV2(graphList: List<IndexGraph>) {
//    val checkmateGraphList = graphList.filter { it.checkState == CheckState.BLACK_IN_CHECKMATE }
//    checkmateGraphList.forEach { graph ->
//        // Currently move is for black
//        graph.nextIndex = -1
//    }
//
////    val drawGraphList = graphList.filter { it.checkState == CheckState.DRAW || it.checkState == CheckState.STALEMATE }
////    drawGraphList.forEach { graph ->
////         Currently move is for black
////        graph.nextWorstIndex = -2
////    }
//
//    repeat(1) { // If repeating too much will be worst move for black. He needs to aim for DRAW or STALEMATE
//        graphList.forEach { graph ->
//            if (graph.isLegal) {
//                graph.nextGraphList?.forEach { nextGraph ->
//                    when (graph.move) {
//                        Move.WHITE -> {
//                            if (graph.nextIndex == null && nextGraph.nextIndex != null) {
//                                graph.nextIndex = nextGraph.index
//                            }
//                        }
//                        Move.BLACK -> {
//                            if (graph.nextIndex == null && nextGraph.nextIndex == null) {
//                                graph.nextIndex = nextGraph.index
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

// Right now we start from the first index and go deep.
//  What if we instead started from the bottom, from the checkmate state?
//fun searchV1(graph: IndexGraph) {
//    if (graph.isLegal && (graph.checkState == CheckState.BLACK_IN_CHECKMATE || graph.nextBestIndex != null)) {
//        graph.nextBestIndex?.let { return }
//        graph.nextGraphList?.forEach { nextGraph ->
//            when (nextGraph.checkState) {
//                CheckState.STALEMATE, CheckState.DRAW -> {
//                    // For white: bad, avoid this
//                    // For black: good, we want this
//                    when (nextGraph.move) {
//                        Move.WHITE -> {
//                            return@forEach
//                        }
//                        Move.BLACK -> {
//                            graph.nextBestIndex = nextGraph.index
//                            return
//                        }
//                    }
//                }
//                CheckState.BLACK_IN_CHECKMATE -> {
//                    // For white: bad, avoid this
//                    // For black: good, we want this
//                    when (nextGraph.move) {
//                        Move.WHITE -> {
//                            graph.nextBestIndex = nextGraph.index
//                            return
//                        }
//                        Move.BLACK -> {
//                            return@forEach
//                        }
//                    }
//                }
//                else -> {
//                    // Don't care, continue searching
//                    val nextBest = graph.nextGraphList?.find { it.nextBestIndex != null }
//                    if (nextBest == null) {
//                        searchV2(nextGraph)
//                    } else {
//                        graph.nextBestIndex = nextBest.index
//                    }
//                }
//            }
//        }
//    }
//}