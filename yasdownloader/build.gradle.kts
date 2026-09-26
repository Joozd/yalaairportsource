plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.2.0"
}

group = "nl.joozd.airportsource"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":yasinterfaces"))
    implementation(project(":yasio"))

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
    moduleName.set("YAS Downloader")

    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("docs"))
        failOnWarning.set(false)
    }
}

/**
 * Packages the generated Dokka HTML documentation as the Maven
 * javadoc artifact.
 */
val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
    group = "documentation"
    description = "Packages the generated Dokka HTML documentation as a Javadoc JAR."

    dependsOn("dokkaGeneratePublicationHtml")
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("docs"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "nl.joozd.airportsource"
            artifactId = "yasdownloader"
            version = project.version.toString()

            artifact(dokkaHtmlJar)

            pom {
                name.set("YAS Downloader")
                description.set(
                    "Downloader for YALA Airport Source data"
                )
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