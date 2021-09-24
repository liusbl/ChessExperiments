/**
 * Using part of David Parlett's piece movement description.
 *
 * These are used:
 *
 * Distances:
 *  1 –> a distance of one (i.e. to adjacent square)
 *  n –> any distance in the given direction
 *
 * Directions:
 *  + -> orthogonally (four possible directions)
 *  X -> diagonally (four possible directions)
 *  * -> orthogonally or diagonally (all eight possible directions)
 *
 *  More TODO
 */
sealed interface Movement {
    fun getNextBoardList(tile: Tile, board: Board): List<Board>

    object None : Movement {
        override fun getNextBoardList(tile: Tile, board: Board) = listOf<Board>()
    }

    data class Basic(val distance: Distance, val direction: Direction) : Movement {
        override fun getNextBoardList(tile: Tile, board: Board): List<Board> = when (distance) {
            Distance.ONE -> {
                when (direction) {
                    is Direction.AnyOrthogonal -> {
                        TODO()
                    }
                    is Direction.AnyDiagonal -> {
                        TODO()
                    }
                    is Direction.OrthogonalAndDiagonal -> {
                        direction.getNextLocationList(tile.location)
                            .filter { location -> location.x < 0 || location.y < 0 }
                            .filter { location -> location.x >= board.size || location.y >= board.size }
                            .filter { location ->
                                val piece = board.tileList.find { tile.location == location }?.piece ?: return@filter true
                                piece == Piece.Empty || tile.piece.color != piece.color
                            }
                            .map { location ->
                                val tileList = board.tileList
                                val result = tileList.map { newTile ->
                                    when {
                                        newTile == tile -> newTile.copy(piece = Piece.Empty)
                                        newTile.location == location -> newTile.copy(piece = tile.piece)
                                        else -> newTile
                                    }
                                }
                                board.copy(
                                    tileList = result,
                                    move = if (board.move == Move.WHITE) Move.BLACK else Move.WHITE
                                )
                            }
                    }
                }
            }
            Distance.N -> {
                listOf()
            }
        }

        enum class Distance {
            ONE,
            N
        }

        sealed interface Direction {
            fun getNextLocationList(location: Location): List<Location>

            object AnyOrthogonal : Direction {
                override fun getNextLocationList(location: Location) = listOf(
                    location.copy(x = location.x + 1),
                    location.copy(x = location.x - 1),
                    location.copy(y = location.y + 1),
                    location.copy(y = location.y - 1),
                )
            }

            object AnyDiagonal : Direction {
                override fun getNextLocationList(location: Location) = listOf(
                    location.copy(x = location.x + 1, y = location.y + 1),
                    location.copy(x = location.x - 1, y = location.y + 1),
                    location.copy(x = location.x + 1, y = location.y - 1),
                    location.copy(x = location.x - 1, y = location.y - 1),
                )
            }

            object OrthogonalAndDiagonal : Direction {
                override fun getNextLocationList(location: Location) =
                    AnyOrthogonal.getNextLocationList(location) +
                            AnyDiagonal.getNextLocationList(location)
            }
        }
    }
}