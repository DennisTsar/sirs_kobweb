package io.github.dennistsar.sirs_kobweb.states

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.dennistsar.sirs_kobweb.data.*
import io.github.dennistsar.sirs_kobweb.data.api.Repository
import io.github.dennistsar.sirs_kobweb.data.classes.School
import io.github.dennistsar.sirs_kobweb.misc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class Status {
    InitialLoading,
    Dept,
    Course,
    Prof,
}

class SearchDeptViewModel(
    private var repository: Repository,
    private var coroutineScope: CoroutineScope,
    initialSchool: String?,
    initialDept: String?,
    initialCourse: String?,
    initialProf: String?,
) {
    var state: SearchDeptState by mutableStateOf(SearchDeptState())
        private set

    private var schoolMap: Map<String,School> by mutableStateOf(emptyMap())

    private var initialLoading by mutableStateOf(true)

    var profListLoading by mutableStateOf(false)

    val url: String
        get() = "searchdept?"+
                "school=${state.schoolState.selected}" +
                "&dept=${state.deptState.selected}" +
                with(state.courseState.selected) { if (isBlankOrNone()) "" else "&course=$this" } +
                with(state.profState.selected) { if (isBlankOrNone()) "" else "&prof=${encodeURLParam()}" }

    val status
        get() =
            when{
                // schoolMap.isEmpty() can work for initialLoading but doesn't account for dept/prof loading
                initialLoading -> Status.InitialLoading
                !state.profState.selected.isBlankOrNone() -> Status.Prof
                !state.courseState.selected.isBlankOrNone() -> Status.Course
                else -> Status.Dept
            }

    val scoresByProf by derivedStateOf {
        state.entriesByProf.toDisplayMap()
    }

    val scoresByProfForCourse by derivedStateOf {
        state.entriesByCourse[state.courseState.selected]?.run {
            mapByProfs().toDisplayMap()
        } ?: emptyMap()
    }

    fun onSelectSchool(
        school: String?,
        dept: String? = null,
        course: String? = null,
        prof: String? = null,
    ) {
        (schoolMap[school] ?: schoolMap["01"])?.let {
            // need to handle this case (if there are no depts)
            val newDept = dept ?: it.depts.firstOrNull() ?: ""
            onSelectDept(dept = newDept, school = it, course = course, prof = prof)
        } ?: throw IllegalStateException("01 School Not Found")
    }

    fun onSelectDept(
        dept: String,
        school: School = schoolMap[state.schoolState.selected]
            ?: throw IllegalStateException("Selected School Not Found"),
        course: String? = null,
        prof: String? = null,
    ) {
        profListLoading = true
        coroutineScope.launch {
            val response = repository.getEntries(school.code, dept)
            val deptEntries =
                if (response is Resource.Success && response.data!=null) {
                    response.data.filter { it.scores.size >= 100 }
                } else {
                    console.log("Error: ${response.message}")
                    emptyList()
                }

            val entriesByProf = deptEntries.mapByProfs()
            val entriesByCourse = deptEntries.mapByCourses()

            val profList = entriesByProf.keys.sorted()
            val courseList = entriesByCourse.keys.sorted()

            val courseValid = courseList.contains(course)
            state = with(state) {
                copy(
                    schoolState = schoolState.copy(schoolMap.values, school.code),
                    deptState = deptState.copy(school.depts, dept),
                    courseState = courseState.copy(
                        courseList.plusElementAtStart(None),
                        course.takeIf { courseValid } ?: None,
                    ),
                    profState = profState.copy(
                        profList.plusElementAtStart(None),
                        prof.takeIf { !courseValid && profList.contains(it) } ?: None,
                    ),
                    entriesByCourse = entriesByCourse,
                    entriesByProf = entriesByProf,
                    deptEntries = deptEntries,
                )
            }
            initialLoading = false
        }
    }

    fun onSelectCourse(course: String) {
        state = with(state) {
            copy(
                courseState = courseState.copy(selected = course),
                profState = profState.copy(selected = if (!course.isBlankOrNone()) None else profState.selected)
            )
        }
    }

    fun onSelectProf(prof: String) {
        state = with(state) {
            copy(
                profState = profState.copy(selected = prof),
                courseState = courseState.copy(selected = if (!prof.isBlankOrNone()) None else courseState.selected)
            )
        }
    }

    init {
        coroutineScope.launch {
            schoolMap = repository.getSchoolMap()
                .takeIf { it is Resource.Success }?.data ?: emptyMap()
            onSelectSchool(school = initialSchool, dept = initialDept, course = initialCourse, prof = initialProf)
        }
    }

    // temp as I figure out what data the prof composable needs
    // eventually that logic will be in this (or a different?) State object
    // and it will be part of interface
    val selectedProfEntries get() = state.entriesByProf[state.profState.selected] ?: emptyList()
    // key: each course for which selected prof has data
    // value: list of average scores for each question (for whole course)
    val applicableCourseAves get() = state.entriesByCourse.filterKeys { name ->
        name in selectedProfEntries.map { it.code.getCourseFromFullCode() }.toSet() // does toSet() matter here?
    }
        .mapValues { (_, courseEntries) ->
        // both of these methods do the same thing, second is clearer but is first more efficient? maybe not tbh
        // also, I think first will more easily expand to extra (post 10) questions
//        courseEntries.mapByProfs().map { (_, profEntries) ->
//            profEntries.aveScores().toTotalAndAvesPair().second
//        }
//            .flatMap { it.withIndex() }
//            .groupBy({ it.index }, { it.value })
//            .values
//            .map { it.average().roundToDecimal(2) }
        val aves = courseEntries.mapByProfs().map { (_ ,profEntries) ->
            profEntries.allScoresPerQ().toTotalAndAvesPair().second
        }
        (0..9).map { i -> // corresponding to each question
            aves.map { it[i] }.average()
        }
    }

    fun onPopState(params: String) {
        params.drop(1).split('&').associate {
            it.split('=', limit = 2).zipWithNext().getOrNull(0) ?: return
        }.let {
            val school = it["school"].takeIf { i -> i != "" } ?: return
            val dept = it["dept"].takeIf { i -> i != "" } ?: return
            val course = it["course"]
            val prof = it["prof"]?.decodeURLParam()

            // need to figure out how to deal wih invalid course/prof on pop -maybe?
            when {
                school != state.schoolState.selected ->
                    onSelectSchool(school = school, dept = dept, course = course, prof = prof)
                dept != state.deptState.selected ->
                    onSelectDept(dept = dept, course = course, prof = prof)
                course != null -> onSelectCourse(course)
                prof != null -> onSelectProf(prof)
                else -> {
                    if (state.courseState.selected != None) onSelectCourse(None)
                    else if (state.profState.selected != None) onSelectProf(None)
                }
            }
        }
    }
}