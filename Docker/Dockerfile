FROM ubuntu:latest

RUN apt-get update

RUN apt-get install --no-install-recommends --no-install-suggests -y openjdk-8-jre
RUN apt-get install --no-install-recommends --no-install-suggests -y asterisk 

ENV CONFIGURATION_FILE /earlywarning/configuration/earlywarning.xml

ENV TRIGGERS_PORT  4445
ENV CONTACTS_PORT  6001
ENV HEARTBEAT_PORT 6002
ENV SIP_PORT 5060
ENV RTP_PORT_START 10000
ENV RTP_PORT_END 10100

EXPOSE ${TRIGGERS_PORT}
EXPOSE ${CONTACTS_PORT}
EXPOSE ${HEARTBEAT_PORT}
EXPOSE ${SIP_PORT}
EXPOSE ${RTP_PORT_START}-${RTP_PORT_END}

WORKDIR /earlywarning
VOLUME /earlywarning/configuration
VOLUME /etc/asterisk
VOLUME /usr/share/asterisk/sounds

COPY earlywarning /earlywarning

ENTRYPOINT /bin/bash run.sh