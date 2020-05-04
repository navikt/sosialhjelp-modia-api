import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.sbl"

object Versions {
    const val kotlin = "1.3.70"
    const val springBoot = "2.2.6.RELEASE"
    const val logback = "1.2.3"
    const val logstash = "6.3"
    const val filformat = "1.2020.01.09-15.55-f18d10d7d76a"
    const val micrometerRegistry = "1.3.5"
    const val prometheus = "0.8.1"
    const val tokenValidation = "1.1.4"
    const val jackson = "2.10.3"
    const val jacksonDatabind = "2.10.3"
    const val guava = "28.2-jre"
    const val swagger = "2.9.2"
    const val resilience4j = "1.3.1"
    const val rxKotlin = "2.4.0"
    const val vavrKotlin = "0.10.2"
    const val ktor = "1.3.1"
    const val kotlinCoroutines = "1.3.3"
    const val abacAttributeConstants = "3.3.13"

    // Test only
    const val junitJupiter = "5.6.0"
    const val mockk = "1.9.3"
}

val mainClass = "no.nav.sbl.sosialhjelpmodiaapi.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.3.70"
//    id("org.jmailen.kotlinter") version "2.3.1" // TODO - burde tas i bruk
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.70"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.github.ben-manes.versions") version "0.28.0"
}

application {
    applicationName = "sosialhjelp-modia-api"
    mainClassName = mainClass
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configurations {
    "implementation" {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    "testImplementation" {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        exclude(group = "junit", module = "junit")
        exclude(group = "org.hamcrest", module = "hamcrest-library")
        exclude(group = "org.hamcrest", module = "hamcrest-core")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("io.ktor:ktor-server-netty:${Versions.ktor}")
    implementation("io.ktor:ktor-auth:${Versions.ktor}")
    implementation("io.ktor:ktor-auth-jwt:${Versions.ktor}")
    implementation("io.ktor:ktor-jackson:${Versions.ktor}")
    implementation("io.ktor:ktor-client-core:${Versions.ktor}")
    implementation("io.ktor:ktor-client-apache:${Versions.ktor}")
    implementation("io.ktor:ktor-client-json:${Versions.ktor}")
    implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Versions.kotlinCoroutines}")

    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-jetty:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-security:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")

    implementation("io.micrometer:micrometer-registry-prometheus:${Versions.micrometerRegistry}")
    implementation("io.prometheus:simpleclient_common:${Versions.prometheus}")
    implementation("io.prometheus:simpleclient_hotspot:${Versions.prometheus}")

    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstash}")

    implementation("no.nav.sbl.dialogarena:soknadsosialhjelp-filformat:${Versions.filformat}")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")

    implementation("no.nav.security:token-validation-spring:${Versions.tokenValidation}")
    implementation("io.springfox:springfox-swagger2:${Versions.swagger}")
    implementation("io.springfox:springfox-swagger-ui:${Versions.swagger}")

    implementation("no.nav.abac.policies:abac-attribute-constants:${Versions.abacAttributeConstants}")

    //spesifikke versjoner oppgradert etter ønske fra snyk
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}")
    implementation("com.google.guava:guava:${Versions.guava}")

    //selftest
    implementation ("io.github.resilience4j:resilience4j-kotlin:${Versions.resilience4j}")
    implementation ("io.github.resilience4j:resilience4j-timelimiter:${Versions.resilience4j}")
    implementation ("io.github.resilience4j:resilience4j-circuitbreaker:${Versions.resilience4j}")
    implementation ("io.reactivex.rxjava2:rxkotlin:${Versions.rxKotlin}")
    implementation ("io.vavr:vavr-kotlin:${Versions.vavrKotlin}")

    //Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}") {
        exclude(group = "org.mockito", module = "mockito-core}")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("no.nav.security:token-validation-test-support:${Versions.tokenValidation}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}")
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2/")
    maven("https://repo.spring.io/plugins-release/")
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
        classifier = ""
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