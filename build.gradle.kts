val javaVersion = JavaVersion.VERSION_17
val ktorVersion = "2.0.2"
val kotlinxSerializationVersion = "1.3.3"

plugins {
    kotlin("jvm") version "1.7.10"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://packages.confluent.io/maven/")
        maven("https://jitpack.io")
    }
    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        autoCorrect = true
        buildUponDefaultConfig = true
        allRules = false
        config = files("$rootDir/config/detekt.yml")
    }
}

configurations.all {
    // exclude JUnit 4
    exclude(group = "junit", module = "junit")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = javaVersion.toString()
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = javaVersion.toString()
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
        test {
            // JUnit 5 support
            useJUnitPlatform()
        }
    }
    configurations.all {
        // exclude JUnit 4
        exclude(group = "junit", module = "junit")
    }
}
