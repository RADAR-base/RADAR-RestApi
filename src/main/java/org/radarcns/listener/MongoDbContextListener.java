package org.radarcns.listener;

/*
 *  Copyright 2016 King's College London and The Hyve
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

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.net.ConnectException;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.bson.Document;
import org.radarcns.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * pon the web application initialisation, this Context Listener creates a MongoDb client that can
 *      be reused by each call. A Mongo Client should be seen like a Thread Pool.
 */
@WebListener
public class MongoDbContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbContextListener.class);

    public static final String MONGO_CLIENT = "MONGO_CLIENT";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MongoClient mongo = (MongoClient) sce.getServletContext().getAttribute(MONGO_CLIENT);

        if (mongo != null) {
            mongo.close();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MongoClient mongoClient = null;

        try {
            List<MongoCredential> credentials = Properties.getApiConfig().getMongoDbCredentials();

            mongoClient = new MongoClient(Properties.getApiConfig().getMongoDbHosts(),credentials);

            if (checkMongoConnection(mongoClient)) {
                sce.getServletContext().setAttribute(MONGO_CLIENT, mongoClient);

                LOGGER.info("MongoDB connection established");
            }
        } catch (com.mongodb.MongoSocketOpenException exec) {
            if (mongoClient != null) {
                mongoClient.close();
            }

            LOGGER.error(exec.getMessage());
        } catch (java.lang.ExceptionInInitializerError exec) {
            LOGGER.error("Mongo Client cannot be initialised since Property cannot be initialised.",
                    exec);
            throw new ExceptionInInitializerError(exec);
        }
    }

    /**
     * Checks if with the given client and credential is it possible to establish a connection
     *      towards the MongoDB host.
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

            if (mongoClient != null) {
                mongoClient.close();
            }

            LOGGER.error("Error during connection test",exec);
        }

        LOGGER.info("MongoDB connection is {}",flag.toString());

        return flag;
    }

    /**
     * Tries to recover the connection with MongoDB.
     *
     * @param context useful to retrieve and store the MongoDb client
     * @throws ConnectException if the connection cannot be established
     */
    public static void recoverOrThrow(ServletContext context) throws ConnectException {
        LOGGER.warn("Try to reconnect to MongoDB");

        MongoClient mongoClient = null;
        Boolean flag = true;

        try {
            List<MongoCredential> credentials = Properties.getApiConfig().getMongoDbCredentials();

            mongoClient = new MongoClient(Properties.getApiConfig().getMongoDbHosts(),credentials);

            try {
                for (MongoCredential user : credentials) {
                    mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
                }
            } catch (Exception exec) {
                flag = false;

                if (mongoClient != null) {
                    mongoClient.close();
                }

                LOGGER.error("The connection with MongoDb cannot be established", exec);
            }

            if (flag.booleanValue()) {
                context.setAttribute(MONGO_CLIENT, mongoClient);

                LOGGER.info("MongoDB connection established");
            } else {
                throw new ConnectException("The connection with MongoDb cannot be established");
            }
        } catch (com.mongodb.MongoSocketOpenException exec) {
            if (mongoClient != null) {
                mongoClient.close();
            }

            LOGGER.error(exec.getMessage());

            throw new ConnectException("The connection with MongoDb cannot be established");
        }

        LOGGER.info("MongoDB connection is {}", flag.toString());

    }

    /**
     * Verifies if the required database can be pinged.
     *
     * @param context useful to retrieve and the MongoDb client
     * @return {@cod true} if the required database can be ping, {@code false} otherwise
     * @throws ConnectException if the MongoDB client is faulty
     */
    public static boolean testConnection(ServletContext context) throws ConnectException {
        boolean flag = false;

        MongoClient mongoClient = (MongoClient) context.getAttribute(MONGO_CLIENT);

        if (mongoClient == null) {
            return false;
        }

        try {
            for (MongoCredential user : Properties.getApiConfig().getMongoDbCredentials()) {
                mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
            }

            flag = true;

        } catch (Exception exec) {
            if (mongoClient != null) {
                mongoClient.close();
            }

            context.setAttribute(MONGO_CLIENT, null);
            LOGGER.error("The connection with MongoDb cannot be established", exec);
        }

        LOGGER.debug("MongoDB connection is {}", flag);

        return false;
    }

}
