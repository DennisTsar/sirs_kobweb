package io.github.dennistsar.sirs_kobweb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.InitSilk
import com.varabyte.kobweb.silk.InitSilkContext
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.getColorMode
import com.varabyte.kobweb.silk.theme.registerBaseStyle
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.vh

private const val COLOR_MODE_KEY = "sirs_kobweb:colorMode"

@InitSilk
fun updateTheme(ctx: InitSilkContext) {
    ctx.config.initialColorMode = localStorage.getItem(COLOR_MODE_KEY)?.let { ColorMode.valueOf(it) } ?: ColorMode.LIGHT

    ctx.config.registerBaseStyle("body") {
        Modifier.fontFamily(
            "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "Oxygen", "Ubuntu",
            "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", "sans-serif"
        )
    }
//    ctx.config.registerStyle("Select"){
//        base{
//            Modifier.fontFamily("Segoe UI")
//                .padding(left=5.px)
////                .position(Position.Relative)
////                .backgroundColor(Color.transparent)
////                .display(DisplayStyle.InlineBlock)
////                .cursor(Cursor.Pointer)
//        }
//        hover{
//            Modifier.background("transparent")
//        }
////        after{
////            Modifier.backgroundColor(Color.green)
////        }
//        active{
//            Modifier.backgroundColor(Color.yellow)
//        }
//
//    }
//
//    ctx.config.registerStyle("Option"){
//        base {
//            Modifier
//                .position(Position.Relative)
//                .display(DisplayStyle.Block)
//                .padding(0.px,22.px)
//                .borderBottom("1px solid #b5b5b5")
//                .color("#b5b5b5")
//                .lineHeight(47.px)
//                .cursor(Cursor.Pointer)
//                .transition("all .4s ease-in-out")
////                .backgroundColor(Color.green)
////                .listStyle("none")
////                .display(DisplayStyle.Block)
////                .position(Position.Absolute)
////                .border("1px solid #b5b5b5")
////                .borderRadius(4.px)
////                .boxShadow("0 2px 1px rgba(0,0,0,.07)")
////                .background("#fff")
////                .opacity(0)
//        }
//
//        hover{
//            Modifier.backgroundColor(Color.darkgreen)
//        }
//    }
}

@App
@Composable
fun MyApp(content: @Composable () -> Unit) {
    SilkApp {
        val colorMode = getColorMode()
        remember(colorMode) {
            localStorage.setItem(COLOR_MODE_KEY, colorMode.name)
        }

        Surface(Modifier.minHeight(100.vh)) {
            content()
        }
    }
}
