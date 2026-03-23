plugins {
    java
}

group = "org.lwjgl.glfw"

configurations.getByName("default").isCanBeResolved = true

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("lwjgl-glfw-classes")
    destinationDirectory.set(file("../ZalithLauncher/src/main/assets/components/lwjgl3/"))
    // Auto update the version with a timestamp so the project jar gets updated by Pojav
    doLast {
        val versionFile = file("../ZalithLauncher/src/main/assets/components/lwjgl3/version")
        versionFile.writeText(System.currentTimeMillis().toString())
    }
    from({
        configurations.getByName("default").map {
            println(it.name)
            if (it.isDirectory) it else zipTree(it)
        }
    })
    exclude("net/java/openjdk/cacio/ctc/**")
    manifest {
        attributes("Manifest-Version" to "3.3.6")
        attributes("Automatic-Module-Name" to "org.lwjgl")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}
