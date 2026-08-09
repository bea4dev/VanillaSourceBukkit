import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.jar.JarFile

plugins {
    id("com.gradleup.shadow")
}

base {
    archivesName.set("vanilla_source_plugin")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.59.Final")
    compileOnly("com.mojang:datafixerupper:1.0.20")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.0-SNAPSHOT")
    compileOnly("com.github.Be4rJP:Cinema4C:e1a2df2c89")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.7")

    implementation("com.github.Be4rJP:ChiyogamiLib:71d7778b4c")
    implementation("fastutil:fastutil:5.0.9")
    implementation(project(":API"))
    implementation(project(":v1_21_R1"))
    implementation(project(":v1_21_R7"))
    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("dev.jorel:commandapi-paper-shade:12.0.0")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("com.github.bea4dev:ArtGUI:215171490c")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand("project" to mapOf("version" to project.version.toString()))
    }
}

tasks.jar {
    archiveClassifier.set("plain")
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    relocate(
        "dev.jorel.commandapi",
        "com.github.bea4dev.vanilla_source.relocation.commandapi"
    )
    relocate(
        "net.wesjd.anvilgui",
        "com.github.bea4dev.vanilla_source.anvilgui"
    )

    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/*.kotlin_module"
    )
}

tasks.assemble {
    dependsOn(tasks.named("shadowJar"))
}

val verifyMojangMappingsManifest = tasks.register("verifyMojangMappingsManifest") {
    val shadowJar = tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    inputs.file(shadowJar.flatMap { it.archiveFile })

    doLast {
        JarFile(shadowJar.get().archiveFile.get().asFile).use { jar ->
            check(jar.manifest.mainAttributes.getValue("paperweight-mappings-namespace") == "mojang") {
                "The shaded plugin must declare the Mojang mappings namespace"
            }
        }
    }
}

tasks.check {
    dependsOn(verifyMojangMappingsManifest)
}
