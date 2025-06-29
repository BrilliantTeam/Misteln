import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.patcher") version "1.7.1"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://jitpack.io")
    }
}

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content {
            onlyForConfigurations(configurations.paperclip.name)
        }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.10.2:fat")
    decompiler("org.vineflower:vineflower:1.10.1")
    paperclip("io.papermc:paperclip:3.0.3")
}

paperweight {
    serverProject = project(":misteln-server")

    remapRepo = paperMavenPublicUrl
    decompileRepo = paperMavenPublicUrl

    useStandardUpstream("purpur") {
        url = github("PurpurMC", "Purpur")
        ref = providers.gradleProperty("purpurCommit")

        withStandardPatcher {
            baseName("Purpur")

            apiPatchDir = layout.projectDirectory.dir("patches/api")
            apiOutputDir = layout.projectDirectory.dir("Misteln-API")

            serverPatchDir = layout.projectDirectory.dir("patches/server")
            serverOutputDir = layout.projectDirectory.dir("Misteln-Server")
        }

        patchTasks.register("generatedApi") {
            isBareDirectory = true
            upstreamDirPath = "paper-api-generator/generated"
            patchDir = layout.projectDirectory.dir("patches/generated-api")
            outputDir = layout.projectDirectory.dir("paper-api-generator/generated")
        }
    }
}

tasks.generateDevelopmentBundle {
    apiCoordinates = "org.brilliantw.misteln:misteln-api"
    libraryRepositories.addAll(
        "https://repo.maven.apache.org/maven2/",
        paperMavenPublicUrl,
        "https://repo.purpurmc.org/snapshots",
    )
}

allprojects {
    publishing {
        repositories {

        }
    }
}

publishing {
    publications.create<MavenPublication>("devBundle") {
        artifact(tasks.generateDevelopmentBundle) {
            artifactId = "dev-bundle"
        }
    }
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printMistelnVersion") {
    doLast {
        println(project.version)
    }
}
