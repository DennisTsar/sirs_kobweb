package io.github.dennistsar.sirs_kobweb.data.classes

import kotlinx.serialization.Serializable

@Serializable
data class School(
    val code: String,
    val name: String,
    val depts: List<String>,
)