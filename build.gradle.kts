import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.sbl"

object Versions {
    const val kotlin = "1.4.0"
    const val coroutines = "1.3.9"
    const val springBoot = "2.3.3.RELEASE"
    const val logback = "1.2.3"
    const val logstash = "6.3"
    const val sosialhjelpCommon = "1.a615c63"
    const val filformat = "1.2020.06.25-09.12-23b98d57ab78"
    const val micrometerRegistry = "1.5.1"
    const val prometheus = "0.8.1"
    const val tokenValidation = "1.3.0"
    const val jackson = "2.11.0"
    const val guava = "28.2-jre"
    const val swagger = "2.9.2"
    const val abacAttributeConstants = "3.3.13"
    const val nettyCodec = "4.1.50.Final"
    const val logbackSyslog4j = "1.0.0"
    const val syslog4j = "0.9.30"
    const val jerseyMediaJaxb = "2.31"
    const val redisMock = "0.1.16"
    const val lettuce = "5.3.1.RELEASE"

    // Test only
    const val junitJupiter = "5.6.0"
    const val mockk = "1.10.0"
}

val mainClass = "no.nav.sbl.sosialhjelpmodiaapi.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.4.0"
//    id("org.jmailen.kotlinter") version "2.3.1" // TODO - burde tas i bruk
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
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
        exclude(group = "org.mockito", module = "mockito-core")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

//    Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

//    Spring
    implementation("org.springframework.boot:spring-boot-starter-web:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-jetty:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-actuator:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-logging:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${Versions.springBoot}")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:${Versions.springBoot}")

//    Sosialhjelp-common
    implementation("no.nav.sosialhjelp:sosialhjelp-common-selftest:${Versions.sosialhjelpCommon}")
    implementation("no.nav.sosialhjelp:sosialhjelp-common-api:${Versions.sosialhjelpCommon}")
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

//    Swagger
    implementation("io.springfox:springfox-swagger2:${Versions.swagger}")
    implementation("io.springfox:springfox-swagger-ui:${Versions.swagger}")

//    Abac-attributter
    implementation("no.nav.abac.policies:abac-attribute-constants:${Versions.abacAttributeConstants}")

//    Redis
    implementation("io.lettuce:lettuce-core:${Versions.lettuce}")
    implementation("com.github.fppt:jedis-mock:${Versions.redisMock}")

//    Spesifikke versjoner oppgradert etter ønske fra snyk
    implementation("com.google.guava:guava:${Versions.guava}")
    implementation("io.netty:netty-codec-http2:${Versions.nettyCodec}")
    implementation("org.glassfish.jersey.media:jersey-media-jaxb:${Versions.jerseyMediaJaxb}")

//    Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:${Versions.springBoot}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("no.nav.security:token-validation-test-support:${Versions.tokenValidation}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}")
}

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
    jcenter()
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
