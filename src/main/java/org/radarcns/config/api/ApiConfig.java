package org.radarcns.config.api;

/*
 * Copyright 2016 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Yaml deserializer.
 */
public class ApiConfig {

    //private final Logger LOGGER = LoggerFactory.getLogger(ApiConfig.class);

    /** Release date. **/
    private Date released;
    /** Release version. **/
    private String version;

    /** MongoDb hosts. **/
    @JsonProperty("mongo_hosts")
    private Map<String,String> mongoHosts;
    /** MongoDb users. **/
    @JsonProperty("mongo_user")
    private Map<String,String> mongoUser;

    /** DeviceItem Catalog path. **/
    @JsonProperty("device_catalog")
    private String deviceCatalog;

    /** Swagger Documentation version. **/
    @JsonProperty("swagger_version")
    private String swaggerVersion;

    /** Comma separated list stating supported Application Protocols applicable to query APIs. **/
    @JsonProperty("application_protocol")
    private String applicationProtocol;

    /** Machine address hosting the APIs. **/
    private String host;

    /** Base or Root path for the RESTFul interface. **/
    @JsonProperty("api_base_path")
    private String apiBasePath;

    /** Returns the release date. **/
    public Date getReleased() {
        return released;
    }

    /** Sets the release date. **/
    public void setReleased(Date released) {
        this.released = released;
    }

    /** Returns the version number. **/
    public String getVersion() {
        return version;
    }

    /** Sets the version number. **/
    public void setVersion(String version) {
        this.version = version;
    }

    /** Returns MongoDb hosts. **/
    public Map<String, String> getMongoHosts() {
        return mongoHosts;
    }

    /** Sets MongoDb intances. **/
    public void setMongoHosts(Map<String, String> mongoHosts) {
        this.mongoHosts = mongoHosts;
    }

    /** Returns MongoDb users. **/
    public Map<String, String> getMongoUser() {
        return mongoUser;
    }

    /** Sets MongoDb users. **/
    public void setMongoUser(Map<String, String> mongoUser) {
        this.mongoUser = mongoUser;
    }

    /** Returns DeviceItem Catalog path. **/
    public String getDeviceCatalog() {
        return deviceCatalog;
    }

    /** Sets DeviceItem Catalog path. **/
    public void setDeviceCatalog(String deviceCatalog) {
        this.deviceCatalog = deviceCatalog;
    }

    /** Returns Swagger Documentation version. **/
    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    /** Sets Swagger Documentation version. **/
    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }

    /** Returns an array listing all supported Application Protocols applicable to query APIs. **/
    public String[] getApplicationProtocols() {
        return applicationProtocol.replaceAll("\\s","").split(",");
    }

    /** Returns a comma separated list stating supported Application Protocols applicable. **/
    public String getApplicationProtocol() {
        return applicationProtocol;
    }

    /** Sets the list of supported Application Protocols applicable to query APIs. **/
    public void setApplicationProtocol(String applicationProtocol) {
        this.applicationProtocol = applicationProtocol;
    }

    /** Returns the Host address hosting the API. **/
    public String getHost() {
        return host;
    }

    /** Sets the Host address hosting the API. **/
    public void setHost(String host) {
        this.host = host;
    }

    /** Returns the API base bath making sure it starts with slash. **/
    public String getApiBasePath() {
        if (apiBasePath.charAt(0) != '/') {
            return  "/" + apiBasePath;
        }

        return apiBasePath;
    }

    /** Returns the URL to access the RESTFul interface. **/
    public String getApiUrl() {
        String apiComponent = getApiBasePath();
        if (apiComponent.charAt(apiComponent.length() - 1) != '/') {
            apiComponent += "/";
        }

        return applicationProtocol + "://" + host + apiComponent;
    }

    /** Sets the API base bath. **/
    public void setApiBasePath(String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    /**
     * Returns the list of all known MongoDB instances.
     * @return MongoDB instances as List
     */
    public List<ServerAddress> getMongoDbHosts() {

        final List<ServerAddress> mongoHostsTemp = new LinkedList<>();
        for (final String key : mongoHosts.keySet()) {
            mongoHostsTemp.add(new ServerAddress(key,Integer.valueOf(mongoHosts.get(key))));
        }

        return mongoHostsTemp;
    }

    /**
     * Returns the list of all known MongoDB credentials.
     * @return MongoDB credentials as List
     */
    public List<MongoCredential> getMongoDbCredentials() {
        return singletonList(MongoCredential.createCredential(mongoUser.get("usr"),
                mongoUser.get("db"), mongoUser.get("pwd").toCharArray()));
    }

    /**
     * Returns a String representing the MongoDB database name.
     * @return MongoDB database name as String
     */
    public String getMongoDbName() {
        return mongoUser.get("db");
    }
}
