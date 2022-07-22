package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.silk.components.text.Text
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.get

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
       DisposableEffect(selected) {
           scopeElement.selectedIndex = list.indexOf(selected).takeIf { it >= 0 } ?: 0
           scopeElement.options[scopeElement.selectedIndex]
               .unsafeCast<HTMLOptionElement?>()?.selected = true
           onDispose {  }
       }
        list.forEach {
            Option(
                getValue(it),
                optionModifier.asAttributesBuilder(),
            ) {
                Text(getText(it))
            }
        }
    }
}