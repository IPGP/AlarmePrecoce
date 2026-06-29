# build Maven (JDK 8)
FROM maven:3.9-eclipse-temurin-8 AS builder
WORKDIR /build

COPY EarlyWarning/pom.xml ./pom.xml
COPY EarlyWarning/lib ./lib

RUN mvn -B -q validate || true
RUN mvn -B -q dependency:go-offline || true

COPY EarlyWarning/src ./src
RUN mvn -B validate && mvn -B package -DskipTests

# runtime (Asterisk + JRE 8 + supervisord)
FROM ubuntu:22.04 AS runtime

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
        asterisk \
        openjdk-8-jre-headless \
        supervisor \
        ca-certificates \
        tzdata \
        vim \
        netcat-traditional \
        iputils-ping \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /earlywarning
COPY --from=builder /build/target/EarlyWarning.jar /earlywarning/EarlyWarning.jar
COPY --from=builder /build/src/main/resources /earlywarning/resources

COPY Docker/asterisk/sounds/ /usr/share/asterisk/sounds/

COPY TriggerSender//src/main/triggersender.sh /usr/local/bin/triggersender.sh
RUN chmod +x /usr/local/bin/triggersender.sh

COPY Docker/supervisord.conf /etc/supervisor/supervisord.conf
COPY Docker/entrypoint.sh    /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# triggers/udp, web, heartbeat, SIP, RTP
EXPOSE 4445/udp 6001/tcp 6002/tcp 5060/udp 5060/tcp
EXPOSE 10000-10100/udp

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]