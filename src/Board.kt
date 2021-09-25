data class Board(
    val size: Int,
    val tileList: List<Tile>,
    val move: Move,
    val legality: Legality,
    val nextBoardList: List<Board>
) {
    override fun toString() = "\n" + this.getPrintableBoard()

    companion object {
        fun createEmpty(size: Int): Board {
            val rowList = (0 until size).map { tileX ->
                (0 until size).map { tileY ->
                    Tile(Location(tileX, tileY), Piece.Empty)
                }
            }.flatten()
            return Board(size, rowList, Move.WHITE, Legality.Legal, listOf())
        }
    }
}

fun Board.getPrintableBoard() = tileList.chunked(size)
    .joinToString(separator = "") { row ->
        row.joinToString(separator = "", postfix = "\n", transform = { tile -> tile.piece.letter.toString() })
    }

/**
 * FEN customizations:
 * Since generate all possible combinations,
 * there are some illegal positions.
 *
 * This is the notation for illegal position:
 * 0. Legal position - L
 * 1. Two kings near to each other - K
 * 2. Side that is making a check has the next move - C
 *
 * Something to note - more then once of these can be at the same time
 *
 * Illegal position notation is added to the end after a space.
 *
 * TODO perhaps add illegal positions for too many or too little kings.
 */
fun Board.getFen(): String {
    var result = ""
    tileList.chunked(size)
        .forEach { row ->
            var emptyCount = 0
            row.forEach { tile ->
                if (tile.piece is Piece.Empty) {
                    emptyCount++
                } else {
                    result += if (emptyCount == 0) {
                        "${tile.piece}"
                    } else {
                        "${emptyCount}${tile.piece}".also {
                            emptyCount = 0
                        }
                    }
                }
            }
            result += if (emptyCount != 0) {
                "$emptyCount/"
            } else {
                "/"
            }
        }
    result = result.dropLast(1)
    result += " ${move.letter}"
    result += " $legality"
    result += " $nextBoardList"
    return result
}