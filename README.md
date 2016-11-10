# RADAR-CNS REST-API

A REST-FULL Service using Tomcat 8.0.37, MongoDb 3.2.10 and Jersey 2.

Thi project implents the downstream REST API for the RADAR-CNS project'

## Setup

Before deploying the war file copy the radar.yml file into `/usr/local/tomcat/conf/`. If your installation uses a different path you have to modify the variable `pathFile` in `org.radarcns.config.Properties` After that
- Run `./gradlew build`
- Copy the radar.war located at `build/libs/` in `/usr/local/tomcat/webapp/`
- Stop tomcat
- Start tomcat

The application log file is located at `/usr/local/tomcat/log/radar-restapi.log`

The api documentation is located at `<your-server-address>:<port>/radar/api/swagger.json`

## Contributing

Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html). If you want to contribute a feature or fix, please make a pull request