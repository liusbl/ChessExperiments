import Piece.Color

private const val SIZE = 4

fun main(args: Array<String>) {
//    if (args.isEmpty()) {
//        // TODO
//    }
//
//    if (args[0] == "generate") {
//        // TODO
//    }

    println("Starting")
    println()

    val allPieceCombinationBoardList = BoardFactory.createAllPieceBoards(
        SIZE, listOf(
            Piece.King(Color.WHITE),
            Piece.King(Color.BLACK),
            Piece.Queen(Color.WHITE),
        )
    )

    allPieceCombinationBoardList.forEach {
        println(it.generateFen())
        it.print()
        println()
    }

    println("All piece combination board list size: ${allPieceCombinationBoardList.size}")
}