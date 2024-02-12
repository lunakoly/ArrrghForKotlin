plugins {
    alias(libs.plugins.kotlinJvm)
}

group = "lunakoly.arrrgh"
version = "1.0.0"

dependencies {
    testImplementation(libs.kotlin.test.junit)
    implementation(kotlin("reflect"))
}
