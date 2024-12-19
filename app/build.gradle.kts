import org.graalvm.buildtools.gradle.tasks.MetadataCopyTask

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
    agent {
        enabled = true
    }
}

tasks.named<JavaExec>("run") {
    args(rootDir.resolve("image.png"), "3", "240", rootDir.resolve("image.jpg"))
    doLast {
        projectDir.resolve("out-01-edges.png").delete()
        projectDir.resolve("out-02-traced.svg").delete()
        projectDir.resolve("out-03-filtered.svg").delete()
        projectDir.resolve("out-04-result.svg").delete()
    }
}

tasks.withType<MetadataCopyTask>() {
    inputTaskNames.add("run")
    outputDirectories.add("src/main/resources/META-INF/native-image")
    dependsOn("run")
}

tasks.register("generateMetadata") {
    dependsOn("metadataCopy")
}
