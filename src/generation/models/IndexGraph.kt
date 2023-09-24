package generation.models

data class IndexGraph(
    val index: Int,
    val usualFen: String,
    val fullFen: String,
    val isLegal: Boolean,
    val move: Move,
    val checkState: CheckState,
    val parentIndexList: MutableList<Int>,
    val nextIndexList: List<Int>,
    val nextGraphList: MutableList<IndexGraph>,
    val winIndexList: MutableSet<WinIndex>
) {
    override fun toString() =
        "IndexGraph(index=$index, " +
            "usualFen=$usualFen, " +
            "legality=${if (isLegal) "LEGAL" else "ILLEGAL"}, " +
            "move=$move, " +
            "checkState=$checkState, " +
            "\nnextIndexList=$nextIndexList, " +
            "\nparentIndexList=$parentIndexList), " +
            "\nwinIndexList=$winIndexList)\n"

    /**
     * This is always a win from Whites perspective, even if assign to Black move.
     */
    sealed interface WinIndex {
        val nextIndex: Int
        data class Unknown(override val nextIndex: Int) : WinIndex

        data class Forced(override val nextIndex: Int, val pliesUntilCheckmate: Int) : WinIndex

        data class Avoidable(override val nextIndex: Int) : WinIndex

        // Consider if Impossible is an option?
    }
}

@SuppressWarnings("FunctionName")
fun IndexGraph(board: IndexBoard) = IndexGraph(
    index = board.index,
    usualFen = board.usualFen,
    fullFen = board.fullFen,
    isLegal = board.isLegal,
    move = board.move,
    checkState = board.checkState,
    parentIndexList = mutableListOf(),
    nextIndexList = board.nextBoardIndexList,
    nextGraphList = mutableListOf(),
    winIndexList = mutableSetOf(),
)