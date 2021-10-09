package generation.models

class IndexGraph(
    val index: Int,
    val nextIndexList: List<Int>,
    var nextGraphList: List<IndexGraph>?
)