allprojects {
    version = "1.0"

    group = "io.braendli"

    apply {
        plugin("kotlin")
        plugin("edu.sc.seis.launch4j") version "2.3.0"
    }

    launch4j {
        mainClassName = "io.braendli.importer.ImportForm"
        icon = "../../src/main/resources/icons/app.ico"
        chdir = "."
    }

    dependencies {
        compile("org.apache.poi:poi:3.15")
        compile("org.apache.poi:poi-ooxml:3.15")

        compile("org.controlsfx:controlsfx:8.40.12")

        compile("org.slf4j:slf4j-api:1.7.22")
        compile("ch.qos.logback:logback-classic:1.1.9")

        compile(kotlin("stdlib-jdk8"))
    }

    repositories {
        mavenCentral()
    }
}
