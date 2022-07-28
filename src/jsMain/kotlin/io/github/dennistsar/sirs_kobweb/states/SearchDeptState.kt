package io.github.dennistsar.sirs_kobweb.states

import androidx.compose.runtime.*
import io.github.dennistsar.sirs_kobweb.data.*
import io.github.dennistsar.sirs_kobweb.data.api.Repository
import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.data.classes.School
import io.github.dennistsar.sirs_kobweb.misc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

data class DropDownState<T>(
    val list: Collection<T>,
    val selected: String,
) {
    constructor(selected: String?) : this(emptyList(), selected ?: "")
}

private fun<T> setPropIfDifferent(property: KMutableProperty0<DropDownState<T>>, newVal: String?){
    with(property) {
        newVal?.let {
            if (get().selected != it)
                set(get().copy(selected = it))
        }
    }
}

enum class Status {
    InitialLoading,
    Dept,
    Course,
    Prof,
}

@Stable
interface SearchDeptState {
    var schoolState: DropDownState<School>
    var deptState: DropDownState<String>
    var courseState: DropDownState<String>
    var profState: DropDownState<String>
    var profListLoading: Boolean
    var noDeptReset: Boolean
    val scoresByProf: Map<String, Pair<Int, List<Double>>>
    val scoresByProfForCourse: Map<String, Pair<Int, List<Double>>>
    val status: Status
    val url: String?
}

