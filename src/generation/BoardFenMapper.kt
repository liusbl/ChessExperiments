package generation

import generation.models.*

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
    fun getFen(board: Board.WithNextBoardIndexList): String {
        var result = ""
        board.tileList.chunked(board.size)
            .forEach { row ->
                var emptyCount = 0
                row.forEach { tile ->
                    if (tile.piece is Piece.Empty) {
                        emptyCount++
                    } else {
                        result += if (emptyCount == 0) {
                            "${tile.piece.letter}"
                        } else {
                            "${emptyCount}${tile.piece.letter}".also {
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
        result += " ${board.move.letter}"
        result += "~"
        result += "${board.index}"
        result += ","
        result += "${board.legalityWithCheckState.legality.letterList[0]}"
        if (board.legalityWithCheckState is Board.WithNextBoardIndexList.LegalityWithCheckState.Legal) {
            result += ","
            result += board.legalityWithCheckState.checkState.notation
            result += ";"
            board.legalityWithCheckState.nextBoardIndexList.forEach { index ->
                result += "$index"
                result += ","
            }
            if (board.legalityWithCheckState.nextBoardIndexList.isNotEmpty()) {
                result = result.dropLast(1)
            }
        }
        return result
    }

    fun getBoard(fen: String): IndexBoard? {
        val (usualFenPart, customFenPart) = fen.split('~')
        if (!customFenPart.contains("L")) return null
        val index = customFenPart.split(',')[0].toInt()
        val move = Move.values().first { it.letter == usualFenPart.last() }
        val checkState = CheckState.values().first { it.notation == customFenPart.split(',', ';')[2] }
        val nextBoardIndexList = customFenPart.split(';')[1].split(',').map { it.toInt() }
        return IndexBoard(index, move, checkState, nextBoardIndexList)
    }
}

fun main() {
    println(BoardFenMapper.getBoard("1k1K/4/4/Q3 w~45,L,_;15,78,155,611"))
}