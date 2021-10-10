package generation.models

data class IndexGraph(
    val index: Int,
    val isLegal: Boolean,
    val move: Move,
    val checkState: CheckState,
    val nextIndexList: List<Int>,
    val parentIndexList: MutableList<Int>,
    var nextGraphList: List<IndexGraph>?
)

@SuppressWarnings("FunctionName")
fun IndexGraph(board: IndexBoard) = IndexGraph(
    index = board.index,
    isLegal = board.isLegal,
    move = board.move,
    checkState = board.checkState,
    nextIndexList = board.nextBoardIndexList,
    parentIndexList = mutableListOf(),
    nextGraphList = null
)