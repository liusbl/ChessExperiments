package generation

import generation.models.Board
import generation.models.Piece

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
 *  1. generation.models.Legality
 *  2. Check state (unless Illegal)
 *  3. Next Board indexes (unless Illegal)
 *
 * Sample of legal FEN board with size 4:
 * 1k1K/4/4/Q3 w~45,L,_;15,78,155,611 (numbers here are random)
 *
 * Which represents the board:
 * -k-K
 * ----
 * ----
 * Q---
 * White to move
 *
 *
 * Sample of illegal FEN board with size 4:
 * kKQ1/4/4/4 w~45,K
 *
 * Which represents the board:
 * kKQ-
 * ----
 * ----
 * ----
 * White to move
 *
 * -------
 * GAME ENDING
 *
 * Check will be denoted by a plus and color in check: +W or +B
 * Checkmate will be denoted by a hashtag and color in checkmate: #W or #B
 * Stalemate will be denoted by letter 'S'
 * Draw (2 kings left) will be denoted by letter 'D'
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
    fun getFen(board: Board.Final): String {
        var result = ""
        board.tileList.chunked(board.size)
            .forEach { row ->
                var emptyCount = 0
                row.forEach { tile ->
                    if (tile.piece is Piece.Empty) {
                        emptyCount++
                    } else {
                        result += if (emptyCount == 0) {
                            "${tile.piece}"
                        } else {
                            "${emptyCount}${tile.piece}".also {
                                emptyCount = 0
                            }
                        }
                    }
                }
                result += if (emptyCount != 0) {
                    "$emptyCount/"
                } else {
                    "/"
                }
            }
        result = result.dropLast(1)
        result += " ${board.witMoveBoard.move.letter}"
        result += "~"
        result += "${board.index}"
        result += ","
        result += "${board.legality.letterList[0]}"
        if (board is Board.Final.LegalFinalV2) {
            result += ","
            result += board.checkState.notation
            result += ";"
            board.nextBoardIndexes.forEach { index ->
                result += "$index"
                result += ","
            }
            if (board.nextBoardIndexes.isNotEmpty()) {
                result = result.dropLast(1)
            }
        }
        return result
    }

    fun getBoard(fen: String): Board {
        return TODO()
    }
}