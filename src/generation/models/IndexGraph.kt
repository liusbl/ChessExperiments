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
    val winIndexList: MutableList<WinIndex>
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
     * For White, this represents the moves that it takes to checkmate black.
     * For Black, this represents the moves that take the longest until checkmate.
     */
    sealed interface WinIndex {
        object Unknown : WinIndex

        data class Forced(val nextIndex: Int, val pliesUntilCheckmate: Int) : WinIndex

        object Avoidable : WinIndex

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
    winIndexList = mutableListOf(),
)