plugins {
    alias(libs.plugins.kotlin)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
    explicitApi()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    api(libs.kotlin.coroutines.core)
    implementation(project(":interpreter"))
}
