import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `build-scan`
    kotlin("jvm") version "1.2.21"
    id("edu.sc.seis.launch4j") version "2.3.0"
}

version = "1.0"

launch4j {
    mainClassName = "io.braendli.importer.ImportForm"
    icon = "../../src/main/resources/icons/app.ico"
    chdir = "."
}

dependencies {
    compile("org.apache.poi:poi:3.15")
    compile("org.apache.poi:poi-ooxml:3.15")

    compile("org.firebirdsql.jdbc:jaybird-jdk17:2.2.10")

    compile("org.controlsfx:controlsfx:8.40.12")

    compile("org.slf4j:slf4j-api:1.7.22")
    compile("ch.qos.logback:logback-classic:1.1.9")

    compile(files("lib/jaybird22.dll"))
    compile(files("lib/jaybird22_x64.dll"))
    compile(files("lib/fbembed.dll"))
    compile(kotlin("stdlib-jdk8"))
}

repositories {
    mavenCentral()
}
