![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Build/badge.svg?branch=master)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Dev/badge.svg?)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Prod/badge.svg?)

# sosialhjelp-modia-api
Backend-app som skal vise personinformasjon i modia for sosialhjelp.

## Henvendelser
Henvendelser kan sendes via Slack i kanalen #digisos.

## Oppsett av nytt prosjekt
Dette prosjektet bygger og deployer vha Github Actions

### Github package registry
- NB: Fungerer foreløpig kun med personal access token, og tokenet må ha read og write access til packages.
- Docker image bygges på CircleCi og pushes til github package registry, eks [her](https://github.com/navikt/sosialhjelp-modia-api/packages/13432/versions)

### Github Actions
- Docker image bygges ved push => `.github/workflows/build.yml`
- Deploy til dev-fss => `.github/workflows/deploy-miljo.yml`
- Deploy til prod-fss => `.github/workflows/deploy-prod.yml`
- For å deploye til dev-sbs eller prod-sbs brukes av cli-verktøyet [sosialhjelp-ci](https://github.com/navikt/sosialhjelp-ci).

### Github deployment
- Github deployments - registrer ditt github-repo [her](https://deployment.prod-sbs.nais.io/auth/form)
- Deployments vises [her](https://github.com/navikt/sosialhjelp-modia-api/deployments)

### Vault
- Lag PR til `vault-iac` slik at man kan lagre secrets på vault.
- Denne må godkjennes og merges før man kan opprette secrets i din apps katalog `.../app/namespace`.
