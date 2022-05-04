package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomDropDown
import io.github.dennistsar.sirs_kobweb.data.Entry
import io.github.dennistsar.sirs_kobweb.logic.getCourseAvesByProf
import io.github.dennistsar.sirs_kobweb.misc.roundToDecimal
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px

@Page("coursestats/{school}/{dept}/{course}")
@Composable
fun CourseStats() {
    val repository = Repository(Api())
    PageLayout("SEARCH") {
        val ctx = rememberPageContext()
        val school = ctx.params.getValue("school")
        val dept = ctx.params.getValue("dept")
        val course = ctx.params.getValue("course")

        var entries: List<Entry> by remember{ mutableStateOf(emptyList()) }
        var mapOfCourses: Map<String,Map<String,Double>> by remember { mutableStateOf(emptyMap()) }

        var selectedCourse by remember { mutableStateOf(course) }

        LaunchedEffect(true){
            entries = repository.getEntries(school, dept).data?.filter { it.scores.size>=80 }
//                    ?.filter { it.courseName.contains("Lecture") || it.indexNum }
                ?: emptyList()
            mapOfCourses = getCourseAvesByProf(entries)
                .mapValues { (_,v) ->
                    v.mapValues { it.value[8].average() }
                }
//                    .mapKeys { it.key.split(":")[2] }
            console.log("making req${entries.size}")
        }

        CustomDropDown(
            list = mapOfCourses.keys,
            onSelect = { selectedCourse = it}
        )

        Column(
            Modifier
//                .scrollBehavior(ScrollBehavior.Smooth)
//                .scrollMargin(50.px)
                .height(200.px)//500
                .width(800.px)
//                .overflowY(Overflow.Scroll)
        ) {
            console.log(mapOfCourses.keys)
            console.log(mapOfCourses["211"]?.keys)

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
