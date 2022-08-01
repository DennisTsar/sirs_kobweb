package io.github.dennistsar.sirs_kobweb.states

import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.data.classes.School

data class SearchDeptState(
    val schoolState: DropDownState<School> = DropDownState(),
    val deptState: DropDownState<String> = DropDownState(),
    val courseState: DropDownState<String> = DropDownState(),
    val profState: DropDownState<String> = DropDownState(),
    val deptEntries: List<Entry> = emptyList(),
    val entriesByProf: Map<String,List<Entry>> = emptyMap(),
    val entriesByCourse: Map<String,List<Entry>> = emptyMap(),
)

data class DropDownState<T>(
    val list: Collection<T> = emptyList(),
    val selected: String = "",
)