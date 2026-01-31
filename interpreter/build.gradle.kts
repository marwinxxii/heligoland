plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotest)
}

kotlin {
    jvmToolchain(25)
    explicitApi()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.kotlin.coroutines)
    testImplementation(libs.kotlin.kotest)
    testImplementation(libs.kotlin.kotest.property)
    testImplementation(libs.kotlin.kotest.junit5)
}
