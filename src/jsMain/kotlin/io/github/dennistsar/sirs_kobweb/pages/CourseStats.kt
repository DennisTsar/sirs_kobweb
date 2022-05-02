package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.data.Entry
import io.github.dennistsar.sirs_kobweb.logic.getCourseAvesByProf
import io.github.dennistsar.sirs_kobweb.logic.getProfAves
import io.github.dennistsar.sirs_kobweb.misc.roundToDecimal
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
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

        remember {
            MainScope().launch {
                val k = Api().getEntriesFromGit(school,dept)
                console.log(k)
                entries = repository.getEntries(school, dept).data ?: emptyList()
                mapOfCourses = getCourseAvesByProf(entries)
                    .mapValues {(_,v) ->
                        v.mapValues { it.value[8].average() }
                    }
//                    .mapKeys { it.key.split(":")[2] }
                console.log("making req${entries.size}")
            }
            console.log(entries.size)
        }

        Column(
            Modifier
//                .scrollBehavior(ScrollBehavior.Smooth)
//                .scrollMargin(50.px)
                .height(200.px)//500
                .width(800.px)
                .overflowY(Overflow.Scroll)
        ) {
            console.log(mapOfCourses.keys)
            console.log(mapOfCourses["211"]?.keys)
            mapOfCourses[course]?.toList()
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
