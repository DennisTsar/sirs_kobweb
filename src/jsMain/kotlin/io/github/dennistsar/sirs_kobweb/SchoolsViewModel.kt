package io.github.dennistsar.sirs_kobweb

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.data.School
import io.github.dennistsar.sirs_kobweb.misc.Resource
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SchoolsViewModel(private val api: Api) {
    var schoolsMap by mutableStateOf<Resource<(Map<String,School>)>>(Resource.Loading())
    var selectedSchool by mutableStateOf("01")
    var selectedDept by mutableStateOf(schoolsMap.data?.get(selectedSchool)?.depts?.get(0) ?: "")

    suspend fun loadSchoolsMap(){
        console.log("hey")
        val res = api.getSchoolDeptsMapFromGit()

        schoolsMap =
            try {
                Resource.Success(Json.decodeFromString(res))

            } catch (e: Exception){
                console.log(e,e.message)
                Resource.Error(e.message?:"?")
            }
        delay(1000)
        console.log("dept2 $selectedDept")
    }

    suspend fun getSchoolMap(): Resource<Map<String,School>>{
        return try {
            val res = api.getSchoolDeptsMapFromGit()
            Resource.Success(Json.decodeFromString(res))
        } catch (e: Exception){
            console.log(e,e.message)
            Resource.Error(e.message?:"?")
        }
    }
}