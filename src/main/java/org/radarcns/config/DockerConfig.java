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
 * Properties handler for Docker deploy.
 */
public class DockerConfig implements RadarConfig {

    /** Logger. **/
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerConfig.class);
    /** Placeholder MongoDb user. **/
    private static final String MONGODB_USER = "MONGODB_USER";
    /** Placeholder MongoDb password. **/
    private static final String MONGODB_PASS = "MONGODB_PASS";
    /** Placeholder MongoDb database. **/
    private static final String MONGODB_DATABASE = "MONGODB_DATABASE";
    /** Placeholder MongoDb host. **/
    private static final String MONGODB_HOST = "MONGODB_HOST";

    /** MongoDb database name. **/
    private final String mongoDbName;
    /** List of MongoDb host. **/
    private final List<ServerAddress> mongoDbHosts;
    /** List of MongoDb credential. **/
    private final List<MongoCredential> mongoDbCredentials;

    /**
     * Constructor.
     */
    public DockerConfig() throws InvalidParameterException {
        mongoDbName = getOrThrow(MONGODB_DATABASE);

        mongoDbCredentials = singletonList(MongoCredential.createCredential(
            getOrThrow(MONGODB_USER),
            getOrThrow(MONGODB_DATABASE),
            getOrThrow(MONGODB_PASS).toCharArray()));

        mongoDbHosts = new LinkedList<>();

        final String mongoHostParam = getOrThrow(MONGODB_HOST);
        final String[] hostsTemp = mongoHostParam.split(", ");
        for (int i = 0; i < hostsTemp.length; i++) {
            final String[] value = hostsTemp[i].split(":");
            mongoDbHosts.add(new ServerAddress(value[0], Integer.valueOf(value[1])));
        }
    }

    /**
     * Returns the required parameter or throws an execption.
     * @return environment variable named param
     * @throws InvalidParameterException if param does not exist
     */
    private String getOrThrow(final String param) {
        final String value = System.getenv(param);

        if (value == null) {
            LOGGER.error("Error loading parameter. {} is null", param);
            throw new InvalidParameterException(param + " cannot be null");
        }

        return value;
    }

    /**
     * Returns a String representing the MongoDB database name.
     * @return MongoDB database name as String
     */
    @Override
    public String getMongoDbName() {
        return this.mongoDbName;
    }

    /**
     * Returns the list of all known MongoDB instances.
     * @return MongoDB instances as List
     */
    @Override
    public List<ServerAddress> getMongoDbHosts() {
        return this.mongoDbHosts;
    }

    /**
     * Returns the list of all known MongoDB credentials.
     * @return MongoDB credentials as List
     */
    @Override
    public List<MongoCredential> getMongoDbCredentials() {
        return this.mongoDbCredentials;
    }
}
