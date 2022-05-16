package io.github.dennistsar.sirs_kobweb.misc

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.pow
import kotlin.math.roundToInt

fun String.substringAfterBefore(after: String, before: String): String
    = substringAfter(after).substringBefore(before)

fun Double.roundToDecimal(dec : Int): Double
    = (this * 10.0.pow(dec)).roundToInt()/10.0.pow(dec)

//Stolen from te interwebs
suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B>
    = coroutineScope {
        map { async { f(it) } }.awaitAll()
    }

fun List<List<Int>>.toTotalAndAvesPair(): Pair<Int,List<Double>>{
    return filter { it.isNotEmpty() }
        .run {
            map { it.size }.average().toInt() to
                    map { it.average().roundToDecimal(2) }
        }
}

