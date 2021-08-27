data class Board(val size: Int, val tileList: List<Tile>) {
    companion object {
        fun createEmpty(size: Int): Board {
            val rowList = (0 until size).map { tileX ->
                (0 until size).map { tileY ->
                    Tile(Location(tileX, tileY), Piece.Empty)
                }
            }.flatten()
            return Board(size, rowList)
        }
    }
}

fun Board.update(newTile: Tile): Board {
    val tileList = tileList.toMutableList()
    val index = tileList.indexOfFirst { tile -> tile.location == newTile.location }
    tileList[index] = newTile
    return Board(size, tileList)
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
    return result
}