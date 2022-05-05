package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomDropDown
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomForm
import io.github.dennistsar.sirs_kobweb.data.Entry
import io.github.dennistsar.sirs_kobweb.data.School
import io.github.dennistsar.sirs_kobweb.logic.getCourseAvesByProf
import io.github.dennistsar.sirs_kobweb.misc.Resource
import io.github.dennistsar.sirs_kobweb.misc.TenQsShortened
import io.github.dennistsar.sirs_kobweb.misc.gridVariant11
import io.github.dennistsar.sirs_kobweb.misc.roundToDecimal
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.deg
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div

@OptIn(ExperimentalComposeWebApi::class)
@Page
@Composable
fun SearchDept() {
    val repository = Repository(Api())
    PageLayout("Search",Modifier.backgroundColor(Color.lightcyan)) {
        val ctx = rememberPageContext()
        val coroutineScope = rememberCoroutineScope()
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
            courseList = listOf("None") + getCourseAvesByProf(deptEntries).keys
        }

        if(schoolMap.isEmpty())
            return@PageLayout

        CustomForm(
            Modifier
//                .backgroundColor(Color.red)
                .borderRadius(10.px),
            {
                val schoolDeptStr = "${selectedSchool}/${selectedDept}"
                val link =
                    if (selectedCourse.isBlank() || selectedCourse=="None")
                        "proflist/$schoolDeptStr"
                    else
                        "coursestats/$schoolDeptStr/$selectedCourse"
                ctx.router.routeTo("/sirs_kobweb/$link")//Ideally route prefix gets removed or is at least a var
            }
        ){ submitComposable ->
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
                submitComposable = submitComposable,
            )
        }

        if(selectedCourse.isBlank() || selectedCourse=="None")
            return@PageLayout


        Div(
            attrs = SimpleGridStyle
                .toModifier(gridVariant11)
                .asAttributesBuilder()
        ) {
            TenQsShortened.forEach {
                Text(
                    it,
                    Modifier.width(100.px)
                        .padding(10.px)
                        .transform {
                            rotate((-30).deg)
                        }
                )
            }
        }
//        SimpleGrid(numColumns(11), variant = gridVariant11){
//
//        }

        val mapOfCourses = getCourseAvesByProf(deptEntries.filter { it.scores.size>=80 })
            .mapValues { (_,v) ->
                v.mapValues { it.value[8].average() }
            }
        Column(
            Modifier
//                .scrollBehavior(ScrollBehavior.Smooth)
//                .scrollMargin(50.px)
                .height(500.px)//
                .width(800.px)
//                .overflowY(Overflow.Scroll)
        ) {
            mapOfCourses[selectedCourse]?.toList()
                ?.sortedBy { -it.second }
                ?.forEach { (k,v) ->
                    Box(
                        Modifier.fillMaxWidth()
                            .height(50.px)
                            .backgroundColor(Color.lightcyan)
                    ) {
                        Text("$k: ${v.roundToDecimal(2)}")
                    }
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
    submitComposable: @Composable () -> Unit,
){
    Column(
        Modifier.alignItems(AlignItems.Center).rowGap(5.px)
    ) {
        val modifier2 = Modifier.fillMaxSize()
//            .backgroundColor(Color.chocolate)
        val modifier1 = Modifier//.backgroundColor(Color.palevioletred)

        CustomDropDown(
            selectModifier = modifier1.borderRadius(50.px),
            optionModifier = modifier2,
            list = schoolList,
            onSelect = onSelectSchool,
            getText = { "${it.code} - ${it.name}" },
            getValue = { it.code },
//            selected = schoolList.firstOrNull()
        )
        CustomDropDown(
            selectModifier = modifier1,
            optionModifier = modifier2,
            list = deptList,
            onSelect = onSelectDept,
            selected = selectedDept
        )
        CustomDropDown(
            selectModifier = modifier1,
            optionModifier = modifier2,
            list = courseList,
            onSelect = onSelectCourse,
            selected = selectedCourse
        )
        submitComposable()
    }
}