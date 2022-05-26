package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.display
import com.varabyte.kobweb.compose.ui.modifiers.flex
import org.jetbrains.compose.web.css.DisplayStyle


@Composable
fun LeftRightCenterBox(
    modifier: Modifier = Modifier,
    left: @Composable () -> Unit = {},
    right: @Composable () -> Unit = {},
    center: @Composable () -> Unit = {},
){
    Box(modifier
        .display(DisplayStyle.Flex)
    ) {
        Box(Modifier.flex(1)){
            left()
        }
        Box{
            center()
        }
        Box(Modifier.flex(1)){
            right()
        }
    }
}