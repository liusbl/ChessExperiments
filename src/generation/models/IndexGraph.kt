package generation.models

data class IndexGraph(
    val index: Int,
    val usualFen: String,
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

    data class WinIndex(val nextIndex: Int, val pliesUntilCheckmate: Int)
}

@SuppressWarnings("FunctionName")
fun IndexGraph(board: IndexBoard) = IndexGraph(
    index = board.index,
    usualFen = board.usualFen,
    isLegal = board.isLegal,
    move = board.move,
    checkState = board.checkState,
    parentIndexList = mutableListOf(),
    nextIndexList = board.nextBoardIndexList,
    nextGraphList = mutableListOf(),
    winIndexList = mutableListOf(),
)