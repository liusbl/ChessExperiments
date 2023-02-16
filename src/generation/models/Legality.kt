package generation.models

sealed class Legality(val letterList: List<Char>) {
    object Legal : Legality(listOf('L'))

    sealed class Illegal(letterList: List<Char>) : Legality(letterList) {
        class Combined(legalityList: List<Illegal>) : Illegal(legalityList.map(Illegal::letterList).flatten())

        object KingsAdjacent : Illegal(listOf('K'))

        object CheckButWrongMove : Illegal(listOf('C'))

        /**
         * Happens when the state is technically possible, but not reachable from any other board state.
         * Consider the following board state:
         *  KQ1/2k/3, Black to move:
         *
         *  K Q -
         *  - k -
         *  - - -
         *
         * In this situation, there is no previous place for white's queen to have been to make this check.
         * In practice this legality can simply be made by checking whether it has any parent board states.
         *
         * TODO This is difficult to do just after creation, because you can only do it after gathering parent indexes
         */
        object Unreachable : Illegal(listOf('U'))
    }

    override fun toString() = letterList.toString()
}