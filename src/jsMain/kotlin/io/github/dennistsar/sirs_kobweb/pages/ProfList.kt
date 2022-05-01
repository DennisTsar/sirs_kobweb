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
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.data.Entry
import io.github.dennistsar.sirs_kobweb.logic.getProfAves
import io.github.dennistsar.sirs_kobweb.misc.roundToDecimal
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px

@Page("proflist/{school}/{dept}")
@Composable
fun ProfList() {
    PageLayout("SEARCH") {
        val ctx = rememberPageContext()
        val school = ctx.params.getValue("school")
        val dept = ctx.params.getValue("dept")

        var entries by remember{ mutableStateOf<List<Entry>>(emptyList()) }
        var profRatings by remember{ mutableStateOf<Map<String, List<List<Int>>>>(emptyMap()) }

        remember {
            MainScope().launch {
                val k = Api().getEntriesFromGit(school,dept)
                console.log(k)
                entries = Json.decodeFromString(k)
                profRatings = getProfAves(entries)
                console.log("making req${entries.size}")
            }
            console.log(entries.size)
        }

        Column(
            Modifier
//                .scrollBehavior(ScrollBehavior.Smooth)
//                .scrollMargin(50.px)
                .height(500.px)
                .width(800.px)
                .overflowY(Overflow.Scroll)
        ) {
            profRatings.toList().sortedBy { -it.second[8].average() }
                .forEach { (name,scores) ->
                    BaseProfCard(name,scores)
                }
        }
    }
}

@Composable
fun BaseProfCard(name: String, scores: List<List<Int>>){
    Box(
        Modifier
            .fillMaxWidth()
//                            .width(500.px)
            .height(50.px)
            .backgroundColor(Color.lightcyan)
        //                        .padding(50.px)
    ){
        Row(
            Modifier.fillMaxWidth()
        ) {
            Text(
                name,
                Modifier.fillMaxWidth(20.percent)
                    .backgroundColor(Color.purple)
            )
            scores.forEach { nums ->
                var hover by remember { mutableStateOf(false) }
                var customtext by remember { mutableStateOf(nums.average().roundToDecimal(2).toString()) }
                Text(
                    customtext,
                    Modifier
                        .fillMaxWidth(7.percent)
                        .onMouseEnter {
                            hover = true
                            customtext = nums.size.toString()
                        }
                        .onMouseLeave {
                            hover = false
                            customtext = nums.average().roundToDecimal(2).toString()
                        }
                )
//                if(hover)
//                    Text("hi",Modifier.marginBlock(0.px,50.px))

            }
        }
    }
}
