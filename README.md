![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Build%20image/badge.svg?branch=master)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Dev/badge.svg?)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Prod/badge.svg?)

# sosialhjelp-modia-api
Backend-app som skal gi innsyn i sosialhjelp-saker for saksbehandlere ved NKS.

## Henvendelser
Henvendelser kan sendes via Slack i kanalen #digisos.

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
* Fra IDEA: Kjør Gradle Task: sosialhjelp-mock-alt-api -> Tasks -> formatting -> ktlintFormat
* Fra terminal:
    * Kun formater: `./gradlew ktlintFormat`
    * Formater og bygg: `./gradlew ktlintFormat build`
    * Hvis IntelliJ begynner å hikke, kan en kjøre `./gradlew clean ktlintFormat build`

Endre IntelliJ autoformateringskonfigurasjon for dette prosjektet:
* `./gradlew ktlintApplyToIdea`

Legg til pre-commit check/format hooks:
* `./gradlew addKtlintCheckGitPreCommitHook`
* `./gradlew addKtlintFormatGitPreCommitHook`

## Oppsett av nytt prosjekt
Prosjektet bruker Github Actions for bygg og deploy

### Github package registry
- Docker image pushes til github package registry [https://github.com/navikt/sosialhjelp-modia-api/packages/](https://github.com/navikt/sosialhjelp-modia-api/packages/)

### Github Actions
- Docker image bygges ved push => `.github/workflows/build.yml`
- Deploy til dev => `.github/workflows/deploy_dev.yml`
- Deploy til prod => `.github/workflows/deploy_prod.yml`

### Github deployment
- Github deployments - registrer ditt github-repo [her](https://deployment.prod-sbs.nais.io/auth/form)
- Deployments vises [her](https://github.com/navikt/sosialhjelp-modia-api/deployments)

### Vault
- Lag PR til `vault-iac` slik at man kan lagre secrets på vault.
- Denne må godkjennes og merges før man kan opprette secrets i din apps katalog `.../app/namespace`.

## Redis
Applikasjonen bruker Redis for caching. Endringer i `redis-config.yml` eller `redisexporter.yml` i andre brancher enn `master` gir autodeploy til dev-fss, og endringer på `master` gir autodeploy til prod-fss.
Samtidig kan man manuelt deploy redis-instanser med eks `kubectl apply -f nais/redis-config.yml`.

### Lokal kjøring og redis
Legg til spring-profil `no-redis` for å disable redis.
Sett env-variablene `REDIS_HOST=localhost` og `REDIS_PASSWORD=<lokal_redis_pw>` for å gå mot lokal redis (f.eks redis docker image)

## Lokal kjøring
Bruk spring profile `local` eller `mock`.

#### Environment variabler
Sett dummy-verdier for `SRVSOSIALHJELP_MODIA_API_USERNAME` og `SRVSOSIALHJELP_MODIA_API_PASSWORD`
