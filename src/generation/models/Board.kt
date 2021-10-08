package generation.models

import generation.models.*

fun Board.tileAt(x: Int, y: Int): Tile? {
    return tileList.find { tile -> tile.location.x == x && tile.location.y == y }
}

sealed interface Board {
    val size: Int
    val tileList: List<Tile>

//    override fun toString() = "\n" + this.getPrintableBoard()

    fun getPrintableBoard() = tileList.chunked(size)
        .joinToString(separator = "") { row ->
            row.joinToString(separator = "", postfix = "\n", transform = { tile -> tile.piece.letter.toString() })
        }

    data class Partial(
        override val size: Int,
        override val tileList: List<Tile>
    ) : Board {
        override fun toString() = "\n" + this.getPrintableBoard()
    }

    data class WithMove(
        val partialBoard: Partial,
        val move: Move
    ) : Board by partialBoard

    // Add ID or HASH for full board, so that it can be easily searched.
    sealed class Final(
        val witMoveBoard: WithMove,
    ) : Board by witMoveBoard {
        abstract val index: Int
        abstract val legality: Legality

        data class Legal(
            override val index: Int,
            val withMoveBoard: WithMove,
            val checkState: CheckState,
            val nextBoardList: List<WithMove>
        ) : Final(withMoveBoard) {
            override val legality = Legality.Legal
        }

        data class LegalFinalV2(
            override val index: Int,
            val withMoveBoard: WithMove,
            val checkState: CheckState,
            val nextBoardIndexes: List<Int>
        ) : Final(withMoveBoard) {
            override val legality = Legality.Legal
        }

        data class Illegal(
            override val index: Int,
            val withMoveBoard: WithMove,
            override val legality: Legality.Illegal
        ): Final(withMoveBoard)

        override fun toString() = "\n" + this.getPrintableBoard()

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
//            result += " ${move.letter}"
            result += " $legality"
//            result += " $nextBoardList"
            return result
        }
    }
}