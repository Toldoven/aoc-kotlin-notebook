import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.kotlin.jupyter.api") version "0.15.1-710"
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("com.gradleup.shadow") version "9.2.2"
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

val shadowJar by tasks.getting(ShadowJar::class) {
    // publish shaded artifact without the default "-all" suffix
    archiveClassifier.set("")
    mergeServiceFiles()

    fun relocate(pattern: String) = relocate(pattern, "com.toldoven.aoc.shaded.$pattern")
    relocate("it.skrape")
    relocate("io.ktor")
    relocate("kotlinx")
    relocate("org.apache")
}

mavenPublishing {
    coordinates(artifactId = property("artifactId") as String)
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

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

publishing {
    publications {
        create<MavenPublication>("shadow") {
            groupId = project.group.toString()
            artifactId = property("artifactId") as String
            version = project.version.toString()

            artifact(tasks.shadowJar.get())

            pom {
                name.set("Advent of Code in Kotlin Notebook (shaded)")
                description.set("Shaded distribution with relocations to avoid clashes with other dependencies")
            }
        }
    }
}
