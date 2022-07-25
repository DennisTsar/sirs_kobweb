package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P

@Page//("/sirs_kobweb/searchdept")
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb 2.0!") {
        SpanText("Please enter your name")
        var name by remember { mutableStateOf("") }
        Input(
            InputType.Text,
            attrs = {
                onInput { e -> name = e.value }
            }
        )
        P()
        SpanText("Hello ${name.takeIf { it.isNotBlank() } ?: "World"}!")
    }

}