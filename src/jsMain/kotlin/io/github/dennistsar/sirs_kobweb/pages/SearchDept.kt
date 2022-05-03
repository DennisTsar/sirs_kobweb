package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.sections.CustomDropDown
import io.github.dennistsar.sirs_kobweb.components.sections.CustomForm
import io.github.dennistsar.sirs_kobweb.data.School
import io.github.dennistsar.sirs_kobweb.misc.Resource
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.TextInput

@Page
@Composable
fun SearchDept() {
    val repository = Repository(Api())
    PageLayout("Search") {
        val ctx = rememberPageContext()
        var map by remember { mutableStateOf<Map<String, School>>(emptyMap()) }
        var selectedSchool by remember { mutableStateOf("01") }
        var selectedDept by remember { mutableStateOf("") }

        var courseText by remember { mutableStateOf("") }

        LaunchedEffect(""){
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

        val modifier2 = Modifier.fillMaxSize()
            .backgroundColor(Color.chocolate)
        val modifier1 = Modifier.backgroundColor(Color.palevioletred)

        CustomForm(
            Modifier
                .backgroundColor(Color.red)
                .borderRadius(10.px),
            {
                val link =
                    if (courseText.isBlank())
                        "/proflist/${selectedSchool}/${selectedDept}"
                    else
                        "/coursestats/${selectedSchool}/${selectedDept}/${courseText}"
                ctx.router.routeTo("/sirs_kobweb$link")
            }
        ){ submitComposable ->
            Column(
                Modifier.alignItems(AlignItems.Center).rowGap(5.px)
            ) {
                CustomDropDown(
                    selectModifier = modifier1,
                    optionModifier = modifier2,
                    list = map.values.toList(),
                    onSelect = { selectedSchool = it },
                    getText = { "${it.code} -${it.name}" },
                    getValue = { it.code }
                )
                CustomDropDown(
                    selectModifier = modifier1,
                    optionModifier = modifier2,
                    list = map[selectedSchool]?.depts,
                    onSelect = { selectedDept = it },
                    getText = { it }
                )
                TextInput(courseText) {
                    onInput { courseText = it.value }
                }
                submitComposable()
            }
        }
    }
}