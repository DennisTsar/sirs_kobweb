package io.github.dennistsar.sirs_kobweb.data.classes

import kotlinx.serialization.Serializable

@Serializable
class Entry(
    val instructor: String,
    val term: String,
    val code: String,
    val courseName: String,
    val indexNum: String?,
    val note: String?,
    val enrolled: Int,
    val responses: Int,
    val scores: List<Double>,
    val questions: List<String>?, // null = standard - not using empty cuz could be actually empty
)