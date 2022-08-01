package io.github.dennistsar.sirs_kobweb.data

import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.misc.ProfScores
import io.github.dennistsar.sirs_kobweb.misc.roundToDecimal

fun List<Entry>.mapByCourses(): Map<String, List<Entry>> {
    // Currently, a not-ideal approach to try to only show profs and not TAs - probably should be a boolean or just marked somehow
    return groupBy { it.code.getCourseFromFullCode() }
        .mapValues { (_,v) ->
            v.filter {
                it.courseName.contains("Lecture") || it.note!=null
            }.ifEmpty { v }
        }
}

fun List<Entry>.allScoresPerQ(): List<List<Int>> {
    return map { entry ->
        // maps to all answers as list
        // ex. 2 5s and 3 4s gives [5,5,4,4,4]
        // this allows for keeping total # of responses and average calculation after flattening
        entry.scores
            .chunked(10)// grouped by question
            .map {
                it.subList(0,5).flatMapIndexed { index, d ->
                    List(d.toInt()) { index + 1 }
                }
            }
    }
        .flatMap { it.withIndex() }
        .groupBy({ it.index }, { it.value })
        .values
        .map { it.flatten() }
}

fun Map<String, List<Entry>>.toProfScores(): ProfScores {
    return filter { it.value.isNotEmpty() }
        .mapValues { (_, v) -> v.allScoresPerQ() }
}

fun List<Entry>.mapByProfs(): Map<String, List<Entry>> {
    val usefulNames = map { formatName(it.instructor) }
        .filter { it.contains(",") }
    return groupBy {
        formatName(it.instructor).findMatchingName(usefulNames)
    }.filterKeys { it.isNotEmpty() && it != "TA" }
}

//fun List<Entry>.toProfScores(): ProfScores = toMapOfProfs().toProfScores()

fun formatName(name: String): String {
    return name
        .replace(" \\(.*\\)|,".toRegex(),"")// removes stuff in parentheses & removes commas
        .split(" ")
        .run {
            get(0) + (getOrNull(1)?.let { ", ${it[0]}" } ?: "")// Adds first initial if present
        }.uppercase()
}

// This exists so that "Smith" and "Smith, John" are grouped together IFF John is the only Smith in the department
fun String.findMatchingName(names: List<String>): String {
    if (contains(','))
        return this
    return names
        .filter { equals(it.substringBefore(',')) }
        .toSet()
        .takeIf { it.size==1 }
        ?.first() ?: this
}

fun List<List<Int>>.toTotalAndAvesPair(): Pair<Int,List<Double>> {
    return filter { it.isNotEmpty() }
        .run {
            map { it.size }.average().toInt() to
                    map { it.average().roundToDecimal(2) }
        }
}

fun  Map<String, Pair<Int, List<Double>>>.addAveElement(
    key: String = "Average",
    ignoreKey: String? = "Overall",
): Map<String, Pair<Int, List<Double>>> {
    if (size == 1)
        return this
    val aves = filterKeys { it != ignoreKey }.values.map { it.second }
    val ave = (0..9).map { i -> // corresponding to each question
        aves.map { it[i] }.average().roundToDecimal(2)
    }
    return plus(key to (0 to ave))
}

fun ProfScores.addOverallElement(key: String = "Overall"): ProfScores {
    if (size == 1)
        return this
    val aves = (0..9).map { i ->
        values
            .filter { it.size >= 10 }
            .flatMap { it[i] }
    }
    return plus(key to aves)
}

fun Map<String,List<Entry>>.toDisplayMap(): Map<String, Pair<Int, List<Double>>> {
    return toProfScores()
        .addOverallElement()
        .mapValues { it.value.toTotalAndAvesPair() }
        .addAveElement()
}

fun String.getCourseFromFullCode(): String = split(':')[2]