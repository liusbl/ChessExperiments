package generation.models

// TODO: Support for illegal boards
data class IndexBoard(
    val index: Int,
    val usualFen: String,
    val fullFen: String,
    val move: Move,
    val isLegal: Boolean,
    val checkState: CheckState,
    val nextBoardIndexList: List<Int>
)