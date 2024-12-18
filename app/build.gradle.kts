plugins {
    application
    id("org.graalvm.buildtools.native") version "0.10.4"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testImplementation("org.assertj:assertj-core:3.26.3")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "de.gmasil.edgedetection.App"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

graalvmNative {
    binaries.all {
        buildArgs.add("-Djava.awt.headless=false")
        buildArgs.add("-march=native")
        buildArgs.add("--strict-image-heap")
    }
}
