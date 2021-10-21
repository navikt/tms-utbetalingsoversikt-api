plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

val dittNavDependenciesVersion = "2021.10.08-09.46-902572a68e7d"

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:$dittNavDependenciesVersion")
}
