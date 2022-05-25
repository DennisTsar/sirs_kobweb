package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.silk.components.text.Text
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.get

@Composable
fun<T> CustomDropDown(
    selectModifier: Modifier = Modifier.fillMaxSize(),
    optionModifier: Modifier = Modifier.fillMaxSize(),
    list: Collection<T>,
    onSelect: (String) -> Unit,
    getText: (T) -> String = {it.toString()},
    getValue: (T) -> String = getText,
    selected: T? = list.firstOrNull(),
){
    Select(
        selectModifier.asAttributesBuilder {
            onChange {
                onSelect(it.value ?: "Error")
            }
        }
    ){
       DomSideEffect {
           val index = list.indexOf(selected)
           it.selectedIndex = if (index>=0) index else 0
           it.options[it.selectedIndex]
               .unsafeCast<HTMLOptionElement?>()?.selected = true
       }
        list.forEach {
            Option(
                getValue(it),
                optionModifier.asAttributesBuilder()
            ) {
                Text(getText(it))
            }
        }
    }
}