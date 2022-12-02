import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.sosialhjelp"

object Versions {
    const val kotlin = "1.7.20"
    const val coroutines = "1.6.4"
    const val springBoot = "2.7.5"
    const val logback = "1.4.4"
    const val logstash = "7.2"
    const val sosialhjelpCommon = "1.20221019.1049-614783b"
    const val filformat = "1.2022.10.21-12.33-ed1a97d500e4"
    const val micrometerRegistry = "1.10.0"
    const val prometheus = "0.16.0"
    const val tokenValidation = "2.1.7"
    const val jackson = "2.14.0"
    const val guava = "31.1-jre"
    const val logbackSyslog4j = "1.0.0"
    const val javaJwt = "4.2.1"
    const val jwksRsa = "0.21.2"
    const val syslog4j = "0.9.46"
    const val lettuce = "6.2.1.RELEASE"
    const val unleash = "4.4.1"
    const val springdoc = "1.6.12"

    const val jsonSmart = "2.4.8"
    const val gson = "2.9.1"
    const val log4j = "2.19.0"
    const val snakeyaml = "1.33"

    // Test only
    const val junitJupiter = "5.9.1"
    const val mockk = "1.13.2"
    const val junit = "4.13.2"
}

plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
    id("org.springframework.boot") version "2.7.5"
    id("com.github.ben-manes.versions") version "0.42.0" // ./gradlew dependencyUpdates
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

ktlint {
    this.version.set("0.45.2")
}

configurations {
    "implementation" {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        exclude(group = "javax.activation", module = "activation")
        exclude(group = "javax.mail", module = "mailapi")
        exclude(group = "javax.validation", module = "validation-api")
    }
    "testImplementation" {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        exclude(group = "org.hamcrest", module = "hamcrest-library")
        exclude(group = "org.hamcrest", module = "hamcrest-core")
        exclude(group = "org.mockito", module = "mockito-core")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

//    Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Versions.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.coroutines}")

//    Spring
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-webflux:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-jetty:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:${Versions.springBoot}")

//    Sosialhjelp-common
    implementation("no.nav.sosialhjelp:sosialhjelp-common-selftest:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-api:${Versions.sosialhjelpCommon}")

//    Micrometer/prometheus
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.0")
    implementation("io.prometheus:simpleclient_common:${Versions.prometheus}")
    implementation("io.prometheus:simpleclient_hotspot:${Versions.prometheus}")

//    Logging
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstash}")

//    Auditlogger syslog
    implementation("com.papertrailapp:logback-syslog4j:${Versions.logbackSyslog4j}")
    implementation("org.syslog4j:syslog4j:${Versions.syslog4j}")

//    JWT
    implementation("com.auth0:java-jwt:${Versions.javaJwt}")
    implementation("com.auth0:jwks-rsa:${Versions.jwksRsa}")

//    Filformat
    implementation("no.nav.sbl.dialogarena:soknadsosialhjelp-filformat:${Versions.filformat}")

//    Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

//    Token-validation
    implementation("no.nav.security:token-validation-spring:${Versions.tokenValidation}")
    implementation("no.nav.security:token-client-spring:${Versions.tokenValidation}")

//    Springdoc
    implementation("org.springdoc:springdoc-openapi-ui:${Versions.springdoc}")

//    Unleash
    implementation("no.finn.unleash:unleash-client-java:${Versions.unleash}")

//    Redis
    implementation("io.lettuce:lettuce-core:${Versions.lettuce}")

//    Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("no.nav.security:token-validation-spring-test:${Versions.tokenValidation}")

//    Spesifikke versjoner oppgradert etter ønske fra snyk
    constraints {
        implementation("com.google.guava:guava:${Versions.guava}") {
            because("Snyk ønsker 30.1.1-jre eller høyere.")
        }
        implementation("net.minidev:json-smart:${Versions.jsonSmart}") {
            because("Snyk ønsker 2.4.5 eller høyere.")
        }
        implementation("com.google.code.gson:gson:${Versions.gson}") {
            because("Snyk ønsker 2.8.9 eller høyere. Transitiv avhengighet dratt inn av unleash-client-java.")
        }
        testImplementation("junit:junit:${Versions.junit}") {
            because("Snyk ønsker 4.13.1 eller høyere. Transitiv avhengighet dratt inn av token-validation-spring-test.")
        }
        implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }
        implementation("org.apache.logging.log4j:log4j-to-slf4j:${Versions.log4j}") {
            because("0-day exploit i version 2.0.0-2.14.1")
        }

        implementation("org.yaml:snakeyaml:${Versions.snakeyaml}") {
            because("Snyk ønsker 1.31 eller høyere")
        }
    }
}

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/sosialhjelp-common")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable() && !currentVersion.isNonStable()
    }
}
