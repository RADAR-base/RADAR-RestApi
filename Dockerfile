# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:8-alpine as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_OPTS -Dorg.gradle.daemon=false -Dorg.gradle.project.profile=prod

COPY ./gradle/wrapper /code/gradle/wrapper
COPY ./gradlew /code/
RUN ./gradlew --version

COPY ./gradle/prod.gradle /code/gradle/
COPY ./build.gradle ./gradle.properties ./settings.gradle /code/

RUN ./gradlew downloadWarDependencies

COPY ./src/ /code/src

RUN ./gradlew war

FROM tomcat:8.0.47-jre8-alpine

MAINTAINER @yatharthranjan, @blootsvoets

LABEL description="RADAR-CNS Rest Api docker container"

COPY --from=builder /code/build/libs/radar.war /usr/local/tomcat/webapps/radar.war
COPY src/main/docker/classpath.xml /usr/local/tomcat/conf/Catalina/localhost/radar.xml

VOLUME /usr/local/tomcat/conf/radar

EXPOSE 8080

CMD ["catalina.sh", "run"]
