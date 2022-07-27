package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.silk.components.text.SpanText
import io.github.dennistsar.sirs_kobweb.states.DropDownState
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.get
import kotlin.reflect.KMutableProperty0

@Composable
fun<T> CustomDropDown(
    list: Collection<T>,
    onSelect: (String) -> Unit,
    selectModifier: Modifier = Modifier,
    optionModifier: Modifier = Modifier,
    getText: (T) -> String = { it.toString() },
    getValue: (T) -> String = getText,
    selected: T? = list.firstOrNull(),
) {
    Select(
        selectModifier.asAttributesBuilder {
            onChange {
                onSelect(it.value ?: "Error")
            }
        }
    ) {
        // "list" key needed for back press to work when there's switching schools
        // this is cuz in that case "selected" changes before "list"
        // Ex: Select non-None prof, select new school, press back button
        // Without "list" key, prof stays "None" even though it should be the selected value
       DisposableEffect(selected, list) {
           scopeElement.selectedIndex = list.indexOf(selected).takeIf { it >= 0 } ?: 0
               .also { scopeElement.options[it].unsafeCast<HTMLOptionElement?>()?.selected = true }
           onDispose {  }
       }
        list.forEach {
            Option(
                getValue(it),
                optionModifier.asAttributesBuilder(),
            ) {
                SpanText(getText(it))
            }
        }
    }
}

@Composable
fun ReflectiveCustomDropDown(
    property: KMutableProperty0<DropDownState<String>>,
    selectModifier: Modifier = Modifier,
    optionModifier: Modifier = Modifier,
    getText: (String) -> String = { it },
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