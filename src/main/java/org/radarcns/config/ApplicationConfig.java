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

package org.radarcns.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Yaml deserializer.
 */
public class ApplicationConfig {

    //private final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    /** MongoDb hosts. **/
    @JsonProperty("mongodb_hosts")
    private Map<String,String> mongoHosts;
    /** MongoDb users. **/
    @JsonProperty("mongodb_credentials")
    private Map<String,String> mongoUser;

    /** DeviceItem Catalog path. **/
    @JsonProperty("device_catalog")
    private String deviceCatalog;


    @JsonProperty("management_portal_config")
    private ManagementPortalConfig managementPortalConfig;

    /** Returns MongoDb hosts. **/
    public Map<String, String> getMongoHosts() {
        return mongoHosts;
    }

    /** Sets MongoDb instance. **/
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
     * @return a {@code List} of {@link MongoCredential}
     */
    public MongoCredential getMongoDbCredentials() {
        return MongoCredential.createCredential(mongoUser.get("username"),
                mongoUser.get("database_name"), mongoUser.get("password").toCharArray());
    }

    /**
     * Returns a {@code String} representing the MongoDB database name.
     * @return MongoDB database name as {@code String}
     */
    public String getMongoDbName() {
        return mongoUser.get("database_name");
    }

    public ManagementPortalConfig getManagementPortalConfig() {
        return managementPortalConfig;
    }

}
