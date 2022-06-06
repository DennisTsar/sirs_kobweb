package io.github.dennistsar.sirs_kobweb.data

import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.misc.ProfScores


fun List<Entry>.mapByCourses(): Map<String, List<Entry>> {
    // Currently, a not-ideal approach to try to only show profs and not TAs - probably should be a boolean or just marked somehow
    return groupBy {
            it.code.split(':')
                .getOrElse(2) { "" }
        }.mapValues { (_,v) ->
            v.filter {
                it.courseName.contains("Lecture") || it.note!=null
            }.ifEmpty { v }
        }
}

fun List<Entry>.aveScores(): List<List<Int>> {
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
    val profRatings = filter { it.value.isNotEmpty() }
        .mapValues { (_, v) -> v.aveScores() }

    if (profRatings.size<=1)
        return profRatings

    val aves = (0..9).map { i ->
        profRatings.values
            .filter { it.size>=10 }
            .flatMap { it[i] }
    }
    return profRatings+Pair("Average",aves)
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
            get(0) + (getOrNull(1)?.let { ", ${it.first()}" } ?: "")// Adds first initial if present
        }.uppercase()
}

/** This exists so that "Smith" and "Smith, John" are grouped together IFF John is the only Smith in the department */
fun String.findMatchingName(names: List<String>): String {
    if (contains(','))
        return this
    return names
        .filter { equals(it.substringBefore(',')) }
        .toSet()
        .takeIf { it.size==1 }
        ?.first() ?: this
}