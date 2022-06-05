package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.navigation.UpdateHistoryMode
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.widgets.CustomDropDown
import io.github.dennistsar.sirs_kobweb.components.widgets.LeftRightCenterBox
import io.github.dennistsar.sirs_kobweb.data.api.Api
import io.github.dennistsar.sirs_kobweb.data.api.Repository
import io.github.dennistsar.sirs_kobweb.data.aveScores
import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.data.mapByCourses
import io.github.dennistsar.sirs_kobweb.data.toProfScores
import io.github.dennistsar.sirs_kobweb.misc.TenQsShortened
import io.github.dennistsar.sirs_kobweb.misc.decodeURLParam
import io.github.dennistsar.sirs_kobweb.misc.gridVariant12
import io.github.dennistsar.sirs_kobweb.misc.toTotalAndAvesPair
import io.github.dennistsar.sirs_kobweb.states.DropDownState
import io.github.dennistsar.sirs_kobweb.states.SearchDeptState
import io.github.dennistsar.sirs_kobweb.states.SearchDeptStateImpl
import io.github.dennistsar.sirs_kobweb.states.Status
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import kotlin.reflect.KMutableProperty0

@Page
@Composable
fun SearchDept() {
    val repository = Repository(Api())
    PageLayout("Search",Modifier.backgroundColor(Color.lightcyan)) {
        val ctx = rememberPageContext()
        val myCoroutineScope = rememberCoroutineScope()

        val state = remember {
            SearchDeptStateImpl(
                repository = repository,
                coroutineScope = myCoroutineScope,
                initialSchool = ctx.params["school"],
                initialDept = ctx.params["dept"],
                initialCourse = ctx.params["course"],
                initialProf = ctx.params["prof"]?.decodeURLParam()?.uppercase(),
            )
        }

        remember(state.url) {
            ctx.router.navigateTo(state.url, UpdateHistoryMode.REPLACE)
        }

        val status = state.status

        LeftRightCenterBox(
            Modifier
                .fillMaxWidth()
                .alignItems(AlignItems.Center),// vertical alignment
            right = {
                if(state.profListLoading || status == Status.InitialLoading)
                    Image(
                        "circle_loading.gif",
                        "Loading",
                        Modifier.size(75.px),
                    )
            },
            center = { if(status != Status.InitialLoading) SearchDeptFormContent(state) }
        )

        // This logic is kept here as opposed to in State class for performance reasons
        // Prevents having to reload HTML when status changes back to previously used one - it's already loaded
        val finishLoading = { state.profListLoading = false }
        when(status) {
            Status.Prof -> {
                Text(state.profState.selected)
                ProfSummary(state.profEntries,finishLoading)
            }
            Status.Course -> ProfScoresList(state.courseSpecificMap, finishLoading)
            Status.Dept -> ProfScoresList(state.wholeDeptMap, finishLoading)
            else -> {}
        }
    }
}

@Composable
fun ProfSummary(
    list: List<Entry>,
    onLoad: () -> Unit = {},
) {
    val a = list.mapByCourses()
    val b = a.toProfScores()
    val allScores = list.aveScores()

    ProfScoresList(b.mapValues { it.value.toTotalAndAvesPair() },onLoad)
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun ProfScoresList(
    list:  Map<String, Pair<Int, List<Double>>>,
    onLoad: () -> Unit = {},
) {
    Div(
        attrs = SimpleGridStyle
            .toModifier(gridVariant12)
            .asAttributesBuilder()
    ) {
        val spacing = 80.px
        val fontSize = 15.px
        (listOf("")+TenQsShortened+"Total # of Responses").forEach {
            Box(
                Modifier.width(spacing)
            ) {
                Text(
                    it,
                    Modifier
                        .width(175.px)
                        .transform { rotate((-45).deg) }
                        .fontSize(fontSize)
                        .margin(topBottom = 50.px, leftRight = (-18).px)
                        .textDecorationLine(TextDecorationLine.Underline)
                )
            }
        }

        val gridElementModifier =
            Modifier.width(spacing)
                .fontSize(fontSize)
                .margin(topBottom = 7.5.px, leftRight = 0.px)
                .alignSelf(AlignSelf.Center)

        list.entries
            .sortedBy { -it.value.second[8] }
            .take(300)//for performance reasons
            .forEach { (prof, nums) ->
                Box(gridElementModifier) {
                    val offset = 40.px
                    Text(
                        prof,
                        Modifier
                            .margin(left=-offset)
                            .width(spacing+offset)
                    )
                }
                nums.second.subList(0, 10).forEach {
                    Text(it.toString(), gridElementModifier)
                }
                Text(nums.first.toString(), gridElementModifier)
            }
        remember(list) { onLoad() }
    }
}

@Composable
fun SearchDeptFormContent(state: SearchDeptState) {
    val modifier2 = Modifier.fillMaxSize()
//            .backgroundColor(Color.chocolate)
    val modifier1 = Modifier//.backgroundColor(Color.palevioletred)
    val labelModifier = Modifier.fontWeight(FontWeight.Bold).padding(2.px,0.px)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("School",
            labelModifier.alignSelf(AlignSelf.Start),
        )

        state::schoolState.run {
            CustomDropDown(
                selectModifier = modifier1
                    .borderRadius(50.px)
                    .fillMaxWidth()
                    .background("#ddd"),
                optionModifier = modifier2,
                list = get().list,
                onSelect = { set(get().copy(selected = it)) },
                getText = { "${it.code} - ${it.name}" },
                getValue = { it.code },
                selected = get().list.first { it.code==get().selected },
            )
        }

        val secondRowModifier = Modifier.margin(topBottom = 5.px, leftRight = 25.px)

        Row(
            Modifier.alignContent(AlignContent.SpaceEvenly)
        ) {
            Column(secondRowModifier) {
                Text("Department", labelModifier)

                ReflectiveCustomDropDown(
                    property = state::deptState,
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                )
            }

            Column(secondRowModifier) {
                Text("Course (Optional)", labelModifier)

                ReflectiveCustomDropDown(
                    property = state::courseState,
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                )
            }

            Column(secondRowModifier) {
                Text("Prof (Optional)", labelModifier)

                ReflectiveCustomDropDown(
                    property = state::profState,
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                )
            }
        }
    }
}
@Composable
fun ReflectiveCustomDropDown(
    property: KMutableProperty0<DropDownState<String>>,
    selectModifier: Modifier = Modifier,
    optionModifier: Modifier = Modifier,
    getText: (String) -> String = {it},
    getValue: (String) -> String = getText,
) {
    with(property) {
        CustomDropDown(
            selectModifier = selectModifier,
            optionModifier = optionModifier,
            list = get().list,
            onSelect = { set(get().copy(selected = it)) },
            getText = getText,
            getValue = getValue,
            selected = get().selected,
        )
    }
}