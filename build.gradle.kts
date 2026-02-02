import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.sosialhjelp"

plugins {
    alias(libs.plugins.versions)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.boot)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

ktlint {
    this.version.set(libs.versions.ktlint)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation(libs.bundles.coroutines)

    implementation(libs.bundles.spring.boot)

    implementation(libs.sosialhjelp.common.api)

//    Micrometer/prometheus
    implementation(libs.bundles.prometheus)
    implementation(libs.micrometer.registry.prometheus)

//    Logging
    implementation(libs.logback)
    implementation(libs.logstash.logback.encoder)

    implementation(libs.jackson.module.kotlin)

//    Auditlogger syslog
    implementation(libs.logback.syslog4j)
    implementation(libs.syslog4j)

//    Filformat
    implementation(libs.filformat)

//    Springdoc
    implementation(libs.bundles.springdoc)

//    Redis
    implementation(libs.lettuce)

//    Test
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockwebserver)
}

val githubUser: String? by project
val githubPassword: String? by project

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/*")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

kotlin {
    compilerOptions { freeCompilerArgs = listOf("-Xjsr305=strict") }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
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
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable() && !currentVersion.isNonStable()
    }
}
