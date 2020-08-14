plugins {
    kotlin("jvm") version "1.3.72"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

tasks {
    test {
        useJUnitPlatform()
    }
}