import org.jetbrains.dokka.gradle.DokkaTask

// how to build? run ./gradlew
// where is the jar? build/lib/ipp-client-kotlin...jar

plugins {
    // aLINK edit - Start
    // Error resolving plugin [id: 'org.jetbrains.kotlin.jvm', version: '1.5.32']
    id("org.jetbrains.kotlin.jvm")
    // aLINK edit - End
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.sonarqube") version "3.3"
    id("maven-publish")
    id("signing")
    id("jacoco")
}

group = "de.gmuth"
version = "2.4-SNAPSHOT"

repositories {
    mavenCentral()
}

// update gradle wrapper
// ./gradlew wrapper --gradle-version 7.3.3

//java {
//    registerFeature("slf4jSupport") {
//        usingSourceSet(sourceSets["main"])
//    }
//}

dependencies {
    //implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    //implementation("org.jetbrains.kotlin:kotlin-stdlib")
    //implementation("org.jetbrains.kotlin:kotlin-reflect")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    //"slf4jSupportImplementation"("org.slf4j:slf4j-api:1.7.32") // pom.xml: scope=compile, optional=true
    compileOnly("org.slf4j:slf4j-api:1.7.32")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.32")
}

// gradlew clean -x test build publishToMavenLocal
defaultTasks("assemble")

tasks.compileKotlin {
    kotlinOptions {
        // JVM target 1.6 is deprecated and will be removed in a future release.
        jvmTarget = "1.8" // Keep for support of old android versions
    }
}

// avoid warnings "jvm target compatibility should be set to the same Java version."
tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "17"
    }
}
tasks.compileJava {
    targetCompatibility = tasks.compileKotlin.get().kotlinOptions.jvmTarget
}

//tasks.withType<Jar> {
//    archiveBaseName.set("ipp-client")
//    archiveClassifier.set("")
//}

// ================= PUBLISHING ================

// Github Packages:
// do NOT publish from your developer host!
// to release: 1. remove SNAPSHOT from version; 2. commit & push; 3. check github workflow results
// if the workflow tries to publish the same release again you'll get: "Received status code 409 from server: Conflict"
// Maven Central:
// https://central.sonatype.org/publish/release/
val repo = System.getProperty("repo")
publishing {
    repositories {
        if (repo == "github") {
            println("> maven repo github")
            maven {
                name = "GitHubPackages" // Must match regex [A-Za-z0-9_\-.]+.
                url = uri("https://maven.pkg.github.com/gmuth/ipp-client-kotlin")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
        // gradlew publish
        if (repo == "sonatype") {
            println("> maven repo sonatype")
            maven {
                name = "Sonatype"
                //val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                //val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                //url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                val host = "https://s01.oss.sonatype.org"
                val path = if (version.toString().endsWith("SNAPSHOT")) "/content/repositories/snapshots/"
                else "/service/local/staging/deploy/maven2/"
                url = uri(host.plus(path))
                println("> publish.url: $url")
                credentials {
                    username = project.findProperty("ossrh.username") as String?
                    password = project.findProperty("ossrh.password") as String?
                }
            }
        }
    }
    publications {
        create<MavenPublication>("ippclient") {
            from(components["java"])
            pom {
                name.set("ipp client library")
                description.set("A client implementation of the ipp protocol, RFCs 8010, 8011, 3995 and 3996")
                url.set("https://github.com/gmuth/ipp-client-kotlin")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://raw.githubusercontent.com/gmuth/ipp-client-kotlin/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("gmuth")
                        name.set("Gerhard Muth")
                        email.set("gerhard.muth@gmx.de")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/gmuth/ipp-client-kotlin.git")
                    developerConnection.set("scm:git:ssh://git@github.com/gmuth/ipp-client-kotlin.git")
                    url.set("https://github.com/gmuth/ipp-client-kotlin")
                }
            }
        }
    }
}

// ====== signing ======

// set gradle.properties
// signing.keyId
// signing.password
// signing.secretKeyRingFile


// gradle signIppclientPublication
signing {
    sign(publishing.publications["ippclient"])
}

// ======  produce sources.jar and javadoc.jar ======

java {
    withSourcesJar()
    withJavadocJar()
}

// configure task javadocJar to take javadoc generated by dokkaJavadoc
tasks.named<Jar>("javadocJar") {
    from(tasks.named<DokkaTask>("dokkaJavadoc"))
}

// ====== analyse code with SonarQube ======

// required for sonarqube code coverage
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    // https://stackoverflow.com/questions/67725347/jacoco-fails-on-gradle-7-0-2-and-kotlin-1-5-10
    //version = "0.8.7"
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
    }
}

// gradle test jacocoTestReport sonarqube
// https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-gradle/
// configure token with 'publish analysis' permission in file ~/.gradle/gradle.properties:
// systemProp.sonar.login=<token>
// warning: The Report.destination property has been deprecated. This is scheduled to be removed in Gradle 8.0.
sonarqube {
    properties {
        property("sonar.projectKey", "gmuth_ipp-client-kotlin")
        property("sonar.organization", "gmuth")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks.sonarqube {
    dependsOn(tasks.jacocoTestReport) // for coverage
}