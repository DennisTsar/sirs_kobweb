package io.github.dennistsar.sirs_kobweb.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.Text
import io.github.dennistsar.sirs_kobweb.components.layouts.PageLayout
import org.jetbrains.compose.web.dom.P

@Page//("/sirs_kobweb/")
@Composable
fun AboutPage() {
    PageLayout("ABOUT") {
        Text("This is a skeleton app used to showcase a basic site made using Kobweb")
        P()
        Link("/", "Go Home")
    }
}