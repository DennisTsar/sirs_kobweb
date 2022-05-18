package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Box
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
import io.github.dennistsar.sirs_kobweb.logic.mapByCourses
import io.github.dennistsar.sirs_kobweb.logic.mapByProfs
import io.github.dennistsar.sirs_kobweb.logic.toProfScores
import io.github.dennistsar.sirs_kobweb.misc.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Page
@Composable
fun SearchDept() {
    val repository = Repository(Api())
    PageLayout("Search",Modifier.backgroundColor(Color.lightcyan)) {
//        val ctx = rememberPageContext()
        val myCoroutineScope = rememberCoroutineScope()

        var schoolMap: Map<String, School> by remember{ mutableStateOf(emptyMap()) }
        var selectedSchool by remember { mutableStateOf("") }
        var selectedDept by remember { mutableStateOf("") }
        var selectedCourse by remember { mutableStateOf("") }
        var selectedProf by remember { mutableStateOf("") }

        var deptEntries: List<Entry> by remember { mutableStateOf(emptyList()) }
        var mapOfProfs: Map<String,List<Entry>> by remember { mutableStateOf(emptyMap()) }
        var mapOfCourses: Map<String,List<Entry>>  by remember { mutableStateOf(emptyMap()) }

        val updateSelectedDept: (String?) -> Unit = fun(str) {
            console.log("updating dept2: $str")
            selectedCourse = "None"
            selectedProf = "None"
            selectedDept = str ?: "".also { return } // Returns if str is null - kinda cool but a little weird
            myCoroutineScope.launch {
                deptEntries = repository.getEntries(selectedSchool, selectedDept)
                    .takeIf { it is Resource.Success }
                    ?.data
                    ?.filter { it.scores.size >= 100 } ?: emptyList()
                mapOfProfs = deptEntries.mapByProfs()
                mapOfCourses = deptEntries.mapByCourses()
            }
        }

        LaunchedEffect(true){
            console.log("making req")
            schoolMap =
                repository.getSchoolMap()
                    .takeIf { it is Resource.Success }
                    ?.data ?: emptyMap()
            val firstSchool = schoolMap["01"]!!//.firstNotNullOf{ it }//entries.first()//idk about the !!//maybe have no default??
            selectedSchool = firstSchool.code
            updateSelectedDept(firstSchool.depts.firstOrNull())
        }

        if (schoolMap.isEmpty())
            return@PageLayout

        searchDeptFormContent(
            selectedDept = selectedDept,
            selectedCourse = selectedCourse,
            selectedProf = selectedProf,
            schools = schoolMap.values,
            depts = schoolMap[selectedSchool]!!.depts,
            courses = listOf("None") + mapOfCourses.keys.sorted(),
            profs = listOf("None") + mapOfProfs.keys.sorted(),
            onSelectSchool =
            {
                selectedSchool = it
                updateSelectedDept(schoolMap[selectedSchool]!!.depts.firstOrNull())
            },
            onSelectDept = { updateSelectedDept(it) },
            onSelectCourse = { selectedCourse = it },
            onSelectProf = { selectedProf = it },
        )

        if (deptEntries.isEmpty())
            return@PageLayout

        if (!selectedProf.isBlankOrNone())
            Text(selectedProf)

        val mapOfEntries =
            if (selectedCourse.isBlankOrNone())
                mapOfProfs
            else
                mapOfCourses[selectedCourse]!!.mapByProfs()
        profScoresList(
            mapOfEntries
                .toProfScores()
                .mapValues { it.value.toTotalAndAvesPair() }
        ){}
    }
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun profScoresList(
    list:  Map<String, Pair<Int, List<Double>>>,
    onNameClick: (String) -> Unit,
){
    Div(
        attrs = SimpleGridStyle
            .toModifier(gridVariant12)
            .asAttributesBuilder()
    ) {
        val spacing = 80.px
        val fontSize = 15.px
        (listOf("")+TenQsShortened+"Total # of Responses").forEach {
            Box(
                Modifier.width(spacing)
            ){
                Text(
                    it,
                    Modifier
                        .width(175.px)
                        .transform { rotate((-45).deg) }
                        .fontSize(fontSize)
                        .margin(topBottom = 50.px, leftRight = (-18).px)
                        .textDecorationLine(TextDecorationLine.Underline)
                )
            }
        }

        val gridElementModifier =
            Modifier.width(spacing)
                .fontSize(fontSize)
                .margin(topBottom = 7.5.px, leftRight = 0.px)
                .alignSelf(AlignSelf.Center)
//                .overflowWrap(OverflowWrap.BreakWord)

        list.entries
            .sortedBy { -it.value.second[8] }
            .take(300)//for performance reasons
            .forEach { (prof, nums) ->
                Box(gridElementModifier){
                    val offset = 40.px
                    Text(prof,
                        Modifier
                            .margin(left=-offset)
                            .width(spacing+offset)
                            .onClick { onNameClick(prof) }
                    )
                }
                nums.second.subList(0, 10).forEach {
                    Text(it.toString(), gridElementModifier)
                }
                Text(nums.first.toString(), gridElementModifier)
            }
    }
}

@Composable
fun searchDeptFormContent(
    //First 2 are needed for proper switch back to first upon reset
    //Maybe should include selectedSchool too but not practically necessary
    selectedDept: String,
    selectedCourse: String,
    selectedProf: String,
    schools: Collection<School>,
    depts: Collection<String>,
    courses: Collection<String>,
    profs: Collection<String>,
    onSelectSchool: (String) -> Unit,
    onSelectDept: (String) -> Unit,
    onSelectCourse: (String) -> Unit,
    onSelectProf: (String) -> Unit,
){
    val modifier2 = Modifier.fillMaxSize()
//            .backgroundColor(Color.chocolate)
    val modifier1 = Modifier//.backgroundColor(Color.palevioletred)
    val labelModifier = Modifier.fontWeight(FontWeight.Bold).padding(2.px,0.px)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("School",
            labelModifier.alignSelf(AlignSelf.Start),
        )

        CustomDropDown(
            selectModifier =
            modifier1.borderRadius(50.px)
                .fillMaxWidth()
                .background("#ddd"),
            optionModifier = modifier2,
            list = schools,
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
                    list = depts,
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
                    list = courses,
                    onSelect = onSelectCourse,
                    selected = selectedCourse,
                )
            }

            Column(
                Modifier.margin(topBottom = 5.px, leftRight = 25.px)
            ) {
                Text("Prof (Optional)", labelModifier)

                CustomDropDown(
                    selectModifier = Modifier.width(125.px),
                    list = profs,
                    onSelect = onSelectProf,
                    selected = selectedProf,
                )
            }
        }
    }
}