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
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomDropDown
import io.github.dennistsar.sirs_kobweb.data.Entry
import io.github.dennistsar.sirs_kobweb.data.School
import io.github.dennistsar.sirs_kobweb.logic.aveScores
import io.github.dennistsar.sirs_kobweb.logic.mapByCourses
import io.github.dennistsar.sirs_kobweb.logic.mapByProfs
import io.github.dennistsar.sirs_kobweb.logic.toProfScores
import io.github.dennistsar.sirs_kobweb.misc.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Page
@Composable
fun SearchDept() {
    val repository = Repository(Api())
    PageLayout("Search",Modifier.backgroundColor(Color.lightcyan)) {
        val ctx = rememberPageContext()
        val myCoroutineScope = rememberCoroutineScope()

        var schoolMap: Map<String, School> by remember{ mutableStateOf(emptyMap()) }
        var selectedSchool by remember { mutableStateOf(ctx.params["school"] ?: "") }
        var selectedDept by remember { mutableStateOf(ctx.params["dept"] ?: "") }
        var selectedCourse by remember { mutableStateOf(ctx.params["course"] ?: "") }
        var selectedProf by remember { mutableStateOf(ctx.params["prof"]?.decodeURLParam()?.uppercase() ?: "") }

        var deptEntries: List<Entry> by remember { mutableStateOf(emptyList()) }
        var mapOfProfs: Map<String,List<Entry>> by remember { mutableStateOf(emptyMap()) }
        var mapOfCourses: Map<String,List<Entry>>  by remember { mutableStateOf(emptyMap()) }
        var mapOfEntries: Map<String, Pair<Int, List<Double>>>  by remember { mutableStateOf(emptyMap()) }

        var profListLoading by remember { mutableStateOf(false) }

        val updateMapOfEntries : (String,Boolean) -> Unit = fun(ab,bool)
            {
                if(bool)
                    profListLoading = true
                val a =
                    if (ab.isBlankOrNone())
                        mapOfProfs
                    else
                        mapOfCourses[ab]!!.mapByProfs()
                myCoroutineScope.launch {
                    delay(50)
                    mapOfEntries = a.toProfScores().mapValues { it.value.toTotalAndAvesPair() }
                }
            }

        val updateSelectedDept: (String?,Boolean) -> Unit = fun(str,firstTime) {
            console.log("updating dept2: $str")
            if(!firstTime){
                profListLoading = true
                selectedCourse = NONE
                selectedProf = NONE
            }

            selectedDept = str ?: "".also { return } // Returns if str is null - kinda cool but a little weird
            myCoroutineScope.launch {
                deptEntries = repository.getEntries(selectedSchool, selectedDept)
                    .takeIf { it is Resource.Success }
                    ?.data
                    ?.filter { it.scores.size >= 100 } ?: emptyList()
                mapOfProfs = deptEntries.mapByProfs()
                mapOfCourses = deptEntries.mapByCourses()
                updateMapOfEntries(selectedCourse,false)

                if (!firstTime)
                    return@launch
                if (!mapOfProfs.keys.contains(selectedProf))
                    selectedProf = NONE
                if (!mapOfCourses.keys.contains(selectedProf))
                    selectedCourse = NONE
            }
        }

        LaunchedEffect(true){
            console.log("making req")
            schoolMap =
                repository.getSchoolMap()
                    .takeIf { it is Resource.Success }?.data ?: emptyMap()
            val school = schoolMap[selectedSchool] ?: schoolMap["01"]!!
            selectedSchool = school.code
            updateSelectedDept(
                selectedDept.takeIf { school.depts.contains(it) } ?: school.depts.first(),
                true
            )
        }

        if (schoolMap.isEmpty())
            return@PageLayout

        Box(Modifier
            .fillMaxWidth()
            .display(DisplayStyle.Flex)
            .alignItems(AlignItems.Center)// vertical alignment
        ) {
            Box(Modifier.flex(1))
            Box{
                searchDeptFormContent(
                    selectedSchool = schoolMap[selectedSchool]!!,
                    selectedDept = selectedDept,
                    selectedCourse = selectedCourse,
                    selectedProf = selectedProf,
                    schools = schoolMap.values,
                    depts = schoolMap[selectedSchool]!!.depts,
                    courses = listOf(NONE) + mapOfCourses.keys.sorted(),
                    profs = listOf(NONE) + mapOfProfs.keys.sorted(),
                    onSelectSchool =
                    {
                        selectedSchool = it
                        updateSelectedDept(schoolMap[selectedSchool]!!.depts.firstOrNull(),false)
                    },
                    onSelectDept = { updateSelectedDept(it,false) },
                    onSelectCourse =
                    {
                        updateMapOfEntries(it,it==NONE)
                        selectedCourse = it
                        selectedProf = NONE
                    },
                    onSelectProf =
                    {
                        if (selectedCourse!=NONE) {
                            selectedCourse = NONE
                            updateMapOfEntries(selectedCourse,false)
                        }
                        selectedProf = it
                    },
                )
            }
            Box(Modifier.flex(1)) loading@{
                if(!profListLoading)
                    return@loading
                Image(
                    "circle_loading.gif",
                    "Loading",
                    Modifier.size(75.px)
                )
            }
        }

        if (deptEntries.isEmpty())
            return@PageLayout

        if (!selectedProf.isBlankOrNone()) {
            Text(selectedProf)
            profSummary(mapOfProfs[selectedProf]!!)
            return@PageLayout
        }

        profScoresList(
            mapOfEntries,
        ) {
            console.log("hi")
            profListLoading = false
        }
    }
}

@Composable
fun profSummary(list: List<Entry>){
    val a = list.mapByCourses()
    val b = a.toProfScores()
    val allScores = list.aveScores()

    profScoresList(b.mapValues { it.value.toTotalAndAvesPair() })
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun profScoresList(
    list:  Map<String, Pair<Int, List<Double>>>,
    invisible: Boolean = false,
    onLoad: () -> Unit = {},
){
    Div(
        attrs = SimpleGridStyle
            .toModifier(gridVariant12)
            .styleModifier { if(invisible) display(DisplayStyle.None) }
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

        list.entries
            .sortedBy { -it.value.second[8] }
            .take(300)//for performance reasons
            .forEach { (prof, nums) ->
                Box(gridElementModifier){
                    val offset = 40.px
                    Text(
                        prof,
                        Modifier
                            .margin(left=-offset)
                            .width(spacing+offset)
                    )
                }
                nums.second.subList(0, 10).forEach {
                    Text(it.toString(), gridElementModifier)
                }
                Text(nums.first.toString(), gridElementModifier)
            }
        LaunchedEffect(list) {
            onLoad()
        }
    }
}

@Composable
fun searchDeptFormContent(
    selectedSchool: School,
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
            selected = selectedSchool,
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