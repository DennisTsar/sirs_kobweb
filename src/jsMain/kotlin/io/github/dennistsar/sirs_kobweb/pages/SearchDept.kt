package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.*
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
import com.varabyte.kobweb.navigation.UpdateHistoryMode
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaStar
import com.varabyte.kobweb.silk.components.icons.fa.FaStarHalf
import com.varabyte.kobweb.silk.components.icons.fa.IconStyle
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
import io.github.dennistsar.sirs_kobweb.data.toTotalAndAvesPair
import io.github.dennistsar.sirs_kobweb.misc.*
import io.github.dennistsar.sirs_kobweb.states.DropDownState
import io.github.dennistsar.sirs_kobweb.states.SearchDeptState
import io.github.dennistsar.sirs_kobweb.states.SearchDeptStateImpl
import io.github.dennistsar.sirs_kobweb.states.Status
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import kotlin.math.roundToInt
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
                if(state.profListLoading && status != Status.InitialLoading)
                    LoadingSpinner()
            },
            center = {
                if(status != Status.InitialLoading)
                    SearchDeptFormContent(state)
                else
                    LoadingSpinner()
            }
        )

        // dummy change
        // This logic is kept here as opposed to in State class for performance reasons
        // Prevents having to reload HTML when status changes back to previously used one - it's already loaded
        val finishLoading = { state.profListLoading = false }
        when(status) {
            Status.Prof -> {
                Text(state.profState.selected)
                ProfSummary(state.selectedProfEntries, state.applicableCourseAves, finishLoading)
            }
            Status.Course -> ProfScoresList(state.scoresByProfForCourse, finishLoading)
            Status.Dept -> ProfScoresList(state.scoresByProf, finishLoading)
            else -> {}
        }
    }
}

@Composable
fun ProfSummary(
    entries: List<Entry>,
    applicableCourseAves: Map<String, List<Double>>,
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

                    Text(courseName)
                    BarGraph(ratings)
                    Text(
                        "Prof Average",
                        courseAve?.let {
                            Modifier.color(
                                if (selectedScore>=it) Color.green else Color.red
                            )
                        } ?: Modifier
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StarRating(selectedScore)
                        Text(selectedScore.roundToDecimal(2).toString(), Modifier.margin(left = 5.px))
                    }
                    courseAve?.let{
                        Text("Course Average")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRating(it)
                            Text(it.roundToDecimal(2).toString(), Modifier.margin(left = 5.px))
                        }
                    }
                }
            }
    }

    ProfScoresList(b.mapValues { it.value.toTotalAndAvesPair() },onLoad)
}

@Composable
fun StarRating(
    rating: Double,
    yellow: CSSColorValue = Color("#FDCC0D"),
    gray: CSSColorValue = Color("#dbdbdf"),
    style: IconStyle = IconStyle.FILLED
){
    val yellowModifier = Modifier.color(yellow)
    val grayModifier = Modifier.color(gray)

    val a = (rating*2).roundToInt()/2.0
    Row {
        for (i in 1..5) {
            when {
                (i <= a) -> FaStar(yellowModifier, style)
                (i - .5 == a) -> HalfStarColored(yellowModifier, grayModifier, style)
                else -> FaStar(grayModifier, style)
            }
        }
    }
}

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun HalfStarColored(yellowModifier: Modifier, grayModifier: Modifier, style: IconStyle = IconStyle.FILLED){
    val len = (-1.0/16).px
    Box {
        FaStarHalf(Modifier.margin(right = len).then(yellowModifier), style)
        FaStarHalf(Modifier.margin(left = len).transform { scaleX(-1) }.then(grayModifier), style)
    }
}

@Composable
fun BarGraph(
    ratings: List<Int>,
    max: Int = ratings.maxOrNull() ?: 0,
    height: Double = 175.0,
    colWidth: Double = 36.0,
    textHeight: Double = 25.0,
) {
    Box {
        Row(
            Modifier
                .height(height.px)
                .padding(leftRight = 15.px) // don't really like this but idk how else to extend bounds to end of "Excellent"
        ) {
            ratings.forEachIndexed { index, num ->
                Column(
                    Modifier
                        .fillMaxHeight()
                        .width(colWidth.px),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bottomLabel = when (index) {
                        0 -> "Poor"
                        4 -> "Excellent"
                        else -> null
                    }
                    Box(Modifier.flex(1)) // pushes everything down
                    Text(num.toString())
                    Box(
                        Modifier
                            .width(28.px)
                            .height(num.px * (height - 2.5 * textHeight) / max)
                            .backgroundColor(Color.purple)
                            .thenIf(bottomLabel == null, Modifier.margin(bottom = textHeight.px))
                    )
                    // possibly add rotation to this
                    bottomLabel?.let { Text(it, Modifier.height(textHeight.px)) }
                }
            }
        }
    }
}

@Composable
fun LoadingSpinner() =
    Image(
        "circle_loading.gif",
        "Loading",
        Modifier.size(75.px),
    )

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
        (listOf("")+TenQsShortened+"Total # of Responses").forEachIndexed { index, text ->
            Box(
                Modifier.width(spacing)
            ) {
                Text(
                    text,
                    Modifier
                        .width(175.px)
                        .transform { rotate((-45).deg) }
                        .fontSize(fontSize)
                        .margin(topBottom = 50.px, leftRight = (-18).px)
                        .textDecorationLine(TextDecorationLine.Underline)
                        .thenIf(index-1 in TenQs.indices) { Modifier.title(TenQs[index-1]) }
                )
            }
        }

        val gridElementModifier =
            Modifier.width(spacing)
                .fontSize(fontSize)
                .margin(topBottom = 7.5.px)
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
        remember(list) { onLoad() } // since loading only happens when list is changed
    }
}

@Composable
fun SearchDeptFormContent(state: SearchDeptState) {
    val modifier2 = Modifier.fillMaxSize()
//            .backgroundColor(Color.chocolate)
    val modifier1 = Modifier//.backgroundColor(Color.palevioletred)
    val labelModifier = Modifier.fontWeight(FontWeight.Bold).padding(leftRight = 2.px)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("School",
            labelModifier.alignSelf(AlignSelf.Start),
        )

        state::schoolState.run {
            CustomDropDown(
                list = get().list,
                onSelect = { set(get().copy(selected = it)) },
                selectModifier = modifier1
                    .borderRadius(50.px)
                    .fillMaxWidth()
                    .background("#ddd"),
                optionModifier = modifier2,
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
            list = get().list,
            onSelect = { set(get().copy(selected = it)) },
            selectModifier = selectModifier,
            optionModifier = optionModifier,
            getText = getText,
            getValue = getValue,
            selected = get().selected,
        )
    }
}
