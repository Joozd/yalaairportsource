plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"

    id("com.gradleup.shadow") version "9.6.1"
    application
}

group = "nl.joozd"
version = "1.02"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":yasinterfaces"))
    implementation(project(":yasio"))

    implementation("org.apache.commons:commons-csv:1.14.1")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        // freeCompilerArgs.add("-XXLanguage:+FullValueClasses")
    }
}

application {
    mainClass.set("nl.joozd.airportsource.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}