package io.github.dennistsar.sirs_kobweb.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asAttributesBuilder
import org.jetbrains.compose.web.attributes.onSubmit
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.SubmitInput

@Composable
fun CustomForm(
    submitModifier: Modifier,
    submitBehavior: () -> Unit,
    content:  @Composable ((@Composable () -> Unit) -> Unit),
) {
    Form(
        attrs = {
            onSubmit {
                it.preventDefault()// This stops the form from "submitting"
                submitBehavior()
            }
        }
    ) {
        content {
            SubmitInput(submitModifier.asAttributesBuilder())
        }
    }
}