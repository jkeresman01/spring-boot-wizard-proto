package com.keresman.springbootwizard

data class SpringBootSettings(
    var serverUrl: String = "https://start.spring.io",
    var bootVersion: String = "3.5.6",
    var language: Language = Language.JAVA,
    var buildType: BuildType = BuildType.GRADLE_GROOVY,
    var packaging: Packaging = Packaging.JAR,
    var javaVersion: String = "17",
    var group: String = "com.example",
    var artifact: String = "demo",
    var name: String = "demo",
    var description: String = "Demo project for Spring Boot",
    var packageName: String = "com.example.demo",
    var dependencies: MutableSet<Dependency> = mutableSetOf()
)

enum class Language(val value: String, val displayName: String) {
    JAVA("java", "Java"),
    KOTLIN("kotlin", "Kotlin"),
    GROOVY("groovy", "Groovy")
}

enum class BuildType(val value: String, val displayName: String) {
    GRADLE_GROOVY("gradle-project", "Gradle - Groovy"),
    GRADLE_KOTLIN("gradle-project-kotlin", "Gradle - Kotlin"),
    MAVEN("maven-project", "Maven")
}

enum class Packaging(val value: String, val displayName: String) {
    JAR("jar", "Jar"),
    WAR("war", "War")
}

data class Dependency(
    val id: String,
    val name: String,
    val description: String,
    val category: String
)