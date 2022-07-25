package io.github.dennistsar.sirs_kobweb.data.api

import io.ktor.client.*
import io.ktor.client.request.*

private const val GH_BASE_URL = "https://raw.githubusercontent.com/DennisTsar/Rutgers-SIRS/master"

class Api {
    private val client = HttpClient()

    suspend fun getEntriesFromGit(school: String, dept: String): String =
        client.get("$GH_BASE_URL/json-data-6/$school/$dept.txt")

    suspend fun getSchoolDeptsMapFromGit(): String =
        client.get("$GH_BASE_URL/json-data/schoolDeptsMap.json")
}