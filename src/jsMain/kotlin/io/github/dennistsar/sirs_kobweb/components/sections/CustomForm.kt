package io.github.dennistsar.sirs_kobweb.components.sections

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.asStyleBuilder
import org.jetbrains.compose.web.attributes.onSubmit
import org.jetbrains.compose.web.dom.Form
import org.jetbrains.compose.web.dom.SubmitInput

@Composable
fun CustomForm(
    submitModifier: Modifier,
    submitBehavior: () -> Unit,
    content:  @Composable ((@Composable () -> Unit) -> Unit),
){
    Form(
        attrs = {
            onSubmit {
                it.preventDefault()// This stops the form from "submitting"
                submitBehavior()
            }
        }
    ) {
        content {
            SubmitInput {
                style { submitModifier.asStyleBuilder().invoke(this) }
            }
        }
    }
}