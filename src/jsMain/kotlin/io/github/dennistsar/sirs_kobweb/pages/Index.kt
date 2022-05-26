package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.data.api.Api
import io.github.dennistsar.sirs_kobweb.data.classes.School
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P

@Page//("/sirs_kobweb/searchdept")
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb 2.0!") {
        var depts by remember{ mutableStateOf<Map<String, School>>(emptyMap()) }
        remember {
            MainScope().launch {
                depts = Json.decodeFromString(Api().getSchoolDeptsMapFromGit())
                console.log("making req")
            }
        }
        val messages = listOf("a","b","c","d")
        console.log("recompsed")
        console.log(depts["01"])

        Text("Please enter your name")
        var name by remember { mutableStateOf("") }
        Input(
            InputType.Text,
            attrs = {
                onInput { e -> name = e.value }
            }
        )
        P()
        Text("Hello ${name.takeIf { it.isNotBlank() } ?: "World"}!")
        Column {
            messages.forEach { message ->
                Text(modifier = Modifier.fillMaxWidth(), text = message)
            }
        }
//        Text(depts.substring(0..10))
        val k = remember { mutableStateOf(messages) }


    }

}