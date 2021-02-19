FROM navikt/java:11-appdynamics

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY /nais/scripts /init-scripts
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"
RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /init-scripts/0-dump-memory-config.sh