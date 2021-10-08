package generation.models

fun Board.tileAt(x: Int, y: Int): Tile? {
    return tileList.find { tile -> tile.location.x == x && tile.location.y == y }
}

// TODO consider adding mapping function for Initial -> WithMove -> WithLegality, etc..
sealed interface Boo {
    val size: Int
    val tileList: List<Tile>

    data class Initial(
        override val size: Int,
        override val tileList: List<Tile>
    ) : Board

    data class WithMove(
        override val size: Int,
        override val tileList: List<Tile>,
        val move: Move
    ) : Board

    data class WithCheckState(
        override val size: Int,
        override val tileList: List<Tile>,
        val move: Move,
        val index: Int,
        val legalityWithCheckState: LegalityWithCheckState
    ) : Board {
        sealed interface LegalityWithCheckState {
            data class Legal(val legality: Legality.Legal = Legality.Legal, val checkState: CheckState) : LegalityWithCheckState

            data class Illegal(val legality: Legality.Illegal) : LegalityWithCheckState
        }
    }
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
        ) : Final(withMoveBoard)

        override fun toString() = "\n" + this.getPrintableBoard()
    }
}