package io.github.dennistsar.sirs_kobweb.data.api

import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.data.classes.School
import io.github.dennistsar.sirs_kobweb.misc.Resource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Repository(private val api: Api) {
    suspend fun getSchoolMap(): Resource<Map<String, School>> =
        api.getSchoolDeptsMapFromGit().toResource()

    suspend fun getEntries(school: String, dept: String): Resource<List<Entry>> =
        api.getEntriesFromGit(school, dept).toResource()

    private inline fun<reified T> String.toResource(): Resource<T> {
        return try {
            Resource.Success(Json.decodeFromString<T>(this))
        } catch (e: Exception) {
            console.log(e, e.message)
            Resource.Error(e.message ?: "Error decoding JSON")
        }
    }
}