class SearchDeptStateImpl(
    private var repository: Repository,
    private var coroutineScope: CoroutineScope,
    initialSchool: String?,
    initialDept: String?,
    initialCourse: String?,
    initialProf: String?,
) : SearchDeptState {

    private var schoolMap: Map<String,School> by mutableStateOf(emptyMap())

    private var deptEntries: List<Entry> by mutableStateOf(emptyList())
    private var entriesByProf: Map<String,List<Entry>> by mutableStateOf(emptyMap())
    private var entriesByCourse: Map<String,List<Entry>> by mutableStateOf(emptyMap())

    private var urlReady by mutableStateOf(false)
    private var initialLoading by mutableStateOf(true)

    // used for first time and for pop events (forward/back press)
    override var noDeptReset by mutableStateOf(true)

    override var profListLoading by mutableStateOf(false)

    override val url: String?
        get() {
            return if (urlReady) {
                "searchdept?" + // note that lack of slash at the start means routePrefix is used
                        "school=${schoolState.selected}" +
                        "&dept=${deptState.selected}" +
                        with(courseState.selected) { if (isBlankOrNone()) "" else "&course=$this" } +
                        with(profState.selected) { if (isBlankOrNone()) "" else "&prof=${encodeURLParam()}" }
            } else null
        }

    override val status
        get() =
            when{
                // schoolMap.isEmpty() can work for initialLoading but doesn't account for dept/prof loading
                initialLoading -> Status.InitialLoading
                !profState.selected.isBlankOrNone() -> Status.Prof
                !courseState.selected.isBlankOrNone() -> Status.Course
                else -> Status.Dept
            }

    override val scoresByProf by derivedStateOf {
        entriesByProf
            .toProfScores()
            .mapValues { it.value.toTotalAndAvesPair() }
    }

    override val scoresByProfForCourse by derivedStateOf {
        entriesByCourse[courseState.selected]?.run {
            mapByProfs()
            .toProfScores()
            .mapValues { it.value.toTotalAndAvesPair() }
        } ?: emptyMap()
    }

    private var _schoolState by mutableStateOf(DropDownState<School>(initialSchool))
    override var schoolState
        get() = _schoolState
        set(value) {
            urlReady = false
            _schoolState = value
            with(schoolMap[value.selected] ?: throw IllegalStateException("Invalid School Key")) {
                deptState = DropDownState(depts, depts.firstOrNull() ?: "")
            }
        }

    private var _deptState by mutableStateOf(DropDownState<String>(initialDept))
    override var deptState
        get() = _deptState
        set(value) {
            urlReady = false
            _deptState = value
                .also { if (it.selected.isBlank()) return }

            profListLoading = true

            coroutineScope.launch {
                val response = repository.getEntries(schoolState.selected, deptState.selected)
                deptEntries =
                    if (response is Resource.Success && response.data!=null){
                        response.data.filter { it.scores.size >= 100 }
                    } else {
                        console.log("Error: ${response.message}")
                        emptyList()
                    }
                entriesByProf = deptEntries.mapByProfs()
                entriesByCourse = deptEntries.mapByCourses()

                // using backing var (with _) to avoid default behavior which would lose initial values
                _courseState = courseState.copy(listOf(None) + entriesByCourse.keys.sorted())
                _profState = profState.copy(listOf(None) + entriesByProf.keys.sorted())

                // this keeps course value and ignores prof value if the course is valid
                // otherwise does exactly what you'd expect
                // note all of these don't have to use backing vars, but might as well ig? - or not?
                if (!noDeptReset || !(courseState.list-None).contains(courseState.selected)) {
                    _courseState = courseState.copy(selected = None)
                    if (!noDeptReset || !profState.list.contains(profState.selected))
                        _profState = profState.copy(selected = None)
                }
                else
                    _profState = profState.copy(selected = None)

                if (urlReady)
                    noDeptReset = false
                urlReady = true
                initialLoading = false
            }
        }

    private var _courseState by mutableStateOf(DropDownState<String>(initialCourse))
    override var courseState
        get() = _courseState
        set(value) {
            _courseState = value
            if (!value.selected.isBlankOrNone())
                profState = profState.copy(selected = None)
        }

    private var _profState by mutableStateOf(DropDownState<String>(initialProf))
    override var profState
        get() = _profState
        set(value) {
            _profState = value
            if (!value.selected.isBlankOrNone())
                courseState = courseState.copy(selected = None)
        }

    init {
        coroutineScope.launch {
            schoolMap = repository.getSchoolMap()
                .takeIf { it is Resource.Success }?.data ?: emptyMap()
            val selectedSchool = schoolMap[schoolState.selected]
                ?: schoolMap["01"]
                ?: throw IllegalStateException("01 School Not Present")
            // using private var to not cause default behavior of resetting dept
            // doesn't seem ideal, but I don't want permanent boolean check
            _schoolState = DropDownState(schoolMap.values, selectedSchool.code)

            selectedSchool.depts.let {
                deptState =
                    if (it.contains(deptState.selected))
                        deptState.copy(list = it)
                    else
                        deptState.copy(list = it, selected = it.firstOrNull() ?: "")
            }
        }
    }

    // temp as I figure out what data the prof composable needs
    // eventually that logic will be in this (or a different?) State object
    // and it will be part of interface
    val selectedProfEntries get() = entriesByProf[profState.selected] ?: emptyList()
    // key: each course for which selected prof has data
    // value: list of average scores for each question (for whole course)
    val applicableCourseAves get() = entriesByCourse.filterKeys { name ->
        name in selectedProfEntries.map{ it.code.getCourseFromFullCode() }.toSet() // does toSet() matter here?
    }.mapValues {(_,courseEntries) ->
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
            profEntries.aveScores().toTotalAndAvesPair().second
        }
        (0..9).map{ i -> // corresponding to each question
            aves.map { it[i] }.average()
        }
    }

    fun onPopState(s: String){
        s.drop(1).split('&').associate {
            it.split('=', limit = 2).zipWithNext().getOrNull(0) ?: return
        }.let {
            noDeptReset = true
            setPropIfDifferent(::schoolState, it["school"])
            setPropIfDifferent(::deptState, it["dept"])
            setPropIfDifferent(::courseState, it["course"] ?: None)
            setPropIfDifferent(::profState, it["prof"]?.decodeURLParam() ?: None)
        }
    }
}