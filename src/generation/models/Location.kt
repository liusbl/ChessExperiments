package generation.models

import kotlin.math.hypot

data class Location(val x: Int, val y: Int)

fun Location.getDistance(location: Location) = hypot(location.x - x.toDouble(), location.y - y.toDouble())