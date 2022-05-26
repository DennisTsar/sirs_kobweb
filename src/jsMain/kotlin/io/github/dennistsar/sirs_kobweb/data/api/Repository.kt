package io.github.dennistsar.sirs_kobweb.data.api

import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.data.classes.School
import io.github.dennistsar.sirs_kobweb.misc.Resource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Repository(private val api: Api) {
//        suspend fun getSchoolsMapFromGit(): Map<String, School> =
//            Json.decodeFromString(api.getSchoolDeptsMapFromGit())
    suspend fun getSchoolMap(): Resource<Map<String, School>>{
        val res = api.getSchoolDeptsMapFromGit()
        return getResource(res)
    }

    suspend fun getEntries(school: String, dept: String): Resource<List<Entry>>{
        val res = api.getEntriesFromGit(school,dept)
        return getResource(res)
    }

    private inline fun<reified T> getResource(s: String): Resource<T>{
        return try {
            Resource.Success(Json.decodeFromString<T>(s))
        } catch (e: Exception){
            console.log(e,e.message)
            Resource.Error(e.message?:"?")
        }
    }
}