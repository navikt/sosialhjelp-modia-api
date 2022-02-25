import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.sosialhjelp"

object Versions {
    const val kotlin = "1.6.10"
    const val coroutines = "1.6.0"
    const val springBoot = "2.6.3"
    const val logback = "1.2.10"
    const val logstash = "7.0.1"
    const val sosialhjelpCommon = "1.4204e7e"
    const val filformat = "1.2022.01.30-13.18-01cd95216e0b"
    const val micrometerRegistry = "1.8.3"
    const val prometheus = "0.14.0"
    const val tokenValidation = "1.3.19"
    const val jackson = "2.13.1"
    const val guava = "30.1.1-jre"
    const val abacAttributeConstants = "3.3.13"
    const val logbackSyslog4j = "1.0.0"
    const val syslog4j = "0.9.30"
    const val lettuce = "6.1.6.RELEASE"
    const val unleash = "3.3.4"
    const val springdoc = "1.6.6"
    const val jsonSmart = "2.4.7"
    const val gson = "2.8.9"
    const val junit = "4.13.2"
    const val log4j = "2.17.1"

    // Test only
    const val junitJupiter = "5.8.2"
    const val mockk = "1.12.2"
    const val mockwebserver = "5.0.0-alpha.2"
}

plugins {
    application
    kotlin("jvm") version "1.6.10"

    id("org.jetbrains.kotlin.plugin.spring") version "1.6.10"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

application {
    applicationName = "sosialhjelp-modia-api"
    mainClass.set("no.nav.sosialhjelp.modia.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

ktlint {
    this.version.set("0.41.0")
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
    implementation("no.nav.sosialhjelp:sosialhjelp-common-kotlin-utils:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-kommuneinfo-client:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-idporten-client:${Versions.sosialhjelpCommon}")

//    Micrometer/prometheus
    implementation("io.micrometer:micrometer-registry-prometheus:${Versions.micrometerRegistry}")
    implementation("io.prometheus:simpleclient_common:${Versions.prometheus}")
    implementation("io.prometheus:simpleclient_hotspot:${Versions.prometheus}")

//    Logging
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstash}")

//    Auditlogger syslog
    implementation("com.papertrailapp:logback-syslog4j:${Versions.logbackSyslog4j}")
    implementation("org.syslog4j:syslog4j:${Versions.syslog4j}")

//    Filformat
    implementation("no.nav.sbl.dialogarena:soknadsosialhjelp-filformat:${Versions.filformat}")

//    Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")

//    Token-validation
    implementation("no.nav.security:token-validation-spring:${Versions.tokenValidation}")
    implementation("no.nav.security:token-client-spring:${Versions.tokenValidation}")

//    Springdoc
    implementation("org.springdoc:springdoc-openapi-ui:${Versions.springdoc}")

//    Abac-attributter
    implementation("no.nav.abac.policies:abac-attribute-constants:${Versions.abacAttributeConstants}")

//    Unleash
    implementation("no.finn.unleash:unleash-client-java:${Versions.unleash}")

//    Redis
    implementation("io.lettuce:lettuce-core:${Versions.lettuce}")

//    Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("no.nav.security:token-validation-spring-test:${Versions.tokenValidation}")
    testImplementation("com.squareup.okhttp3:mockwebserver3-junit5:${Versions.mockwebserver}")

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

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-XXLanguage:+InlineClasses")
        }
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        testLogging {
            events("skipped", "failed")
        }
    }

    withType<ShadowJar> {
        archiveClassifier.set("")
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }
}
