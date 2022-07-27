package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.div
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.times

@Composable
fun BarGraph(
    ratings: List<Int>,
    labels: Pair<String,String>,
    max: Int = ratings.maxOrNull() ?: 0,
    height: Double = 130.0,
    colWidth: Double = 36.0,
) {
    Box {
        Row(
            // don't really like this but idk how else to extend bounds to end of "Excellent"
            Modifier.padding(leftRight = 15.px)
        ) {
            ratings.forEachIndexed { index, num ->
                Column(
                    Modifier.width(colWidth.px),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        Modifier.height(height.px),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.flex(1)) // pushes everything down
                        SpanText(num.toString())
                        Box(
                            Modifier
                                .width(28.px)
                                .height(num.px * (height) / max)
                                .backgroundColor(Color.purple)
                        )
                    }
                    when (index) {
                        0 -> labels.first
                        4 -> labels.second
                        else -> null
                    }?.let { SpanText(it) } // possibly add rotation to this
                }
            }
        }
    }
}

