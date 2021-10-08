package generation.models

sealed class Legality(val letterList: List<Char>) {
    object Legal : Legality(listOf('L'))

    sealed class Illegal(letterList: List<Char>) : Legality(letterList) {
        class Combined(legalityList: List<Illegal>) : Illegal(legalityList.map(Illegal::letterList).flatten())

        object KingsAdjacent : Illegal(listOf('K'))

        object CheckButWrongMove : Illegal(listOf('C'))
    }

    override fun toString() = letterList.toString()
}