# --- WIREMOCK ---

FROM openjdk:8-jre-alpine
MAINTAINER https://github.com/yatharthranjan

ARG WM_VERSION=2.7.1

RUN mkdir /wiremock
WORKDIR /wiremock
ADD "https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/${WM_VERSION}/wiremock-standalone-${WM_VERSION}.jar" wiremock.jar

EXPOSE 8089

ENTRYPOINT ["java","-jar","wiremock.jar","--port","8089","--verbose"]