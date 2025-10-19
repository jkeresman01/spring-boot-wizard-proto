plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.keresman"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.2.5")

        // Required plugins for Spring Boot wizard functionality
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.gradle")
        bundledPlugin("org.jetbrains.idea.maven")

        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "243.*"
        }

        changeNotes = """
            <h3>1.0-SNAPSHOT</h3>
            <ul>
                <li>Initial release</li>
                <li>Spring Boot project wizard for IntelliJ IDEA Community Edition</li>
                <li>Integration with Spring Initializr</li>
                <li>Support for Java, Kotlin, and Groovy</li>
                <li>Support for Gradle and Maven build systems</li>
                <li>Dependency selection interface</li>
            </ul>
        """.trimIndent()
    }

    instrumentCode = false
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("243.*")
    }
}