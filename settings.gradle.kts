rootProject.name = "tms-utbetalingsoversikt-api"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("managed") {
            from("no.nav.tms:tms-dependency-admin:20261027.7.1")
        }
    }
}