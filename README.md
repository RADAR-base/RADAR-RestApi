# RADAR-CNS REST-API

[![Build Status](https://travis-ci.org/RADAR-CNS/RADAR-RestApi.svg?branch=master)](https://travis-ci.org/RADAR-CNS/RADAR-RestApi) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/622b8036e0a5420db5206cdcd55bbd11)](https://www.codacy.com/app/RADAR-CNS/RADAR-RestApi?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RADAR-CNS/RADAR-RestApi&amp;utm_campaign=Badge_Grade) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/622b8036e0a5420db5206cdcd55bbd11)](https://www.codacy.com/app/RADAR-CNS/RADAR-RestApi?utm_source=github.com&utm_medium=referral&utm_content=RADAR-CNS/RADAR-RestApi&utm_campaign=Badge_Coverage)

A REST-FULL Service using Tomcat 8.0.37, MongoDb 3.2.10, Swagger 2.0, Apache Avro 1.7.7 and Jersey 2.

Thi project implents the downstream REST API for the RADAR-CNS project'

## Setup
This project uses `git submodule`. When cloning, please use the command `git clone --recursive`. For already cloned repos, use `git submodule update --init --recursive` to update modules.

To deploy the war do:
- edit the `device-catalog.yml` configuration file and specify its location in `radar.yml`
- edit the `radar.yml` config file and then copy it your config folder. Paths checked to find the config file are
  - `/usr/share/tomcat8/conf/`
  - `/usr/local/tomcat/conf/radar/`
- run `./gradlew build`
- Copy the radar.war located at `build/libs/` in `/usr/local/tomcat/webapp/`

By default, log messages are redirected to the `STDOUT`.

The api documentation is located at `<your-server-address>:<port>/radar/api/swagger.json`

## Dev Environment
Click [here](http://radar-restapi.eu-west-1.elasticbeanstalk.com/api/swagger.json) to see documentation of dev deploy instance.

Click [here](http://radar-restapi.eu-west-1.elasticbeanstalk.com/api/user/getAllPatients/0) to see some mock data.

## Clients
Swagger provides a tool to automatically generate a client in several programming language.
- Access this [link](http://editor.swagger.io)
- Click on `File / Import URL`
- Paste `http://radar-restapi.eu-west-1.elasticbeanstalk.com/api/swagger.json` 
- Click on `Import`
- Click on `Generate Client` and select your programming language

## Integration test
Useful for testing the integration between `RADAR-CNS Hotstorage` and `RADAR-CNS Rest API`. Before running the test, add `127.0.0.1	hotstorage` to the `hosts` file.
To run the test:
- `./gradlew clean`
- `./gradlew build`
- `cd src/integrationTest`
- `docker-compose up -d --build`
- `cd -`
- `./gradlew integrationTest`
- `cd -`
- `docker-compose down`

## End to end test
This project contains an end to end test for the RADAR-CNS platform covering:
- `Confluent Rest-Proxy`
- `Kafka infrastructure`
- `RADAR-CNS kafka streams application`
- `RADAR-CNS MongoDb connector`
- `RADAR-CNS Hotstorage`
- `RADAR-CNS Rest API`

Infrastructure settings are located at `src/endToEndTest/resources/pipeline.yml`.
Test case settings are located at `src/endToEndTest/resources/pipeline.yml`. Each test case is specified as:
```yaml
- topic: android_empatica_e4_acceleration
  sensor: ACCELEROMETER
  frequency: 32.0
  file: accelerometer.csv
  key_schema: org.radarcns.key.MeasurementKey
  value_schema: org.radarcns.empatica.EmpaticaE4Acceleration
  values_to_test: "x, y, z"
  magnitude: 10
```
The test will generate random data and stream it to the landing topic specified by `topic` having for key `key_schema` and for value `value_schema`. Data for the sensor type `sensor` are randomly generated according to the `frequency`: number of messages generated per second. `value_to_test` are the variables list which will be tested against the RESTfull service. Since we are comparing `double`s, `magnitude` states a negative power of 10 representing the maximum delta between expected and actual values for which both numbers are still considered equal.

To run the test:
- `./gradlew clean`
- `./gradlew build`
- `cd src/integrationTest/image`
- `docker image build .`
- `cd -`
- `./gradlew setRadarEnvironmentForDocker`
- `cd src/endToEndTest/dockerRadar/dcompose-stack/radar-cp-hadoop-stack`
- `./install-radar-stack.sh`
- `cd -`
- `./gradlew endToEndTest`
- `cd -`
- `./stop-radar-stack.sh`

## Contributing
Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html). If you want to contribute a feature or fix, please make a pull request.
