# RADAR-CNS REST-API

[![Build Status](https://api.travis-ci.org/RADAR-base/RADAR-RestApi.svg?branch=master)](https://travis-ci.org/RADAR-base/RADAR-RestApi) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/622b8036e0a5420db5206cdcd55bbd11)](https://www.codacy.com/app/RADAR-CNS/RADAR-RestApi?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=RADAR-CNS/RADAR-RestApi&amp;utm_campaign=Badge_Grade) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/622b8036e0a5420db5206cdcd55bbd11)](https://www.codacy.com/app/RADAR-CNS/RADAR-RestApi?utm_source=github.com&utm_medium=referral&utm_content=RADAR-CNS/RADAR-RestApi&utm_campaign=Badge_Coverage)

A REST-FULL Service using implemented using Jersey and deployed on Grizzly server.
Thi project implements the downstream REST API for the RADAR-CNS project. 

All the endpoints are `secured` and a valid user or a client registered with ManagementPortal with 
appropriate permissions/scopes can access the endpoints. For more details see [Management 
Portal] and [radar-auth]

## Getting started
To run the application do:
- Edit the `radar.yml` config file and then copy it the config folder under any of these paths.
  - `/usr/share/conf/radar/rest-api`
  - `/usr/local/conf/radar/rest-api`
- run `./gradlew build`
- Run the `radar-restapi-0.2.0.jar` located at `build/libs/`

By default, log messages are redirected to the `STDOUT`.

The api documentation is located at `<your-server-address>:<port>/api/openapi.json`
## Access Control
For accessing the end-points of this API, you will need JWT tokens from the [Management Portal]
(https://github.com/RADAR-CNS/ManagementPortal) and send it with each request in the header. In order for your token to allow access to the Rest-Api you will need to add the resource name of rest-api (ie - `res_RestApi`) in the oauth client details of the Management Portal(MP). For example, if you want a client named `dashboard` to have access to the REST API just add this line to the OAuth client credentials csv file of MP - 
```
dashboard;res_RestApi;my-secret-token-to-change-in-production;SUBJECT.READ,PROJECT.READ,SOURCE
.READ,SOURCETYPE.READ,MEASUREMENT.READ;client_credentials;;;1800;3600;{};true
```
You can change the secret, scope, name, etc according to your needs. For more info, read the configuration in the Readme of Management Portal

The Rest-api is capable of reading `bins.csv` file generated while restructuring the HDFS file system. See [hdfs_restructure.sh](https://github.com/RADAR-CNS/RADAR-Docker/tree/dev/dcompose-stack/radar-cp-hadoop-stack). This file gives a summary of records being received by the Radar Platform. An example of this file is included here in the root directory. Please place it in the `/usr/local/tomcat/bin/radar/bins.csv` for it to be readable by the RestApi. This is available at the end-point `<your-server-address>:<port>/api/status/hdfs` and can be obtained as a `CSV` or `JSON` as specified by the Accept header in your request.

## Dev Environment
Click [here](http://radar-backend.co.uk/api/openapi.json) to see documentation of dev deploy 
instance.

## Clients
Swagger provides a tool to automatically generate a client in several programming language.
- Access this [link](http://editor.swagger.io)
- Click on `File / Import URL`
- Paste your public URL to `openapi.json`
- Click on `Import`
- Click on `Generate Client` and select your programming language

## Integration test
Useful for testing the integration between `RADAR-CNS Hotstorage` , `ManagementPortal` and 
`RADAR-CNS Rest 
API`. 
Before running the test, add `127.0.0.1	hotstorage` to the `hosts` file.
To run the test:
```shell
  ./gradlew integrationTest
```

## Contributing
Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html). If you want to contribute a feature or fix, please make a pull request.
