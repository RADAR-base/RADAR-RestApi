package org.radarcns.config;

import static java.util.Collections.singletonList;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/11/2016.
 */
public class DockerConfig implements RadarConfig{

    private final Logger logger = LoggerFactory.getLogger(DockerConfig.class);

    private final String MONGODB_USER = "MONGODB_USER";
    private final String MONGODB_PASS = "MONGODB_PASS";
    private final String MONGODB_DATABASE = "MONGODB_DATABASE";
    private final String MONGODB_HOST = "MONGODB_HOST";

    private final String dbName;
    private final List<ServerAddress> mongoHost;
    private final List<MongoCredential> mongoCredentials;

    public DockerConfig(){
        dbName = getOrThrow(MONGODB_DATABASE);

        mongoCredentials = singletonList(MongoCredential.createCredential(
            getOrThrow(MONGODB_USER),
            getOrThrow(MONGODB_DATABASE),
            getOrThrow(MONGODB_PASS).toCharArray()));

        mongoHost = new LinkedList<>();

        String mongoHostParam = getOrThrow(MONGODB_HOST);
        String[] hostsTemp = mongoHostParam.split(", ");
        for(int i=0; i<hostsTemp.length; i++){
            String[] value = hostsTemp[i].split(":");
            mongoHost.add(new ServerAddress(value[0], Integer.valueOf(value[1])));
        }
    }

    private String getOrThrow(String param){
        try {
            final String value = System.getenv(param);
            if (value == null) {
                logger.error("{} is null", param);
                throw new InvalidParameterException(param + "cannot be null");
            }
            return value;
        }
        catch (Exception e){
            logger.error("Error loading {}", param, e);
        }

        return null;
    }

    public String getMongoDbName() {
        return dbName;
    }

    public List<ServerAddress> getMongoDBHosts() {
        return mongoHost;
    }

    public List<MongoCredential> getMongoDBCredential() {
        return mongoCredentials;
    }
}