package io.github.dennistsar.sirs_kobweb.logic

import io.github.dennistsar.sirs_kobweb.data.Entry

fun getCourseAvesByProf(entries: List<Entry>): Map<String,Map<String,List<List<Int>>>>{
    return entries.groupBy {
        it.code.split(":")
            .getOrElse(2){""}
    }.mapValues { (_,v) ->
        val a = v.any { it.courseName.contains("Lecture") || it.note!=null }
        if(a)
            getProfAves(v.filter { it.courseName.contains("Lecture") || it.note!=null })
        else
            getProfAves(v)
    }
}

fun getProfAves(entries: List<Entry>): Map<String, List<List<Int>>> {
    val names = entries.map { formatName(it.instructor) }
    val mapOfProfs = entries.groupBy { parseName(it.instructor, names) }
        .filterKeys { it.isNotEmpty() && it != "TA" }

    val profRatings = mapOfProfs.filter { it.value.isNotEmpty() }
        .mapValues { (k, v) ->
            v.map { i ->
                i.scores.chunked(10)//grouped by question
                    .map {
                        it.flatMapIndexed { index, k ->
                            if (index in 0..4)
                                List(k.toInt()) { index + 1 }
                            else
                                emptyList()
                        }
                    }//maps to all answers as list
                //ex. 2 5s and 3 4s gives [5,5,4,4,4]
                //this allows for keeping total # of responses and average calculation after flattening
            }
                .flatMap { it.withIndex() }
                .groupBy({ it.index }, { it.value }).values
                .map { it.flatten() }
        }

//    if (profRatings.isEmpty())
//        return null

    return profRatings

//    val profAves = profRatings.map {
//        val row = it.value[8]//This is the teaching effectiveness question
//        Pair(it.key,Pair(row.average().roundToDecimal(2),row.size))
//    }

//    val deptAve = profAves.map { it.second.first }.average().roundToDecimal(2)
//    val totalNum = profAves.sumOf { it.second.second }
//
//    val csv =  (profAves + Pair("Average", Pair(deptAve,totalNum)))
//        .sortedBy { -it.second.first }
//        .joinToString("\n") { "${it.first};${it.second.first};${it.second.second}" }
//    return "Professor;Rating;Total Responses\n$csv"
}

fun formatName(name: String): String{
    return name.replace(Regex(" \\(.*\\)|[,]"),"")//removes stuff in parentheses & removes commas
        .split(" ")
        .run {
            get(0) + (getOrNull(1)?.let { ", ${it.first()}" } ?: "")//Adds first initial if present
        }.uppercase()
}

//This exists so that "Smith" and "Smith, John" are grouped together IFF John is the only Smith in the department
fun parseName(name: String, names: List<String>): String{
    with(formatName(name)){
        if(contains(','))
            return this

        val filtered = names.filter {
            val split = it.split(',')
            split[0]==this && split.size>1
        }.toSet()

        return if (filtered.size==1) filtered.first() else this
    }
}