import org.graalvm.buildtools.gradle.tasks.MetadataCopyTask

plugins {
    application
    id("org.graalvm.buildtools.native") version "0.10.4"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")
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
        enabled = !project.gradle.startParameter.taskNames.contains(":app:test")
    }
}

val defaultRun = tasks.named<JavaExec>("run") {
    args(rootDir.resolve("image.png"), rootDir.resolve("image.jpg"))
    doLast {
        projectDir.resolve("out-01-edges.png").delete()
        projectDir.resolve("out-02-traced.svg").delete()
        projectDir.resolve("out-03-filtered.svg").delete()
        projectDir.resolve("out-04-result.svg").delete()
    }
}

val runCmdInstrumentation = tasks.register<JavaExec>("runCmdInstrumentation") {
    mainClass = defaultRun.get().mainClass
    classpath(defaultRun.get().classpath)
    args(rootDir.resolve("image.png"), rootDir.resolve("image.jpg"))
}

val runGuiInstrumentation = tasks.register<JavaExec>("runGuiInstrumentation") {
    mainClass = defaultRun.get().mainClass
    classpath(defaultRun.get().classpath)
    args(rootDir.resolve("image.png"), "-gui", "--instrumentation-run-only")
}

tasks.withType<MetadataCopyTask>() {
    inputTaskNames.add("runCmdInstrumentation")
    inputTaskNames.add("runGuiInstrumentation")
    outputDirectories.add("src/main/resources/META-INF/native-image")
    dependsOn(runCmdInstrumentation, runGuiInstrumentation)
}

tasks.register("generateMetadata") {
    dependsOn(runCmdInstrumentation, runGuiInstrumentation)
    doLast {
        projectDir.resolve("out-01-edges.png").delete()
        projectDir.resolve("out-02-traced.svg").delete()
        projectDir.resolve("out-03-filtered.svg").delete()
        projectDir.resolve("out-04-result.svg").delete()
    }
}

tasks.named("nativeCompile") {
    dependsOn("generateMetadata")
}
