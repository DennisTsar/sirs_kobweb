import androidx.compose.runtime.CompositionLocalProvider
import com.varabyte.kobweb.core.AppGlobalsLocal
import com.varabyte.kobweb.navigation.RoutePrefix
import com.varabyte.kobweb.navigation.Router
import kotlin.Unit
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable

public fun main(): Unit {
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
