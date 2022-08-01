package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextDecorationLine
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
//import com.varabyte.kobweb.silk.ui.thenIf
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import io.github.dennistsar.sirs_kobweb.components.widgets.*
import io.github.dennistsar.sirs_kobweb.data.api.Api
import io.github.dennistsar.sirs_kobweb.data.api.Repository
import io.github.dennistsar.sirs_kobweb.data.aveScores
import io.github.dennistsar.sirs_kobweb.data.classes.Entry
import io.github.dennistsar.sirs_kobweb.data.mapByCourses
import io.github.dennistsar.sirs_kobweb.data.toProfScores
import io.github.dennistsar.sirs_kobweb.data.toTotalAndAvesPair
import io.github.dennistsar.sirs_kobweb.misc.*
import io.github.dennistsar.sirs_kobweb.states.SearchDeptViewModel
import io.github.dennistsar.sirs_kobweb.states.Status
import kotlinx.browser.window
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Page
@Composable
fun SearchDept() {
    PageLayout("Search",Modifier.backgroundColor(Color.lightcyan)) {
        //region non-UI setuo
        val ctx = rememberPageContext()
        val coroutineScope = rememberCoroutineScope()
        val originalOnPopState = remember { window.onpopstate } // need to use this to avoid infinite recursion
        val repository = remember { Repository(Api()) }

        val viewModel = remember {
            SearchDeptViewModel(
                repository = repository,
                coroutineScope = coroutineScope,
                initialSchool = ctx.params["school"],
                initialDept = ctx.params["dept"],
                initialCourse = ctx.params["course"],
                initialProf = ctx.params["prof"]?.decodeURLParam()?.uppercase(),
            )
        }

        DisposableEffect(true){
            window.onpopstate = {
                originalOnPopState?.run { invoke(it) } // keeps default behavior when going back to other page
                viewModel.onPopState(window.location.search)
            }
            onDispose {
                window.onpopstate = originalOnPopState
            }
        }

        val status = viewModel.status

        remember(viewModel.url) {
            if (status != Status.InitialLoading)
                ctx.router.routeTo(viewModel.url)
        }
        // endregion

        LeftRightCenterBox(
            Modifier
                .fillMaxWidth()
                .alignItems(AlignItems.Center),// vertical alignment
            right = {
                if (viewModel.profListLoading && status != Status.InitialLoading)
                    LoadingSpinner()
            },
            center = {
                if (status != Status.InitialLoading)
                    SearchDeptFormContent(viewModel)
                else
                    LoadingSpinner()
            }
        )

        // This logic is kept here as opposed to in State class for performance reasons
        // Prevents having to reload HTML when status changes back to previously used one - it's already loaded
        val finishLoading = { viewModel.profListLoading = false }
        when(status) {
            Status.Prof -> {
                SpanText(viewModel.state.profState.selected)
                ProfSummary(
                    viewModel.selectedProfEntries,
                    viewModel.applicableCourseAves,
                    { viewModel.onSelectCourse(it) },
                    finishLoading,
                )
            }
            Status.Course ->
                ProfScoresList(
                    viewModel.scoresByProfForCourse,
                    { viewModel.onSelectProf(it) },
                    finishLoading,
                )
            Status.Dept ->
                ProfScoresList(
                    viewModel.scoresByProf,
                    { viewModel.onSelectProf(it) },
                    finishLoading,
                )
            else -> {}
        }
    }
}

