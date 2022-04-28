import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)

    kotlin("plugin.serialization") version "1.6.21"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
}

group = "io.github.dennistsar.sirs_kobweb"
version = "1.0-SNAPSHOT"

val ktorVersion = "1.6.8"//Should upgrade to 2.0.0 but that requies Kotlin 1.6.20 which compose doesn't support yet

kobweb {
    index {
        description.set("Powered by Kobweb")
    }
}

kotlin {
    jvm {
        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }

        tasks.named("jvmJar", Jar::class.java).configure {
            archiveFileName.set("sirs_kobweb.jar")
        }
    }
    js(IR) {
        moduleName = "sirs_kobweb"
        browser {
            commonWebpackConfig {
                outputFileName = "sirs_kobweb.js"
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk.core)
                implementation(libs.kobweb.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
             }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kobweb.api)
             }
        }
    }
}