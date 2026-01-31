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
}

dependencies {
    implementation(project(":grammar"))
    testImplementation(libs.kotlin.kotest)
    testImplementation(libs.kotlin.kotest.property)
    testImplementation(libs.kotlin.kotest.junit5)
}
