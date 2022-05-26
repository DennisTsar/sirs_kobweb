package io.github.dennistsar.sirs_kobweb.data.api

import io.ktor.client.*
import io.ktor.client.request.*

class Api {
    private val client = HttpClient()
    suspend fun getByDeptOrCourse(
        semester: String,
        year: Int,
        school: String,
        dept: String,
        course: String = ""
    ): String{
        return client.get("https://sirs.ctaar.rutgers.edu/index.php"){
            parameter("survey[semester]",semester)
            parameter("survey[year]",year)
            parameter("survey[school]",school)
            parameter("survey[dept]",dept)
            parameter("survey[course]",course)
            parameter("mode","course")
        }
    }

    suspend fun getByLastName(lastname: String): String{
        return client.get("https://sirs.ctaar.rutgers.edu/index.php"){
            parameter("survey[lastname]",lastname)
            parameter("mode","name")
        }
    }

    suspend fun getByID(id: String): String{
        return client.get("https://sirs.ctaar.rutgers.edu/index.php"){
            parameter("survey[record]",id)
            parameter("mode","name")
        }
    }

    suspend fun getSchoolsOrDepts(semester: String, year: Int, school: String = ""): String{
        return client.get("https://sirs.ctaar.rutgers.edu/courseFilter.php"){
            parameter("survey[semester]",semester)
            parameter("survey[year]",year)
            parameter("survey[school]",school)
            parameter("mode","course")
        }
    }

    suspend fun getEntriesFromGit(school: String, dept: String): String{
        return client.get("https://raw.githubusercontent.com/DennisTsar/Rutgers-SIRS/master/json-data-4/$school/$dept.txt")
    }

    suspend fun getSchoolDeptsMapFromGit(): String{
        return client.get("https://raw.githubusercontent.com/DennisTsar/Rutgers-SIRS/master/json-data/schoolDeptsMap.json")
    }

    suspend fun getSchoolDeptsMapFromGit2(): String{
        return client.get("https://raw.githubusercontent.com/DennisTsar/Rutgers-SIRS/master/json-data/schoolDeptsMap.json") {
//            install(ContentNegotiation) {
//                json(Json {
//                    prettyPrint = true
//                    isLenient = true
//                })
//            }
        }
    }
}