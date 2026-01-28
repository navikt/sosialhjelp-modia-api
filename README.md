[![Build image](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/build.yml)
[![Deploy til prod-gcp](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/deploy_prod.yml/badge.svg)](https://github.com/navikt/sosialhjelp-modia-api/actions/workflows/deploy_prod.yml)

# sosialhjelp-modia-api
Backend-app som skal gi innsyn i sosialhjelp-saker for saksbehandlere ved NKS.

## Henvendelser
Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For Nav-ansatte
Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

## Teknologi
* Kotlin
* JDK 17
* Gradle
* Spring-boot
* Redis (cache)

### Krav
- JDK 17

## Bygging og kjøring av tester
Bygge og kjør tester ved å kjøre: `./gradlew test`

### Lokal kjøring mot mock-alt
Kjør `Application.kt` med springprofilene `mock-alt,log-console,no-redis,local`.\

#### Med redis
Kjør `Application.kt` med springprofilene `mock-alt,log-console`\
Sett env-variablene `REDIS_HOST=localhost` og `REDIS_PASSWORD=<lokal_redis_pw>` for å gå mot lokal redis (f.eks bitnami redis docker image)

### Github package registry
- Docker image pushes til github package registry [https://github.com/navikt/sosialhjelp-modia-api/packages/](https://github.com/navikt/sosialhjelp-modia-api/packages/)

### Github Actions
- Docker image bygges ved push: `.github/workflows/build.yml`
- Deploy til dev: `.github/workflows/deploy_dev.yml`
- Deploy til prod: `.github/workflows/deploy_prod.yml`

## Hvordan komme i gang
[Felles dokumentasjon for våre backend apper](https://teamdigisos.intern.nav.no/docs/utviklerdokumentasjon/kom%20igang%20med%20utvikling#backend-gradle)
