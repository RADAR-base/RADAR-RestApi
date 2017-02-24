package org.radarcns.config;

import static java.util.Collections.singletonList;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Yaml deserializer.
 */
public class YamlConfig implements RadarConfig {

    //private final Logger LOGGER = LoggerFactory.getLogger(YamlConfig.class);

    /** Release date. **/
    private Date released;
    /** Release version. **/
    private String version;
    /** MongoDb hosts **/
    private Map<String,String> mongoHosts;
    /** MongoDb users **/
    private Map<String,String> mongoUser;

    /** @return the release date. **/
    public Date getReleased() {
        return released;
    }

    /** Sets the release date. **/
    public void setReleased(Date released) {
        this.released = released;
    }

    /** @return the version number. **/
    public String getVersion() {
        return version;
    }

    /** Sets the version number. **/
    public void setVersion(String version) {
        this.version = version;
    }

    /** @return MongoDb hosts. **/
    public Map<String, String> getMongoHosts() {
        return mongoHosts;
    }

    /** Sets MongoDb intances. **/
    public void setMongoHosts(Map<String, String> mongoHosts) {
        this.mongoHosts = mongoHosts;
    }

    /** @return MongoDb users. **/
    public Map<String, String> getMongoUser() {
        return mongoUser;
    }

    /** Sets MongoDb users. **/
    public void setMongoUser(Map<String, String> mongoUser) {
        this.mongoUser = mongoUser;
    }

    /**
     * Returns the list of all known MongoDB instances.
     * @return MongoDB instances as List
     */
    @Override
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
    @Override
    public List<MongoCredential> getMongoDbCredentials() {
        return singletonList(MongoCredential.createCredential(mongoUser.get("usr"),
                mongoUser.get("db"), mongoUser.get("pwd").toCharArray()));
    }

    /**
     * Returns a String representing the MongoDB database name.
     * @return MongoDB database name as String
     */
    @Override
    public String getMongoDbName() {
        return mongoUser.get("db");
    }
}
