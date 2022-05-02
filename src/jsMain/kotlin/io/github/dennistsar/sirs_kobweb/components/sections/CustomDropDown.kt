package io.github.dennistsar.sirs_kobweb.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.silk.components.text.Text
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select

@Composable
fun<T> CustomDropDown(
    selectModifier: Modifier = Modifier.fillMaxSize(),
    optionModifier: Modifier = Modifier.fillMaxSize(),
    list: List<T>?,
    onSelect: (String) -> Unit,
    getText: (T) -> String,
    getValue: (T) -> String = getText
){
    Select(
        selectModifier.asAttributesBuilder {
            onChange {
                onSelect(it.value ?: "Error")
            }
        }
    ){
        list?.forEach {
            Option(
                getValue(it),
                optionModifier.asAttributesBuilder()
            ) {
                Text(getText(it))
            }
        }
    }
}