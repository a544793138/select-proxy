plugins {
    java
    id("org.springframework.boot") version "2.4.0"
}

group = "com.tjwoods"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.0.3")
    implementation("org.mitre.dsmiley.httpproxy:smiley-http-proxy-servlet:1.11")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    testImplementation("junit", "junit", "4.12")
    implementation(kotlin("script-runtime"))
}

tasks {

    bootJar {
        dependsOn(":clean")
        archiveFileName.set("select-proxy.jar")
        mainClass.set("com.tjwoods.Application")

        exclude("application.properties")

        doLast {
            copy {
                from("src/main/resources")
                include("application.properties")
                into("build/libs/")
            }
        }
    }
}
