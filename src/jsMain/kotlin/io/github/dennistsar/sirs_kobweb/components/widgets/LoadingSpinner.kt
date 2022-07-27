package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.silk.components.graphics.Image
import org.jetbrains.compose.web.css.px

@Composable
fun LoadingSpinner() =
    Image(
        "circle_loading.gif",
        "Loading",
        Modifier.size(75.px),
    )