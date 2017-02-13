# RADAR-CNS REST-API

[![Build Status](https://travis-ci.org/RADAR-CNS/RADAR-RestApi.svg?branch=master)](https://travis-ci.org/RADAR-CNS/RADAR-RestApi) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/622b8036e0a5420db5206cdcd55bbd11)](https://www.codacy.com/app/RADAR-CNS/RADAR-RestApi?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RADAR-CNS/RADAR-RestApi&amp;utm_campaign=Badge_Grade)

A REST-FULL Service using Tomcat 8.0.37, MongoDb 3.2.10, Swagger 2.0, Apache Avro 1.7.7 and Jersey 2.

Thi project implents the downstream REST API for the RADAR-CNS project'

## Setup

Before deploying the war file edit the `radar.yml` config file and then copy it into `/usr/local/tomcat/conf/`. If your installation uses a different path you need to modify the variable `pathFile` in `org.radarcns.config.Properties` After that
- Run `./gradlew build`
- Copy the radar.war located at `build/libs/` in `/usr/local/tomcat/webapp/`

The application log file is located at `/usr/local/tomcat/log/radar-restapi.log`. You need to modify this location, update the file tag in `/resources/logback.xml`
The log `rolling policy` entails you have a folder named `archived` under your log pah. If you do not have it, you must create it before running the project.

The api documentation is located at `<your-server-address>:<port>/radar/api/swagger.json`

## Dev Environment
Click [here](http://radar-restapi.eu-west-1.elasticbeanstalk.com/api/swagger.json) to see documentation of dev deploy instance.

Click [here](http://radar-restapi.eu-west-1.elasticbeanstalk.com/api/User/GetAllPatients) to see some mock data.

## Clients
Swagger provides a tool to automatically generate a client in several programming language.
- Access this [link](http://editor.swagger.io)
- Click on `File / Import URL`
- Paste `http://radar-restapi.eu-west-1.elasticbeanstalk.com/api/swagger.json` 
- Click on `Import`
- Click on `Generate Client` and select your programming language

## Contributing

Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html). If you want to contribute a feature or fix, please make a pull request
