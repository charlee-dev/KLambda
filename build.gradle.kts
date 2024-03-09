import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktLint)
    alias(libs.plugins.shadow) apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        filter {
            exclude { element -> element.file.path.contains("/build/") }
        }
        debug.set(false)
        outputToConsole.set(true)
    }

    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        parallel = true
        config.setFrom(files(rootProject.file("detekt.yml")))
        autoCorrect = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = libs.versions.jvmTarget.get()
        parallel = true
        reports {
            xml.required.set(false)
            html.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
        }
        exclude { it.file.absolutePath.contains("resources/") }
        exclude { it.file.absolutePath.contains("build/") }
        include("**/*.kt")
    }

    tasks.withType<DetektCreateBaselineTask>().configureEach {
        this.jvmTarget = libs.versions.jvmTarget.get()
        exclude { it.file.absolutePath.contains("resources/") }
        exclude { it.file.absolutePath.contains("build/") }
        include("**/*.kt")
    }

    tasks.register("detektAll") {
        group = "verification"
        description = "Runs all detekt tasks"
        dependsOn(tasks.withType<Detekt>())
    }
}
