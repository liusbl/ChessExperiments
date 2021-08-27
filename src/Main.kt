import Piece.Color

private const val SIZE = 4

fun main() {
    println("Starting")
    println()

    val allPieceCombinationBoardList = createAllPieceBoards(
        SIZE, listOf(
            Piece.King(Color.WHITE),
            Piece.King(Color.BLACK),
            Piece.Queen(Color.WHITE)
        )
    )

    allPieceCombinationBoardList.forEach {
        println(it.generateFen())
        it.print()
        println()
    }

    println("All piece combination board list size: ${allPieceCombinationBoardList.size}")
}

private fun createAllPieceBoards(size: Int, pieces: List<Piece>): List<Board> {
    val emptyBoard = Board.createEmpty(size)

    val singlePieceBoards = pieces.map { piece ->
        emptyBoard.tileList.map { filledTile ->
            Board(size, emptyBoard.tileList.map { emptyTile ->
                if (emptyTile.location == filledTile.location) {
                    Tile(emptyTile.location, piece)
                } else {
                    Tile(emptyTile.location, Piece.Empty)
                }
            })
        }
    }

    val result = singlePieceBoards.reduce { acc, next ->
        acc.map { combinedBoard ->
            next.map { nextBoard ->
                Board(size, combinedBoard.tileList.zip(nextBoard.tileList) { oldTile, nextTile ->
                    if (oldTile.piece == Piece.Empty) {
                        oldTile.copy(piece = nextTile.piece)
                    } else {
                        oldTile
                    }
                })
            }
        }.flatten()
    }

    return result.filter { board -> board.tileList.count { tile -> tile.piece != Piece.Empty } == pieces.size }
}