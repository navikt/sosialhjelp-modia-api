[![Build image](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/build.yml)
[![Deploy til prod-fss](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/deploy_prod.yml/badge.svg)](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/deploy_prod.yml)

# sosialhjelp-modia-api
Backend-app som skal gi innsyn i sosialhjelp-saker for saksbehandlere ved NKS.

## Henvendelser
Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

## Teknologi
* Kotlin
* JDK 17
* Gradle
* Spring-boot
* navikt/token-support
* Redis (cache)

### Krav
- JDK 17

## Bygging og kjøring av tester
Bygge og kjør tester ved å kjøre: `./gradlew test`

### Lokal kjøring mot mock-alt
Kjør `Application.kt` med springprofilene `mock-alt,log-console,no-redis`.\
Sett dummy env-variabel-verdier for `SRVSOSIALHJELP_MODIA_API_USERNAME` og `SRVSOSIALHJELP_MODIA_API_PASSWORD`

#### Med redis
Kjør `Application.kt` med springprofilene `mock-alt,log-console`\
Sett env-variablene `REDIS_HOST=localhost` og `REDIS_PASSWORD=<lokal_redis_pw>` for å gå mot lokal redis (f.eks bitnami redis docker image)

### Lokal kjøring med integrasjon mot KS
Kjør `TestApplication.kt` med springprofilene `local,log-console` for integrasjon mot KS sitt testmiljø (lenge siden dette er testet).

## Hvordan komme i gang
### Hente github-package-registry pakker fra NAV-IT
Enkelte pakker brukt i repoet er lastet opp til Github Package Registry, som krever autentisering for å kunne lastes ned.
Ved bruk av f.eks Gradle, kan det løses slik:
```
val githubUser: String by project
val githubPassword: String by project
repositories {
    maven {
        credentials {
            username = githubUser
            password = githubPassword
        }
        setUrl("https://maven.pkg.github.com/navikt/sosialhjelp-common")
    }
}
```

`githubUser` og `githubPassword` er da properties som settes i `~/.gradle/gradle.properties`:

```                                                     
githubUser=x-access-token
githubPassword=<token>
```

Hvor `<token>` er et personal access token med scope `read:packages`. Husk å enable SSO for tokenet.

Alternativt kan variablene kan også konfigureres som miljøvariabler, eller brukes i kommandolinjen:

* `ORG_GRADLE_PROJECT_githubUser`
* `ORG_GRADLE_PROJECT_githubPassword`

```
./gradlew -PgithubUser=x-access-token -PgithubPassword=[token]
```

### Ktlint
Hvordan kjøre Ktlint:
* Fra IDEA: Kjør Gradle Task: sosialhjelp-modia-api -> Tasks -> formatting -> ktlintFormat
* Fra terminal:
    * Kun formater: `./gradlew ktlintFormat`
    * Formater og bygg: `./gradlew ktlintFormat build`
    * Hvis IntelliJ begynner å hikke, kan en kjøre `./gradlew clean ktlintFormat build`

Endre IntelliJ autoformateringskonfigurasjon for dette prosjektet:
* `./gradlew ktlintApplyToIdea`

Legg til pre-commit check/format hooks:
* `./gradlew addKtlintCheckGitPreCommitHook`
* `./gradlew addKtlintFormatGitPreCommitHook`

### Github package registry
- Docker image pushes til github package registry [https://github.com/navikt/sosialhjelp-modia-api/packages/](https://github.com/navikt/sosialhjelp-modia-api/packages/)

### Github Actions
- Docker image bygges ved push: `.github/workflows/build.yml`
- Deploy til dev: `.github/workflows/deploy_dev.yml`
- Deploy til prod: `.github/workflows/deploy_prod.yml`
- Redis: Endringer i `redis-config.yml` eller `redisexporter.yml` i andre brancher enn `master` gir autodeploy til dev-fss, og endringer på `master` gir autodeploy til prod-fss.

