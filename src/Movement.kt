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

        enum class Direction {
            ANY_ORTHOGONAL,
            ANY_DIAGONAL,
            ORTHOGONAL_AND_DIAGONAL
        }
    }
}