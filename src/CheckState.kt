enum class CheckState(val notation: String) {
    NONE("_"),
    STALEMATE("S"),
    WHITE_IN_CHECK("+w"),
    BLACK_IN_CHECK("+b"),
    WHITE_IN_CHECKMATE("#w"),
    BLACK_IN_CHECKMATE("#b"),
}