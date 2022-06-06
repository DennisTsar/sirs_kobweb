package io.github.dennistsar.sirs_kobweb.states

import androidx.compose.runtime.*
import io.github.dennistsar.sirs_kobweb.data.api.Repository
import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.data.classes.School
import io.github.dennistsar.sirs_kobweb.data.mapByCourses
import io.github.dennistsar.sirs_kobweb.data.mapByProfs
import io.github.dennistsar.sirs_kobweb.data.toProfScores
import io.github.dennistsar.sirs_kobweb.misc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class DropDownState<T>(
    val list: Collection<T>,
    val selected: String,
) {
    constructor(selected: String?) : this(emptyList(),selected ?: "")
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
    val wholeDeptMap: Map<String, Pair<Int, List<Double>>>
    val courseSpecificMap: Map<String, Pair<Int, List<Double>>>
    val status: Status
    val url: String
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
    private var mapOfProfs: Map<String,List<Entry>> by mutableStateOf(emptyMap())
    private var mapOfCourses: Map<String,List<Entry>> by mutableStateOf(emptyMap())

    private var firstTime by mutableStateOf(true)

    override var profListLoading by mutableStateOf(true)

    override val url
        get() = "searchdept?" + // note that lack of slash at the start means routePrefix is used
                "school=${schoolState.selected}" +
                "&dept=${deptState.selected}" +
                courseState.selected.run{
                    if(isBlankOrNone()) "" else "&course=$this"
                } +
                profState.selected.run{
                    if(isBlankOrNone()) "" else "&prof=${encodeURLParam()}"
                }

    override val status
        get() =
            if(firstTime)
                Status.InitialLoading
            else if (!profState.selected.isBlankOrNone())
                Status.Prof
            else if (!courseState.selected.isBlankOrNone())
                Status.Course
            else
                Status.Dept

    override val wholeDeptMap by derivedStateOf {
        mapOfProfs
            .toProfScores()
            .mapValues { it.value.toTotalAndAvesPair() }
    }

    override val courseSpecificMap by derivedStateOf {
        mapOfCourses[courseState.selected]?.run {
            mapByProfs()
            .toProfScores()
            .mapValues { it.value.toTotalAndAvesPair() }
        } ?: emptyMap()
    }

    private var _schoolState by mutableStateOf(DropDownState<School>(initialSchool))
    override var schoolState
        get() = _schoolState
        set(value) {
            _schoolState = value
            val temp = schoolMap[value.selected] ?: throw IllegalStateException("Invalid School Key")
            deptState = DropDownState(temp.depts,temp.depts.firstOrNull() ?: "")
        }

    private var _deptState by mutableStateOf(DropDownState<String>(initialDept))
    override var deptState
        get() = _deptState
        set(value) {
            _deptState = value
                .also { if (it.selected.isBlank()) return }

            profListLoading = true

            coroutineScope.launch {
                val response = repository.getEntries(schoolState.selected, deptState.selected)
                deptEntries =
                    if(response is Resource.Success && response.data!=null){
                        response.data.filter { it.scores.size >= 100 }
                    } else {
                        console.log("Error: ${response.message}")
                        emptyList()
                    }
                mapOfProfs = deptEntries.mapByProfs()
                mapOfCourses = deptEntries.mapByCourses()

                // using _ to avoid default behavior which would lose initial values
                _courseState = courseState.copy(listOf(None) + mapOfCourses.keys.sorted())
                _profState = profState.copy(listOf(None) + mapOfProfs.keys.sorted())

                // this keeps ignores prof value if the course is valid
                // otherwise does exactly what you'd expect
                if (!firstTime || !courseState.list.contains(courseState.selected)) {
                    _courseState = courseState.copy(selected = None)
                    if (!firstTime || !profState.list.contains(profState.selected))
                        profState = profState.copy(selected = None)
                }
                else
                    profState = profState.copy(selected = None)

                if (firstTime)
                    firstTime = false
            }
        }

    private var _courseState by mutableStateOf(DropDownState<String>(initialCourse))
    override var courseState
        get() = _courseState
        set(value) {
            _courseState = value
            if(!value.selected.isBlankOrNone())
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
            _schoolState = DropDownState(schoolMap.values,selectedSchool.code)

            selectedSchool.depts.let {
                deptState =
                    if(it.contains(deptState.selected))
                        deptState.copy(list = it)
                    else
                        deptState.copy(list = it, selected = it.firstOrNull() ?: "")
            }
        }
    }

    // temp as I figure out what data the prof composable needs
    // eventually that logic will be in this (or a different?) State object
    // and it will be part of interface
    val profEntries get() = mapOfProfs[profState.selected] ?: emptyList()
}