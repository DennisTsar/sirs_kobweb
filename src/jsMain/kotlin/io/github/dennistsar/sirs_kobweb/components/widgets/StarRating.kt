package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.transform
import com.varabyte.kobweb.silk.components.icons.fa.FaStar
import com.varabyte.kobweb.silk.components.icons.fa.FaStarHalf
import com.varabyte.kobweb.silk.components.icons.fa.IconStyle
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import kotlin.math.roundToInt

@Composable
fun StarRating(
    rating: Double,
    yellow: CSSColorValue = Color("#FDCC0D"),
    gray: CSSColorValue = Color("#dbdbdf"),
    style: IconStyle = IconStyle.FILLED
){
    val yellowModifier = Modifier.color(yellow)
    val grayModifier = Modifier.color(gray)

    val a = (rating*2).roundToInt()/2.0
    Row {
        for (i in 1..5) {
            when {
                (i <= a) -> FaStar(yellowModifier, style)
                (i - .5 == a) -> HalfStarColored(yellowModifier, grayModifier, style)
                else -> FaStar(grayModifier, style)
            }
        }
    }
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun HalfStarColored(yellowModifier: Modifier, grayModifier: Modifier, style: IconStyle = IconStyle.FILLED){
    val len = (-1.0/16).px
    Box {
        FaStarHalf(Modifier.margin(right = len).then(yellowModifier), style)
        FaStarHalf(Modifier.margin(left = len).transform { scaleX(-1) }.then(grayModifier), style)
    }
}