import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.sbl"

object Versions {
    const val kotlin = "1.4.21"
    const val coroutines = "1.4.2"
    const val springBoot = "2.4.2"
    const val logback = "1.2.3"
    const val logstash = "6.5"
    const val sosialhjelpCommon = "1.14b3a11"
    const val filformat = "1.2020.11.05-09.32-14af05dea965"
    const val micrometerRegistry = "1.6.2"
    const val prometheus = "0.9.0"
    const val tokenValidation = "1.3.2"
    const val jackson = "2.12.0"
    const val guava = "30.1-jre"
    const val springfox = "3.0.0"
    const val abacAttributeConstants = "3.3.13"
    const val logbackSyslog4j = "1.0.0"
    const val syslog4j = "0.9.30"
    const val jerseyMediaJaxb = "2.31"
    const val lettuce = "6.0.2.RELEASE"
    const val unleash = "3.3.4"

    // Test only
    const val junitJupiter = "5.7.0"
    const val mockk = "1.10.3"
}

val applicationKt = "no.nav.sbl.sosialhjelpmodiaapi.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.4.21"
    id("org.jmailen.kotlinter") version "3.3.0"
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.github.ben-manes.versions") version "0.28.0"
}

application {
    applicationName = "sosialhjelp-modia-api"
    mainClassName = applicationKt // TODO: erstatt med "mainClass.set(applicationKt)" når denne merges https://github.com/johnrengelman/shadow/pull/612
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${Versions.coroutines}")

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

//    Springfox/swagger
    implementation("io.springfox:springfox-boot-starter:${Versions.springfox}")

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
    testImplementation("no.nav.security:token-validation-test-support:${Versions.tokenValidation}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}")

//    Spesifikke versjoner oppgradert etter ønske fra snyk
    constraints {
        implementation("com.google.guava:guava:${Versions.guava}") {
            because("Transitiv avhengighet dratt inn av jedis-mock@0.1.16 har sårbarhet. Constraintsen kan fjernes når jedis-mock bruker guava@30.0-jre eller nyere")
        }

        //  Test
        testImplementation("org.glassfish.jersey.media:jersey-media-jaxb:${Versions.jerseyMediaJaxb}") {
            because("Transitiv avhengighet dratt inn av token-validation-test-support@1.3.1 har sårbarhet. Constraintsen kan fjernes når token-validation-test-support bruker jersey-media-jaxb@2.31 eller nyere")
        }
    }
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

kotlinter {
    ignoreFailures = false
    indentSize = 4
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = false
    disabledRules = emptyArray<String>()
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

    check {
        dependsOn("installKotlinterPrePushHook")
    }
}
