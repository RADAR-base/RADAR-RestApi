package org.radarcns.config;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.List;

/**
 * Created by Francesco Nobilia on 11/01/2017.
 */
public interface RadarConfig {
    List<ServerAddress> getMongoDBHosts();

    List<MongoCredential> getMongoDBCredential();

    String getMongoDbName();
}
