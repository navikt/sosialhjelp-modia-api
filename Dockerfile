FROM navikt/java:11-appdynamics

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY /nais/scripts /init-scripts

COPY build/libs/*.jar app.jar