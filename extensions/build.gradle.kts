plugins {
    id("me.champeau.jmh") version "0.6.5"
}

dependencies {
    jmh("org.openjdk.jmh:jmh-core:0.9")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:0.9")

    implementation(project(":unsigned"))

    testImplementation(project(":unittest"))
}

tasks {
    compileJmhKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}