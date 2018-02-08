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

package org.radarcns.listener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.List;
import javax.ws.rs.core.Context;
import org.bson.Document;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.server.CloseableService;
import org.radarcns.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to creates a singleton MongoClient with the correct credentials.
 */
public class MongoFactory implements Factory<MongoClient> {

    private static final Logger logger = LoggerFactory.getLogger(MongoFactory.class);

    /**
     * Disposes the client after use.
     */
    @Context
    @SuppressWarnings("PMD.UnusedPrivateField")
    private CloseableService closeableService;

    /**
     * Checks if with the given client and credential is it possible to establish a connection
     * towards the MongoDB host.
     *
     * @param mongoClient client for MongoDB
     * @return {@code true} if the connection can be established false otherwise
     */
    public static boolean checkMongoConnection(MongoClient mongoClient) {
        Boolean flag = true;
        try {
            for (MongoCredential user : mongoClient.getCredentialsList()) {
                mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
            }

        } catch (Exception exec) {
            flag = false;
            logger.error("Error during connection test", exec);
        }

        logger.info("MongoDB connection is {}", flag.toString());

        return flag;
    }

    @Override
    public MongoClient provide() {
        MongoCredential credentials = Properties.getApiConfig().getMongoDbCredentials();
        List<ServerAddress> hosts = Properties.getApiConfig().getMongoDbHosts();

        return new MongoClient(hosts, credentials, MongoClientOptions.builder().build());
    }

    @Override
    public void dispose(MongoClient client) {
        if (client != null) {
            client.close();
        }
    }
}
