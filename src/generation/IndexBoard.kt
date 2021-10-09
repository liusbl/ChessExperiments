package generation

import generation.models.CheckState
import generation.models.Move

// TODO: Support for illegal boards
data class IndexBoard(
    val index: Int,
    val move: Move,
    val checkState: CheckState,
    val nextBoardIndexList: List<Int>
)