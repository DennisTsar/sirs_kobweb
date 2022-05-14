package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomDropDown
import io.github.dennistsar.sirs_kobweb.data.Entry
import io.github.dennistsar.sirs_kobweb.data.School
import io.github.dennistsar.sirs_kobweb.logic.getCourseAvesByProf
import io.github.dennistsar.sirs_kobweb.logic.getProfAves
import io.github.dennistsar.sirs_kobweb.misc.Resource
import io.github.dennistsar.sirs_kobweb.misc.TenQsShortened
import io.github.dennistsar.sirs_kobweb.misc.gridVariant12
import io.github.dennistsar.sirs_kobweb.misc.roundToDecimal
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

fun List<List<Int>>.toTotalAndAvePair(): Pair<Int,List<Double>>{
    val k = filter { it.isNotEmpty() }
    return k.map { it.size }.average().toInt() to
            k.map { it.average().roundToDecimal(2) }
}

@Page
@Composable
fun SearchDept() {
    val repository = Repository(Api())
    PageLayout("Search",Modifier.backgroundColor(Color.lightcyan)) {
//        val ctx = rememberPageContext()
        var schoolMap: Map<String, School> by remember{ mutableStateOf(emptyMap()) }
        var selectedSchool by remember { mutableStateOf("") }
        var selectedDept by remember { mutableStateOf("") }
        var selectedCourse by remember { mutableStateOf("") }

        var deptList: List<String> by remember { mutableStateOf(emptyList()) }

        var deptEntries: List<Entry> by remember { mutableStateOf(emptyList()) }
        var courseList: List<String> by remember { mutableStateOf(emptyList()) }

        LaunchedEffect(true){
            console.log("making req")
            repository.getSchoolMap()
                .takeIf { it is Resource.Success }
                ?.run {
                    data?.let { map ->
                        schoolMap = map
                        val (code,school) = map.entries.first()
                        selectedSchool = code
                        selectedDept = school.depts.firstOrNull() ?: ""
                    }
                }
        }

        LaunchedEffect(selectedDept){
            console.log("launched effect 2")
            deptList = schoolMap[selectedSchool]?.depts ?: emptyList()
            selectedCourse = "None"
            deptEntries =
                if ((selectedSchool + selectedDept).isNotBlank())
                    repository.getEntries(selectedSchool, selectedDept)
                        .takeIf { it is Resource.Success }?.data ?: emptyList()
                else emptyList()
            courseList = listOf("None") + getCourseAvesByProf(deptEntries).keys.sorted()
        }

        if(schoolMap.isEmpty())
            return@PageLayout

        searchDeptFormContent(
            selectedDept = selectedDept,
            selectedCourse = selectedCourse,
            schoolList = schoolMap.values,
            deptList = deptList,//schoolMap[selectedSchool]?.depts ?: emptyList(),
            courseList = courseList,
            onSelectSchool =
            {
                selectedSchool = it
                selectedDept = schoolMap[selectedSchool]?.depts?.firstOrNull() ?: ""
            },
            onSelectDept = { selectedDept = it },//Note that this activates the launched effect - look for better way
            onSelectCourse = { selectedCourse=it },
        )

        // Map<Course,Map<Prof,(Ave Responses,Aves)>>
        // a. Should make this an object
        // b. Should make this part of a function (if not part of ths func)


        if(selectedCourse.isBlank() || selectedCourse=="None"){
            val d = getProfAves(deptEntries.filter { it.scores.size>=100 })
                .mapValues { it.value.toTotalAndAvePair() }
                .filter { it.value.second.size>=10 }
            console.log(d.values.size)
            console.log(d.values.count { it.first>20 })
            profScoresList(d.toList().take(500).toMap())
//            console.log(d.keys.lastOrNull())
        }
        else {
//            val mapOfCourses = profAves
//                .mapValues { (_,v) ->
//                    v.mapValues { (_,listOfAllAnswers) ->
//                        val k = listOfAllAnswers.filter { it.isNotEmpty() }
//                        k.map { it.size }.average().toInt() to
//                                k.map { it.average().roundToDecimal(2) }
//                    }
//                }
//            profScoresList(mapOfCourses[selectedCourse])
            val profAves = getCourseAvesByProf(deptEntries.filter { it.scores.size>=100 })
            profScoresList(
                profAves[selectedCourse]
                    ?.mapValues { it.value.toTotalAndAvePair() }
                    ?.filter { it.value.second.size>=10 }
            )
        }
    }
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun profScoresList(
    list:  Map<String, Pair<Int, List<Double>>>?,
){
    Div(
        attrs = SimpleGridStyle
            .toModifier(gridVariant12)
            .asAttributesBuilder()
    ) {
        val spacing = 175.px
        (listOf("")+TenQsShortened+"Total # of Responses").forEach {
            Text(
                it,
                Modifier.width(spacing)
                    .transform { rotate((-40).deg) }
                    .fontSize(15.px)
                    .margin(topBottom = 50.px, leftRight = (-45).px)
                    .textDecorationLine(TextDecorationLine.Underline)
            )
        }

        val gridElementModifier =
            Modifier.width(spacing)
                .fontSize(15.px)
                .margin(topBottom = 10.px, leftRight = -spacing/1.5)

        list?.run {
            toList()
                .sortedBy { -it.second.second[8] }
                .forEach { (prof, nums) ->
                    Text(prof, gridElementModifier)
                    nums.second.subList(0, 10).forEach {
                        Text(it.toString(), gridElementModifier)
                    }
                    Text(nums.first.toString(), gridElementModifier)
                }
        }
    }
}

@Composable
fun searchDeptFormContent(
    //First 2 are needed for proper switch back to first upon reset
    //Maybe should include selectedSchool too but not practically necessary
    selectedDept: String,
    selectedCourse: String,
    schoolList: Collection<School>,
    deptList: Collection<String>,
    courseList: Collection<String>,
    onSelectSchool: (String) -> Unit,
    onSelectDept: (String) -> Unit,
    onSelectCourse: (String) -> Unit,
){
    val modifier2 = Modifier.fillMaxSize()
//            .backgroundColor(Color.chocolate)
    val modifier1 = Modifier//.backgroundColor(Color.palevioletred)
    val labelModifier = Modifier.fontWeight(FontWeight.Bold).padding(2.px,0.px)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("School",
            labelModifier
                .alignSelf(AlignSelf.Start),
        )

        CustomDropDown(
            selectModifier =
            modifier1.borderRadius(50.px)
                .fillMaxWidth()
                .background("#ddd"),
            optionModifier = modifier2,
            list = schoolList,
            onSelect = onSelectSchool,
            getText = { "${it.code} - ${it.name}" },
            getValue = { it.code },
//            selected = schoolList.firstOrNull()
        )

        Row(
            Modifier.alignContent(AlignContent.SpaceEvenly)
        ) {
            Column(
                Modifier.margin(topBottom = 5.px, leftRight = 25.px)
            ){
                Text("Department", labelModifier)

                CustomDropDown(
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                    list = deptList,
                    onSelect = onSelectDept,
                    selected = selectedDept,
                )
            }

            Column(
                Modifier.margin(topBottom = 5.px, leftRight = 25.px)
            ) {
                Text("Course (Optional)", labelModifier)

                CustomDropDown(
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                    list = courseList,
                    onSelect = onSelectCourse,
                    selected = selectedCourse,
                )
            }
        }
    }
}