@Composable
fun ProfSummary(
    entries: List<Entry>,
    applicableCourseAves: Map<String, List<Double>>,
    onNameClick: (String) -> Unit = {},
    onLoad: () -> Unit = {},
) {
    val a = entries.mapByCourses()
    val b = a.toProfScores("Overall")
    val allScores = entries.aveScores()

    // this will obviously have to be moved to state object eventually
    var selectedQ by remember{ mutableStateOf(8) }
    CustomDropDown(
        list = TenQsShortened,
        onSelect = { selectedQ = TenQsShortened.indexOf(it) },
        selected = TenQsShortened[selectedQ]
    )

    Row(
        Modifier
            .margin(topBottom = 30.px)
            .justifyContent(JustifyContent.Center),
        verticalAlignment = Alignment.CenterVertically,
    ) {// for future reference: "?school=01&dept=640&prof=TUNNELL%2C%20J" has a lot of graphs
        b.entries
            .sortedBy { it.key }
            .forEach { (courseName, scores) ->
                // for each question, list of length 5 corresponding to number of rating 1-5
                val scoresCount = scores[selectedQ].groupingBy { it }.eachCount()
                val ratings = (1..5).map { scoresCount[it] ?: 0 }

                Column(
                    Modifier.margin(topBottom = 10.px, leftRight = 15.px),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val selectedScore = scores[selectedQ].average()
                    val courseAve = applicableCourseAves[courseName]?.get(selectedQ)

                    SpanText(courseName)
                    BarGraph(
                        ratings,
                        if (selectedQ<8)
                            Pair("Strongly Agree", "Strongly Disagree")
                        else
                            Pair("Poor", "Excellent")
                    )
                    SpanText(
                        "Prof Average",
                        courseAve?.let {
                            Modifier.color(
                                if (selectedScore>=it) Color.green else Color.red
                            )
                        } ?: Modifier
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StarRating(selectedScore)
                        SpanText(selectedScore.roundToDecimal(2).toString(), Modifier.margin(left = 5.px))
                    }
                    courseAve?.let{
                        SpanText("Course Average")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRating(it)
                            SpanText(it.roundToDecimal(2).toString(), Modifier.margin(left = 5.px))
                        }
                    }
                }
            }
    }

    ProfScoresList(
        b.mapValues { it.value.toTotalAndAvesPair() },
        onNameClick,
        onLoad,
    )
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun ProfScoresList(
    list:  Map<String, Pair<Int, List<Double>>>,
    onNameClick: (String) -> Unit = {},
    onLoad: () -> Unit = {},
) {
    var z by remember{ mutableStateOf(8) }
    Div(
        attrs = SimpleGridStyle
            .toModifier(gridVariant12)
            .asAttributesBuilder()
    ) {
        val spacing = 80.px
        val fontSize = 15.px

        val underlineTextModifier = underlineOnHoverStyle.toModifier()
        (TenQsShortened.plusElementAtStart("") + "Total # of Responses").forEachIndexed { index, text ->
            Box(
                Modifier.width(spacing)
            ) {
                SpanText(
                    text,
                    Modifier
                        .width(175.px)
                        .transform { rotate((-45).deg) }
                        .fontSize(fontSize)
                        .margin(topBottom = 50.px, leftRight = (-18).px)
                        .textDecorationLine(TextDecorationLine.Underline)
                        .thenIf(index - 1 == z, Modifier.fontWeight(FontWeight.Bold))
                        .thenIf(index != 0 ) {
                            Modifier
                                .thenIf(index - 1 in TenQs.indices) { Modifier.title(TenQs[index - 1]) }
                                .onClick { z = index - 1 }
                                .cursor(Cursor.Pointer)
//                                .then(underlineTextModifier)
                        }
                )
            }
        }

        val gridElementModifier =
            Modifier.width(spacing)
                .fontSize(fontSize)
                .margin(topBottom = 7.5.px)
                .alignSelf(AlignSelf.Center)

        list.entries
            .sortedBy { if (z<10) -it.value.second[z] else -it.value.first.toDouble()  }
            .take(300)//for performance reasons
            .forEach { (prof, nums) ->
                Box(gridElementModifier) {
                    val offset = 40.px
                    val extraModifier =
                        if (!listOf("Average","Overall").contains(prof))
                            Modifier
                                .onClick { onNameClick(prof) }
                                .then(underlineTextModifier)
                        else
                            Modifier.fontWeight(FontWeight.Bold)
                    SpanText(
                        prof,
                        Modifier
                            .margin(left=-offset)
                            .width(spacing + offset)
                            .then(extraModifier)
                    )
                }
                nums.second.subList(0, 10).forEach {
                    SpanText(it.toString(), gridElementModifier)
                }
                SpanText(nums.first.toString(), gridElementModifier)
            }
        remember(list) { onLoad() } // since loading only happens when list is changed
    }
}

@Composable
fun SearchDeptFormContent(viewModel: SearchDeptViewModel) {
    val modifier2 = Modifier.fillMaxSize()
//            .backgroundColor(Color.chocolate)
    val modifier1 = Modifier//.backgroundColor(Color.palevioletred)
    val labelModifier = Modifier.fontWeight(FontWeight.Bold).padding(leftRight = 2.px)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpanText("School",
            labelModifier.alignSelf(AlignSelf.Start),
        )

        with(viewModel.state.schoolState) {
            CustomDropDown(
                list = list,
                onSelect = viewModel::onSelectSchool,
                selectModifier = modifier1
                    .borderRadius(50.px)
                    .fillMaxWidth()
                    .background("#ddd"),
                optionModifier = modifier2,
                getText = { "${it.code} - ${it.name}" },
                getValue = { it.code },
                selected = list.first { it.code==selected },
            )
        }

        val secondRowModifier = Modifier.margin(topBottom = 5.px, leftRight = 25.px)

        Row(
            Modifier.alignContent(AlignContent.SpaceEvenly)
        ) {
            Column(secondRowModifier) {
                SpanText("Department", labelModifier)
                DDSCustomDropDown(
                    property = viewModel.state.deptState,
                    onPropertyChange = viewModel::onSelectDept,
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                )
            }
            Column(secondRowModifier) {
                SpanText("Course (Optional)", labelModifier)
                DDSCustomDropDown(
                    property = viewModel.state.courseState,
                    onPropertyChange = viewModel::onSelectCourse,
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                )
            }
            Column(secondRowModifier) {
                SpanText("Prof (Optional)", labelModifier)
                DDSCustomDropDown(
                    property = viewModel.state.profState,
                    onPropertyChange = viewModel::onSelectProf,
                    selectModifier = modifier1.width(125.px),
                    optionModifier = modifier2,
                )
            }
        }
    }
}
