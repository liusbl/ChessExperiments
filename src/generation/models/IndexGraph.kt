package generation.models

data class IndexGraph(
    val index: Int,
    val isLegal: Boolean,
    val move: Move,
    val checkState: CheckState,
    val parentIndexList: MutableList<Int>,
    val nextIndexList: List<Int>,
    val nextGraphList: MutableList<IndexGraph>,
    val nextBestIndexList: MutableList<Int> // TODO It's not totally clear what is meant by "best"
) {
    override fun toString() =
        "IndexGraph(index=$index, " +
            "legality=${if (isLegal) "LEGAL" else "ILLEGAL"}, " +
            "move=$move, " +
            "checkState=$checkState, " +
            "\nnextIndexList=$nextIndexList, " +
            "\nparentIndexList=$parentIndexList), " +
            "\nnextBestIndexList=$nextBestIndexList)\n"
}

@SuppressWarnings("FunctionName")
fun IndexGraph(board: IndexBoard) = IndexGraph(
    index = board.index,
    isLegal = board.isLegal,
    move = board.move,
    checkState = board.checkState,
    parentIndexList = mutableListOf(),
    nextIndexList = board.nextBoardIndexList,
    nextGraphList = mutableListOf(),
    nextBestIndexList = mutableListOf(),
)