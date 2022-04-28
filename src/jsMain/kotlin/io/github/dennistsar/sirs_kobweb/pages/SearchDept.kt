package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.data.School
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun SearchDept() {
    PageLayout("SEARCH") {
        var schoolMap by remember{ mutableStateOf<Map<String, School>>(emptyMap()) }
        remember {
            MainScope().launch {
                schoolMap = Json.decodeFromString(Api().getSchoolDeptsMapFromGit())
                console.log("making req")
            }
        }
        SelectTest1(schoolMap)
    }
}

@Composable
fun SelectTest1(schoolMap: Map<String, School>){
     var selectedSchool by remember{ mutableStateOf("") }
    var selectedDept by remember{ mutableStateOf("") }


    Select(
        {
            onChange {
                selectedSchool = it.value ?: "ERROR"
                console.log(it.value)
            }
        }
    ) {
        schoolMap.values.forEach {
            Option(it.code,
                Modifier.fillMaxSize()
                    .color(Color.blue)
                    .backgroundColor(Color.green)
                    .onClick { _ ->
                        selectedSchool = it.toString()
                        console.log(selectedSchool)
                    }
                    .asAttributesBuilder()
            ) {
                Text("${it.code} -${it.name}")
            }
        }
    }
    Select(
        {
            onChange {
                selectedDept = it.value ?: "ERROR"
            }
        }
    ) {
        schoolMap[selectedSchool]?.depts?.forEach {
            Option(it,
                Modifier.fillMaxSize()
                    .color(Color.chocolate)
                    .backgroundColor(Color.lightgray)
                    .onClick { _ ->
                        selectedDept = it
                        console.log(selectedSchool)
                    }
                    .asAttributesBuilder()
            ) {
                Text(it)
            }
        }
    }
    Link("/proflist/$selectedSchool/$selectedDept","Submit")
//    SubmitInput{
//        Modifier.onClick {
//
//        }
//    }
}