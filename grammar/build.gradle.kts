import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.antlr.kotlin)
}

val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    dependsOn("cleanGenerateKotlinGrammarSource")

    // Only include *.g4 files. This allows tools (e.g., IDE plugins)
    // to generate temporary files inside the base path
    source = fileTree(layout.projectDirectory.dir("src/grammar")) {
        include("**/*.g4")
    }

    // We want the generated source files to have this package name
    val pkgName = "h8d.parsers.generated"
    packageName = pkgName

    // We want visitors alongside listeners.
    // The Kotlin target language is implicit, as is the file encoding (UTF-8)
    arguments = listOf("-visitor")

    // Generated files are outputted inside build/generatedAntlr/{package-name}
    val outDir = "generated/antlr/${pkgName.replace('.', '/')}"
    outputDirectory = layout.buildDirectory.dir(outDir).get().asFile
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    sourceSets {
        main {
            kotlin {
                srcDir(generateKotlinGrammarSource)
            }
        }
    }
}

dependencies {
    api(libs.antlr.kotlin)
}
