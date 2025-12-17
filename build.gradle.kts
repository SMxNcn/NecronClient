import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    idea
    java
    kotlin("jvm") version "1.9.0"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("gg.essential.loom") version "1.3.12"
}

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val modid: String by project

group = "cn.boop.necron"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    runConfigs {
        getByName("client") {
            property("mixin.debug", "true")
            property("asmhelper.verbose", "true")
            programArgs("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
            programArgs("--mixin", "mixins.necronclient.json")
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.necronclient.json")
    }
    mixin {
        defaultRefmapName.set("mixins.necronclient.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.essential.gg/repository/maven-public/")
    maven("https://repo.polyfrost.cc/releases")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    compileOnly("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")

    implementation("com.google.code.gson:gson:2.2.4")
    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.2-alpha+") // Should not be included in jar
    compileOnly(files("${project.rootDir}/lib/oneconfig-internal.jar"))
    shadowImpl("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta17")

}

tasks {
    processResources {
        inputs.property("version", version)
        inputs.property("mcversion", mcVersion)
        inputs.property("modid", modid)
        inputs.property("basePackage", baseGroup)

        filesMatching(listOf("mcmod.info", "mixins.necronclient.json")) {
            expand(
                mapOf(
                    "version" to version,
                    "mcversion" to mcVersion,
                    "modid" to modid,
                    "basePackage" to baseGroup
                )
            )
        }
        dependsOn(compileJava)
    }
    named<Jar>("jar") {
        manifest.attributes(
            "ModSide" to "CLIENT",
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "TweakOrder" to "0",
            "MixinConfigs" to "mixins.necronclient.json",
            "TweakClass" to "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker"
        )
        dependsOn(shadowJar)
        enabled = false
    }
    named<RemapJarTask>("remapJar") {
        inputFile.set(shadowJar.get().archiveFile)
        archiveClassifier.set("")
    }
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("Necron")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImpl)
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

}

tasks.assemble.get().dependsOn(tasks.remapJar)

