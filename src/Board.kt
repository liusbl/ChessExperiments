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
        abstract val legality: Legality

        data class Legal(
            val withMoveBoard: WithMove,
            val checkState: CheckState,
            val nextBoardList: List<WithMove>
        ) : Final(withMoveBoard) {
            override val legality = Legality.Legal
        }

        data class Illegal(
            val withMoveBoard: WithMove,
            override val legality: Legality.Illegal
        ): Final(withMoveBoard)

        override fun toString() = "\n" + this.getPrintableBoard()

        /**
         * FEN customizations:
         *
         * Check will be denoted by a plus and color in check: +w or +b
         * Checkmate will be denoted by a hashtag and color in checkmate: #w or #b
         *
         * -------
         *
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
//            result += " ${move.letter}"
            result += " $legality"
//            result += " $nextBoardList"
            return result
        }
    }
}