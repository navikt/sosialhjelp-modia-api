![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Build/badge.svg?branch=master)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Dev/badge.svg?)
![](https://github.com/navikt/sosialhjelp-modia-api/workflows/Deploy%20Prod/badge.svg?)

# sosialhjelp-modia-api
Backend-app som skal vise personinformasjon i modia for sosialhjelp.

## Henvendelser
Henvendelser kan sendes via Slack i kanalen #digisos.

## Oppsett av nytt prosjekt
Dette prosjektet bygger og deployer vha CircleCi og Github deployment

### Github package registry
- NB: Fungerer foreløpig kun med personal access token, og tokenet må ha read og write access til packages.
- Docker image bygges på CircleCi og pushes til github package registry, eks [her](https://github.com/navikt/sosialhjelp-modia-api/packages/13432/versions)

### CircleCi
- Logg inn på circleci.com med din Github-bruker. 
- Hvis Github-brukeren din er medlem i `navikt`, burde `navikt` dukke opp automatisk på CircleCi.
- Under 'Add projects' kan du finne ditt github-repo.
- Velg 'Set up project', og følg guiden.
- Vi bruker [sosialhjelp-ci](https://github.com/navikt/sosialhjelp-ci) for deploy til spesifikke miljø. Dette verktøyet bruker APIet til CircleCi til å trigge en job med gitte bygg-parametre (NB: funker kun for versjon 2.0 av CircleCi, ikke versjon 2.1)

### Github deployment
- Krever at appen bruker naiserator
- Github deployments - registrer ditt github-repo [her](https://deployment.prod-sbs.nais.io/auth/form)
- Deployments vises [her](https://github.com/navikt/sosialhjelp-modia-api/deployments)
- [deployment-cli](https://github.com/navikt/deployment-cli) blir brukt i CircleCi.

### Vault
- Lag PR til `vault-iac` slik at man kan lagre secrets på vault.
- Denne må godkjennes og merges før man kan opprette secrets i din apps katalog `.../app/namespace`.
