import androidx.compose.runtime.CompositionLocalProvider
import com.varabyte.kobweb.core.AppGlobalsLocal
import com.varabyte.kobweb.navigation.RoutePrefix
import com.varabyte.kobweb.navigation.Router
import kotlin.Unit
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.hasClass
import kotlinx.dom.removeClass
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.EventSource
import org.w3c.dom.EventSourceInit
import org.w3c.dom.MessageEvent
import org.w3c.dom.`get`

private fun forceReloadNow(): Unit {
    window.stop()
    window.location.reload()
}

private fun handleServerStatusEvents(): Unit {
    val status = document.getElementById("status")!!
    var lastVersion: Int? = null
    var shouldReload = false

    val warningIcon = status.children[0]!!
    val spinnerIcon = status.children[1]!!
    val statusText = status.children[2]!!

    status.addEventListener("transitionend", {
        if (status.hasClass("fade-out")) {
            status.removeClass("fade-out")
            if (shouldReload) {
                forceReloadNow()
            }
        }
    })

    val eventSource = EventSource("/api/kobweb-status", EventSourceInit(true))
    eventSource.addEventListener("version", { evt ->
        val version = (evt as MessageEvent).data.toString().toInt()
        if (lastVersion == null) {
            lastVersion = version
        }
        if (lastVersion != version) {
            lastVersion = version
            if (status.className.isNotEmpty()) {
                shouldReload = true
            } else {
                // Not sure if we can get here but if we can't rely on the status transition
                // to reload we should do it ourselves.
                forceReloadNow()
            }
        }
    })

    eventSource.addEventListener("status", { evt ->
        val values: dynamic = JSON.parse<Any>((evt as MessageEvent).data.toString())
        val text = values.text as String
        val isError = (values.isError as String).toBoolean()
        if (text.isNotBlank()) {
            warningIcon.className = if (isError) "visible" else "hidden"
            spinnerIcon.className = if (isError) "hidden" else "visible"
            statusText.innerHTML = "<i>$text</i>"
            status.className = "fade-in"
        } else {
            if (status.className == "fade-in") {
                status.className = "fade-out"
            }
        }
    })

    eventSource.onerror = { eventSource.close() }
}

public fun main(): Unit {
    handleServerStatusEvents()

    RoutePrefix.set("")
    val router = Router()
    router.register("/") { io.github.dennistsar.sirs_kobweb.pages.HomePage() }
    router.register("/about") { io.github.dennistsar.sirs_kobweb.pages.AboutPage() }
    router.register("/markdown") { io.github.dennistsar.sirs_kobweb.pages.MarkdownPage() }
    router.register("/proflist/{school}/{dept}") { io.github.dennistsar.sirs_kobweb.pages.ProfList()
            }
    router.register("/searchdept") { io.github.dennistsar.sirs_kobweb.pages.SearchDept() }

    com.varabyte.kobweb.silk.initSilkHook = { ctx ->
        ctx.theme.registerComponentStyle(io.github.dennistsar.sirs_kobweb.components.sections.FooterStyle)
        ctx.theme.registerComponentStyle(io.github.dennistsar.sirs_kobweb.components.sections.NavHeaderStyle)
        ctx.theme.registerComponentStyle(io.github.dennistsar.sirs_kobweb.components.sections.NavItemStyle)
        ctx.theme.registerComponentVariants(io.github.dennistsar.sirs_kobweb.components.sections.NavButtonVariant)
        io.github.dennistsar.sirs_kobweb.updateTheme(ctx)
    }

    router.navigateTo(window.location.href.removePrefix(window.location.origin))

    // For SEO, we may bake the contents of a page in at build time. However, we will overwrite them
    // the first time we render this page with their composable, dynamic versions. Think of this as
    // poor man's hydration :)
    // See also: https://en.wikipedia.org/wiki/Hydration_(web_development)
    val root = document.getElementById("root")!!
    while (root.firstChild != null) {
        root.removeChild(root.firstChild!!)
    }

    renderComposable(rootElementId = "root") {
        CompositionLocalProvider(AppGlobalsLocal provides mapOf()) {
            io.github.dennistsar.sirs_kobweb.MyApp {
                router.renderActivePage()
            }
        }
    }
}
