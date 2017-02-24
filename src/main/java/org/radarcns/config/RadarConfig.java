package org.radarcns.config;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.List;

/**
 * Configuration abstraction.
 */
public interface RadarConfig {
    /**
     * Returns the list of all known MongoDB instances.
     * @return MongoDB instances as List
     */
    List<ServerAddress> getMongoDbHosts();

    /**
     * Returns the list of all known MongoDB credentials.
     * @return MongoDB credentials as List
     */
    List<MongoCredential> getMongoDbCredentials();

    /**
     * Returns a String representing the MongoDB database name.
     * @return MongoDB database name as String
     */
    String getMongoDbName();
}
