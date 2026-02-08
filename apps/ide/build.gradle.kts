import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    explicitApi()
    compilerOptions {
        allWarningsAsErrors = true
        // because of :editor, otherwise compiler throws pre-release Kotlin error
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
                implementation(compose.desktop.currentOs)
                implementation(project(":editor"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "h8d.ide.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "HIDE"
            packageVersion = "1.0.0"
            macOS {
                bundleID = "heligoland.ide"
                dockName = "HIDE"
            }
        }
    }
}
