package generation.models

fun Board.tileAt(x: Int, y: Int): Tile? {
    return tileList.find { tile -> tile.location.x == x && tile.location.y == y }
}

fun Board.WithCheckState.setPiece(location: Location, piece: Piece): Board.WithCheckState {
    val index = tileList.indexOfFirst { tile -> tile.location == location }
    val newTileList = tileList.toMutableList()
        .apply { set(index, Tile(location, piece)) }
        .toList()
    return copy(tileList = newTileList)
}

// TODO consider adding mapping function for Initial -> WithMove -> WithLegality, etc..
sealed interface Board {
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
            data class Legal(
                val legality: Legality.Legal = Legality.Legal,
                val checkState: CheckState
            ) : LegalityWithCheckState

            data class Illegal(val legality: Legality.Illegal) : LegalityWithCheckState
        }

        override fun toString() = getPrintableBoard()
    }

    data class WithNextBoardList(
        override val size: Int,
        override val tileList: List<Tile>,
        val move: Move,
        val index: Int,
        val legalityWithCheckState: LegalityWithCheckState,
    ) : Board {
        sealed interface LegalityWithCheckState {
            val legality: Legality

            data class Legal(
                override val legality: Legality.Legal = Legality.Legal,
                val checkState: CheckState,
                val nextBoardList: List<WithCheckState>
            ) : LegalityWithCheckState {
                override fun toString() = "$legality, checkState=$checkState, nextBoardList=$nextBoardList"
            }

            data class Illegal(override val legality: Legality.Illegal) : LegalityWithCheckState {
                override fun toString() = "$legality"
            }
        }

        override fun toString(): String {
            val printableBoard = getPrintableBoard()
            return "WithNextBoardList(move=$move, index=$index, board:\n$printableBoard\n legality=$legalityWithCheckState)"
        }
    }

    data class WithNextBoardIndexList(
        override val size: Int,
        override val tileList: List<Tile>,
        val move: Move,
        val index: Int,
        val legalityWithCheckState: LegalityWithCheckState,
    ) : Board {
        sealed interface LegalityWithCheckState {
            val legality: Legality

            data class Legal(
                override val legality: Legality.Legal = Legality.Legal,
                val checkState: CheckState,
                val nextBoardIndexList: List<Int>
            ) : LegalityWithCheckState {
                override fun toString() = "$legality, checkState=$checkState, nextBoardIndexList=$nextBoardIndexList"
            }

            data class Illegal(override val legality: Legality.Illegal) : LegalityWithCheckState {
                override fun toString() = "$legality"
            }
        }

        override fun toString(): String {
            val printableBoard = getPrintableBoard()
            return "WithNextBoardList(move=$move, index=$index, board:\n$printableBoard\n legality=$legalityWithCheckState)"
        }
    }
}

fun Board.getPrintableBoard() = "\n" + tileList.chunked(size)
    .joinToString(
        separator = "",
    ) { row ->
        row.joinToString(
            separator = "",
            postfix = "\n",
            transform = { tile -> tile.piece.letter.toString() })
    }