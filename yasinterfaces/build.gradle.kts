plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.2.0"
}

group = "nl.joozd.yasinterfaces"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}

dokka {
    moduleName.set("YAS Interfaces")

    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("docs"))
        failOnWarning.set(false)
    }
}

/**
 * Packages the generated Dokka HTML documentation as the Maven
 * javadoc artifact.
 */
val dokkaHtmlJar by tasks.registering(Jar::class) {
    dependsOn("dokkaGeneratePublicationHtml")
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("docs"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "nl.joozd.yasinterfaces"
            artifactId = "yasinterfaces"
            version = project.version.toString()

            artifact(dokkaHtmlJar)

            pom {
                name.set("YAS Interfaces")
                description.set("Interfaces for YALA Airport Source")
            }
        }
    }

    repositories {
        maven {
            val isSnapshot = version.toString().endsWith("-SNAPSHOT")

            name = "reposilite"

            url = uri(
                if (isSnapshot) {
                    "https://repo.joozd.nl/snapshots"
                } else {
                    "https://repo.joozd.nl/releases"
                }
            )

            credentials {
                username = findProperty("repoUsername")?.toString()
                    ?: error("Missing Gradle property: repoUsername")

                password = findProperty("repoPassword")?.toString()
                    ?: error("Missing Gradle property: repoPassword")
            }
        }
    }
}