data class Board(val size: Int, val tileList: List<Tile>, val move: Move) {
    companion object {
        fun createEmpty(size: Int): Board {
            val rowList = (0 until size).map { tileX ->
                (0 until size).map { tileY ->
                    Tile(Location(tileX, tileY), Piece.Empty)
                }
            }.flatten()
            return Board(size, rowList, Move.WHITE)
        }
    }
}

fun Board.update(newTile: Tile): Board {
    val tileList = tileList.toMutableList()
    val index = tileList.indexOfFirst { tile -> tile.location == newTile.location }
    tileList[index] = newTile
    return Board(size, tileList, this.move)
}

fun Board.print() {
    tileList.chunked(size)
        .forEach { row ->
            row.forEach { tile ->
                print(tile.piece)
            }
            println()
        }
    println()
}

/**
 * FEN customizations:
 * Since generate all possible combinations,
 * there are some illegal positions.
 *
 * This is the notation for illegal position:
 * 1. Two kings near to each other - K
 * 2. Side that is making a check has the next move - C
 *
 * Something to note - more then once of these can be at the same time
 *
 * Illegal position notation is added to the end after a space.
 */
fun Board.generateFen(): String {
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
    return result
}