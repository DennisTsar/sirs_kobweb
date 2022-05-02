package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import androidx.compose.web.attributes.SelectAttrsBuilder
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.textDecorationLine
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.sections.CustomDropDown
import io.github.dennistsar.sirs_kobweb.data.School
import io.github.dennistsar.sirs_kobweb.misc.Resource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.dom.*

@Page
@Composable
fun SearchDept() {
    val repository = Repository(Api())
    PageLayout("Search") {
        var map by remember { mutableStateOf<Map<String, School>>(emptyMap()) }
        var selectedSchool by remember { mutableStateOf("01") }
        var selectedDept by remember { mutableStateOf("") }

        var courseText by remember { mutableStateOf("") }

        remember {
            MainScope().launch {
                console.log("making req")
                repository.getSchoolMap()
                    .takeIf { it is Resource.Success }
                    ?.run {
                        data?.let {
                            map = it
                            selectedDept = map[selectedSchool]?.depts?.firstOrNull() ?: ""
                        }
                    }

            }
        }
        val modifier2 = Modifier.fillMaxSize()
            .backgroundColor(Color.chocolate)
        val modifier1 = Modifier.backgroundColor(Color.palevioletred)

        CustomDropDown(
            selectModifier = modifier1,
            optionModifier = modifier2,
            list = map.values.toList(),
            onSelect = { selectedSchool = it },
            getText = {"${it.code} -${it.name}"},
            getValue = {it.code}
        )
        CustomDropDown(
            selectModifier = modifier1,
            optionModifier = modifier2,
            list = map[selectedSchool]?.depts,
            onSelect = { selectedDept = it },
            getText = {it}
        )
        Input(
            InputType.Text
        ) {
            onChange {// only runs after clicking off text
                courseText = it.value
            }
        }
        val link =
            if(courseText.isBlank())
                "/proflist/${selectedSchool}/${selectedDept}"
            else
                "/coursestats/${selectedSchool}/${selectedDept}/${courseText}"
        Link(link,"Submit"){
            console.log("recomposed link")
        }

    }
}