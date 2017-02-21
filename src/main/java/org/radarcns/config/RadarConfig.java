package org.radarcns.config;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.List;

/**
 * Configuration abstraction.
 */
public interface RadarConfig {
    List<ServerAddress> getMongoDbHosts();

    List<MongoCredential> getMongoDbCredential();

    String getMongoDbName();
}
