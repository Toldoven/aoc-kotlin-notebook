import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.kotlin.jupyter.api") version "0.12.0-339"
    id("com.vanniktech.maven.publish.base") version "0.30.0"
}

repositories {
    mavenCentral()
}

kotlinJupyter {
    addApiDependency()
}

dependencies {
    implementation("it.skrape:skrapeit:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    coordinates(artifactId = property("artifactId") as String)
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    configure(KotlinJvm())

    pom {
        name.set("Advent of Code Kotlin Notebook")
        description.set("Solve Advent of Code interactively, without leaving your IDE, with Kotlin Notebook!")
        inceptionYear.set("2024")
        url.set("https://github.com/Toldoven/aoc-kotlin-notebook")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("https://opensource.org/license/mit")
            }
        }

        developers {
            developer {
                id.set("toldoven")
                name.set("Toldoven")
                url.set("https://github.com/Toldoven")
            }
        }

        scm {
            url.set("https://github.com/Toldoven/aoc-kotlin-notebook/")
            connection.set("scm:git:git://github.com/Toldoven/aoc-kotlin-notebook.git")
            developerConnection.set("scm:git:ssh://git@github.com:Toldoven/aoc-kotlin-notebook.git")
        }
    }

    signAllPublications()
}
