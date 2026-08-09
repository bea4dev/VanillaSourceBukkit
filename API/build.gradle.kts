import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.Node
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
    id("com.gradleup.shadow")
}

base {
    archivesName.set("vanilla_source_api")
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.59.Final")
    compileOnly("com.mojang:datafixerupper:1.0.20")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.7")
    compileOnly("org.jetbrains:annotations:26.1.0")

    api("fastutil:fastutil:5.0.9")
    api("com.github.Be4rJP:Contan:0cb1c15c65")
    api("net.wesjd:anvilgui:1.10.3-SNAPSHOT")
    api("com.github.bea4dev:ArtGUI:215171490c")
    api("de.articdive:jnoise-pipeline:4.1.0")
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    relocate(
        "net.wesjd.anvilgui",
        "com.github.bea4dev.vanilla_source.anvilgui"
    )
}

tasks.assemble {
    dependsOn(tasks.named("shadowJar"))
}

publishing {
    publications {
        create<MavenPublication>("api") {
            artifactId = "vanilla_source_api"
            artifact(tasks.named("shadowJar"))
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))

            pom {
                name.set("VanillaSource API")
                description.set("Public API for VanillaSource")
            }

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                listOf(
                    Triple("io.papermc.paper", "paper-api", "1.21.1-R0.1-SNAPSHOT"),
                    Triple("io.netty", "netty-all", "4.1.59.Final"),
                    Triple("com.mojang", "datafixerupper", "1.0.20"),
                    Triple("com.mojang", "authlib", "1.5.25"),
                    Triple("com.ticxo.modelengine", "ModelEngine", "R4.0.7")
                ).forEach { (dependencyGroup, dependencyArtifact, dependencyVersion) ->
                    val dependencyNode: Node = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", dependencyGroup)
                    dependencyNode.appendNode("artifactId", dependencyArtifact)
                    dependencyNode.appendNode("version", dependencyVersion)
                    dependencyNode.appendNode("scope", "provided")
                }
            }
        }
    }

    repositories {
        maven {
            name = "branch"
            val repositoryPath = providers.environmentVariable("MAVEN_REPO_DIR")
                .orElse(layout.buildDirectory.dir("mvn-repo").map { it.asFile.absolutePath })
            url = uri(repositoryPath)
        }
    }
}
