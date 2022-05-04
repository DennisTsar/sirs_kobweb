package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import io.github.dennistsar.sirs_kobweb.api.Api
import io.github.dennistsar.sirs_kobweb.api.Repository
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomDropDown
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomForm
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
    PageLayout("Search",Modifier.backgroundColor(Color.lightcyan)) {
        val ctx = rememberPageContext()
        var schoolMap by remember{ mutableStateOf<Map<String, School>>(emptyMap()) }
        var selectedSchool by remember { mutableStateOf("") }
        var selectedDept by remember { mutableStateOf("") }

        var courseText by remember { mutableStateOf("") }

        LaunchedEffect(true){
            console.log("making req")
            repository.getSchoolMap()
                .takeIf { it is Resource.Success }
                ?.run {
                    data?.let {
                        schoolMap = it
                        val (code,school) = it.entries.first()
                        selectedSchool = code
                        selectedDept = school.depts.firstOrNull() ?: ""
                    }
                }
        }
        if(schoolMap.isEmpty())
            return@PageLayout

        val modifier2 = Modifier.fillMaxSize()
//            .backgroundColor(Color.chocolate)
        val modifier1 = Modifier//.backgroundColor(Color.palevioletred)

        CustomForm(
            Modifier
//                .backgroundColor(Color.red)
                .borderRadius(10.px),
            {
                val schoolDeptStr = "${selectedSchool}/${selectedDept}"
                val link =
                    if (courseText.isBlank())
                        "proflist/$schoolDeptStr"
                    else
                        "coursestats/$schoolDeptStr/${courseText}"
                ctx.router.routeTo("/sirs_kobweb/$link")//Ideally route prefix gets removed or is at least a var
            }
        ){ submitComposable ->
            Column(
                Modifier.alignItems(AlignItems.Center).rowGap(5.px)
            ) {
                CustomDropDown(
                    selectModifier = modifier1.borderRadius(50.px),
                    optionModifier = modifier2,
                    list = schoolMap.values,
                    onSelect =
                    {
                        selectedSchool = it
                        selectedDept = schoolMap[selectedSchool]?.depts?.firstOrNull() ?: ""
                    },
                    getText = { "${it.code} - ${it.name}" },
                    getValue = { it.code }
                )
                CustomDropDown(
                    selectModifier = modifier1,
                    optionModifier = modifier2,
                    list = schoolMap[selectedSchool]?.depts,
                    onSelect = { selectedDept = it },
                )
                TextInput(courseText,
                    Modifier.borderRadius(5.px)
                        .backgroundColor(Color.green)
                        .boxShadow("1px 2px 10px grey")
                        .asAttributesBuilder{
                            onInput { courseText = it.value }
                        }
                )
                submitComposable()
            }
        }
    }
}