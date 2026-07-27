plugins {
    alias(libs.plugins.kotlin.serialization)
    kotlin("jvm") version "2.3.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}