/**
 * Using part of David Parlett's piece movement description.
 *
 * These are used:
 *
 * Distances:
 *  1 –> a distance of one (i.e. to adjacent square)
 *  2 –> a distance of two
 *  n –> any distance in the given direction
 *
 * Directions:
 *  + -> orthogonally (four possible directions)
 *  X -> diagonally (four possible directions)
 *  * -> orthogonally or diagonally (all eight possible directions)
 *
 *  More TODO
 */
sealed class Movement {
    object None : Movement()

    data class Basic(val distance: Distance, val direction: Direction) : Movement() {
        enum class Distance {
            ONE,
            TWO,
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