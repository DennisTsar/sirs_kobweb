package io.github.dennistsar.sirs_kobweb.misc

import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.layout.SimpleGridStyle
import org.jetbrains.compose.web.css.gridTemplateColumns

const val None = "None" // to avoid accidental typos

val TenQs = listOf(
    "The instructor was prepared for class and presented the material in an organized manner",
    "The instructor responded effectively to student comments and questions",
    "The instructor generated interest in the course material",
    "The instructor had a positive attitude toward assisting all students in understanding course material",
    "The instructor assigned grades fairly",
    "The instructional methods encouraged student learning",
    "I learned a great deal in this course",
    "I had a strong prior interest in the subject matter and wanted to take this course",
    "I rate the teaching effectiveness of the instructor as",
    "I rate the overall quality of the course as",
)

val TenQsShortened = listOf(
    "Prepared & Organized",
    "Responded to Questions Effectively",
    "Generated Interest in Material",
    "Positive Attitude Towards Assisting Students",
    "Graded Fairly",
    "Effective Teaching Methods",
    "Learned a Great Deal",
    "Strong Prior Interest",
    "Teaching Effectiveness",
    "Overall Quality of Course",
)

val gridVariant11 = run {
    val numColumns = 11
    val gridModifier = Modifier.styleModifier {
        gridTemplateColumns("repeat($numColumns, 1fr)")
    }
    SimpleGridStyle.addVariant("base-$numColumns") {
        base { gridModifier }
    }
}

val gridVariant12 = run {
    val numColumns = 12
    val gridModifier = Modifier.styleModifier {
        gridTemplateColumns("repeat($numColumns, 1fr)")
    }
    SimpleGridStyle.addVariant("base-$numColumns") {
        base { gridModifier }
    }
}

//val variants = (0 until 12)
//        .associate { i ->
//            val numColumns = i + 1
//            val gridModifier = Modifier.styleModifier {
//                gridTemplateColumns("repeat($numColumns, 1fr)")
//            }
//            numColumns to SimpleGridStyle.addVariant("base-$numColumns") {
//                base { gridModifier }
//            }
//        }

//val a = SimpleGridStyle.addVariant("$name-$numColumns") {
//    base {
//        val gridModifier = Modifier.styleModifier {
//            gridTemplateColumns("repeat($numColumns, 1fr)")
//        }
//    }
//}

//val SimpleGridColumnVariants: Map<Breakpoint?, Map<Int, ComponentVariant>> = run {
//    (listOf(null) + Breakpoint.values())
//        .associateWith { breakpoint ->
//            val name = breakpoint?.toString()?.lowercase() ?: "base"
//            val variants = (0 until MAX_COLUMN_COUNT)
//                .associate { i ->
//                    val numColumns = i + 1
//                    val gridModifier = Modifier.styleModifier {
//                        gridTemplateColumns("repeat($numColumns, 1fr)")
//                    }
//                    println("ADDING $name-$numColumns")
//                    numColumns to SimpleGridStyle.addVariant("$name-$numColumns") {
//                        if (breakpoint == null) {
//                            base = gridModifier
//                        } else {
//                            breakpoints[breakpoint] = gridModifier
//                        }
//                    }
//                }
//
//            variants
//        }
//}