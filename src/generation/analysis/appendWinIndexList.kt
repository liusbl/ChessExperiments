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

    var lastWinIndexList = graphList.map { it.winIndexList.toList() }
    var count = 0 // TODO probably not correct
    while (true) {
        println("Updating win list, ${count++}")
        handleBlackMoves(graphList)
        handleWhiteMoves(graphList)
        val newWinIndexList = graphList.map { it.winIndexList.toList() }
        if (lastWinIndexList == newWinIndexList) {
            break
        } else {
            lastWinIndexList = newWinIndexList
        }
    }
}

// Black to move
// "Stupid" simple approach - if black has a single move and it leads to WinIndex=1, then treat it as "winIndex=2".
// TODO Next perhaps check multiple
private fun handleBlackMoves(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
        if(graph.winIndexList.isNotEmpty()) return@forEach
        if (graph.nextIndexList.isEmpty()) return@forEach

        val allMovesLose = graph.nextIndexList.all { nextIndex ->

            val nextGraph = graphList.find { it.index == nextIndex }
                ?: return@all false // For now this null checking works, because this only happens when King takes Queen and draws.
//TODO                 ?: throw Exception("Unexpected index: $nextIndex, possibleIndexes: ${graphList.map { it.index }}")
// This problem happens when a black King takes the Queen.

            nextGraph.winIndexList.isNotEmpty()
        }
        if (allMovesLose) {
            // TODO not sure about this. Is taking the max until checkmate really the right approach? Maybe?
            val maxPliesUntilCheckmate = graph.nextIndexList.map { nextIndex ->
                val nextGraph = graphList.find { it.index == nextIndex }
                nextGraph?.winIndexList?.map { it.pliesUntilCheckmate } ?: listOf(Int.MIN_VALUE)// TODO
            }.flatten().max()
            graph.nextIndexList.all { nextIndex ->
                graph.winIndexList.add(IndexGraph.WinIndex(nextIndex = nextIndex, pliesUntilCheckmate = maxPliesUntilCheckmate + 1))
            }
        }
    }
}

private fun handleWhiteMoves(graphList: List<IndexGraph>) {
    graphList.forEach { graph ->
        if(graph.winIndexList.isNotEmpty()) return@forEach
        if (graph.nextIndexList.isEmpty()) return@forEach

        val anyMoveWins = graph.nextIndexList.any { nextIndex ->
            val nextGraph = graphList.find { it.index == nextIndex }
                ?: return@any false // For now this null checking works, because this only happens when King takes Queen and draws.
//TODO                 ?: throw Exception("Unexpected index: $nextIndex, possibleIndexes: ${graphList.map { it.index }}")
// This problem happens when a black King takes the Queen.
            nextGraph.winIndexList.isNotEmpty()
        }

        if (anyMoveWins) {
            // TODO not sure about this. Is taking the max until checkmate really the right approach? Maybe?
            val minPliesUntilCheckmate = graph.nextIndexList.map { nextIndex ->
                val nextGraph = graphList.find { it.index == nextIndex }
                nextGraph?.winIndexList?.map { it.pliesUntilCheckmate } ?: listOf(Int.MAX_VALUE)// TODO
            }.flatten().min()
            graph.nextIndexList.all { nextIndex ->
                graph.winIndexList.add(IndexGraph.WinIndex(nextIndex = nextIndex, pliesUntilCheckmate = minPliesUntilCheckmate + 1))
            }
        }
    }
}