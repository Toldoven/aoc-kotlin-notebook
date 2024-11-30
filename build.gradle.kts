plugins {
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.kotlin.jupyter.api") version "0.12.0-339"
}

group = "com.toldoven"
version = "1.0.0"

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

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}