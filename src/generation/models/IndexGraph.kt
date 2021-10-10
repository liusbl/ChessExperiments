package generation.models

class IndexGraph(
    val index: Int,
    val move: Move,
    val checkState: CheckState,
    val nextIndexList: List<Int>,
    var nextGraphList: List<IndexGraph>?
)