plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotest)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    explicitApi()
    compilerOptions {
        allWarningsAsErrors = true
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    providers
        .gradleProperty("snapshots.record")
        .also {
            if (it.isPresent) {
                environment("selfie", "overwrite")
            } else {
                environment("selfie", "readonly")
            }
        }
    inputs.files(layout.projectDirectory.dir("src/test").files("**/*.ss"))
    systemProperty("kotest.framework.config.fqn", "h8d.interpreter.testharness.KotestConfig")
}

dependencies {
    api(libs.kotlin.coroutines.core)
    api(project(":parser"))
    implementation(project(":stackmachine"))
    testImplementation(libs.kotlin.kotest)
    testImplementation(libs.kotlin.kotest.junit5)
    testImplementation(libs.selfie)
}
