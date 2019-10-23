FROM navikt/java:11

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY sosialhjelp-modia-api-*.jar app.jar
