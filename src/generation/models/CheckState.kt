package generation.models

enum class CheckState(val notation: String) {
    NONE("_"),
    DRAW("D"), // Two Kings left on the board
    STALEMATE("S"),
    WHITE_IN_CHECK("+W"),
    BLACK_IN_CHECK("+B"),
    WHITE_IN_CHECKMATE("#W"),
    BLACK_IN_CHECKMATE("#B"),
}