package io.github.dennistsar.sirs_kobweb.data

import kotlinx.serialization.Serializable

@Serializable
data class School(
    val code: String,
    val name: String,
    val depts: List<String>,
)