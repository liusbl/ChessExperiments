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