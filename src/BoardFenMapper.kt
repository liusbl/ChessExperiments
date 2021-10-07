/**
 * FEN CUSTOMIZATIONS
 *
 * Some usual FEN parts are removed and some custom parts are added.
 * Custom FEN parts are needed so that there would be as little logic as possible once we read the data.
 *
 * Custom FEN part separation:
 *  - Custom FEN from usual FEN is separated by tilde: "~"
 *  - Custom FEN parts themselves separated by comma: ","
 *  - Custom FEN next board indexes separated from previous customizations by semicolon: ";"
 *
 * Order of custom FEN parts:
 *  0. Index
 *  1. Legality
 *  2. Check state
 *  3. Next Board indexes
 *
 * Sample FEN board with size 4:
 * 1k1K/4/4/Q3 w~45,L,_;15,78,155,611
 *
 * Which represents the board:
 * -k-K
 * ----
 * ----
 * Q---
 * White to move
 *
 * -------
 * GAME ENDING
 *
 * Check will be denoted by a plus and color in check: +w or +b
 * Checkmate will be denoted by a hashtag and color in checkmate: #w or #b
 * Stalemate will be denoted by letter 'S'
 *
 * -------
 * LEGALITY
 *
 * Since generate all possible combinations,
 * there are some illegal positions.
 *
 * This is the notation for illegal position:
 * 0. Legal position - L
 * 1. Two kings near to each other - K
 * 2. Side that is making a check has the next move - C
 *
 */

object BoardFenMapper {
    fun getFen(board: Board): String {
        return TODO()
    }

    fun getBoard(fen: String): Board {
        return TODO()
    }
}
