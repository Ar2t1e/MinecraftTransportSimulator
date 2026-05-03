import java.nio.file.Paths
import kotlin.io.path.moveTo
import kotlin.io.path.ExperimentalPathApi
import java.nio.file.Files


plugins {
    java
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

subprojects {
    apply(plugin = "java")
}

var modVersion: String = project.property("global_version").toString()

var mcCore = project(":mccore")
var mcInterfaceForge1122 = project(":mcinterfaceforge1122")

tasks.register("buildCore") {
    dependsOn(mcCore.tasks.build)
    doLast {
        moveToOut(mcCore, "core")
    }
}

tasks.register("buildForge1122") {
    doFirst { preBuild() }
    doLast {
        moveToOut(mcInterfaceForge1122, "1.12.2")
    }
    dependsOn(mcInterfaceForge1122.tasks.build)
}

tasks.register("buildForgeAll") {
    dependsOn(tasks.getByName("buildForge1122"))
}

@OptIn(ExperimentalPathApi::class)
fun moveToOut(subProject: Project, versionStr: String) {
    val jarName = "Immersive Vehicles-${subProject.version}.jar"
    val source = Paths.get("${subProject.projectDir.canonicalPath}/build/libs/$jarName")
    val outDir = Paths.get("${project.projectDir.canonicalPath}/out")
    Files.createDirectories(outDir)
    source.moveTo(outDir.resolve(jarName), true)
}

@OptIn(ExperimentalPathApi::class)
fun moveToOut(moduleDirectory: String, artifactVersion: String) {
    val jarName = "Immersive Vehicles-$artifactVersion.jar"
    val source = Paths.get("${project.projectDir.canonicalPath}/$moduleDirectory/build/libs/$jarName")
    val outDir = Paths.get("${project.projectDir.canonicalPath}/out")
    Files.createDirectories(outDir)
    source.moveTo(outDir.resolve(jarName), true)
}

fun preBuild() {
    project.projectDir.canonicalFile.walk()
        .filter { it.name == "gradle.properties" || it.name == "mcmod.info" || it.name == "InterfaceLoader.java" }
        .forEach { it.writeText(it.readText()
            .replace(Regex("mod_version=(.+)"), "mod_version=$modVersion")
            .replace(Regex("\"version\": \"[^\"]*\""), "\"version\": \"$modVersion\"")
            .replace(Regex("MODVER = \"[^\"]*\";"), "MODVER = \"$modVersion\";")) }
}
