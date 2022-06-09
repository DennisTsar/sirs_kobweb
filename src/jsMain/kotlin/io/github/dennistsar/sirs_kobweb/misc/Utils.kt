package io.github.dennistsar.sirs_kobweb.misc

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.padding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.web.css.CSSNumeric
import org.jetbrains.compose.web.css.px
import kotlin.math.pow
import kotlin.math.roundToInt

// where the value is a list of every individual rating 1-5 given (inner list) for each question in the survey (outer list)
typealias ProfScores = Map<String,List<List<Int>>>
fun String.substringAfterBefore(after: String, before: String): String =
    substringAfter(after).substringBefore(before)

fun String.isBlankOrNone(): Boolean = isBlank() || equals(None)

val urlEncodings = listOf("," to "%2C", " " to "%20")
fun String.encodeURLParam(): String =
    urlEncodings.fold(this) { acc, (a,b) -> acc.replace(a,b) }
fun String.decodeURLParam(): String =
    urlEncodings.fold(this) { acc, (a,b) -> acc.replace(b,a) }

fun Double.roundToDecimal(dec : Int): Double =
    (this * 10.0.pow(dec)).roundToInt()/10.0.pow(dec)

// Stolen from te interwebs
suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> =
    coroutineScope {
        map { async { f(it) } }.awaitAll()
    }

fun List<List<Int>>.toTotalAndAvesPair(): Pair<Int,List<Double>> {
    return filter { it.isNotEmpty() }
        .run {
            map { it.size }.average().toInt() to
                    map { it.average().roundToDecimal(2) }
        }
}

fun Modifier.margin(leftRight: CSSNumeric = 0.px, topBottom: CSSNumeric = 0.px): Modifier =
    margin(left = leftRight, right = leftRight, top = topBottom, bottom = topBottom)

fun Modifier.padding(leftRight: CSSNumeric = 0.px, topBottom: CSSNumeric = 0.px): Modifier =
    padding(left = leftRight, right = leftRight, top = topBottom, bottom = topBottom)

