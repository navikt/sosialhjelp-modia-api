![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Build/badge.svg?branch=master)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Dev/badge.svg?)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Prod/badge.svg?)

# sosialhjelp-modia-api
Backend-app som skal gi innsyn i sosialhjelp-saker for saksbehandlere ved NKS.

## Henvendelser
Henvendelser kan sendes via Slack i kanalen #digisos.

## Oppsett av nytt prosjekt
Prosjektet bruker Github Actions for bygg og deploy

### Github package registry
- Docker image pushes til github package registry [https://github.com/navikt/sosialhjelp-modia-api/packages/](https://github.com/navikt/sosialhjelp-modia-api/packages/)

### Github Actions
- Docker image bygges ved push => `.github/workflows/build.yml`
- Deploy til dev-fss => `.github/workflows/deploy-miljo.yml`
- Deploy til prod-fss => `.github/workflows/deploy-prod.yml`
- For å deploye til dev-fss eller prod-fss brukes av cli-verktøyet [sosialhjelp-ci](https://github.com/navikt/sosialhjelp-ci).

### Github deployment
- Github deployments - registrer ditt github-repo [her](https://deployment.prod-sbs.nais.io/auth/form)
- Deployments vises [her](https://github.com/navikt/sosialhjelp-modia-api/deployments)

### Vault
- Lag PR til `vault-iac` slik at man kan lagre secrets på vault.
- Denne må godkjennes og merges før man kan opprette secrets i din apps katalog `.../app/namespace`.

## Lokal kjøring
Bruk spring profile `local` eller `mock`.

#### Environment variabler
Sett dummy-verdier for `SRVSOSIALHJELP_MODIA_API_USERNAME` og `SRVSOSIALHJELP_MODIA_API_PASSWORD`
