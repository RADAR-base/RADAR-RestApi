package org.radarcns.listener;

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

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.net.ConnectException;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.mongodb.ServerAddress;
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
    private static final String MONGO_CLIENT = "MONGO_CLIENT";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        MongoClient mongo = (MongoClient) context.getAttribute(MONGO_CLIENT);

        if (mongo != null) {
            mongo.close();
        }

        context.removeAttribute(MONGO_CLIENT);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MongoClient mongoClient = createClient();

        if (checkMongoConnection(mongoClient)) {
            sce.getServletContext().setAttribute(MONGO_CLIENT, mongoClient);
            LOGGER.info("MongoDB connection established");
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
        if (mongoClient == null) {
            return false;
        }

        try {
            for (MongoCredential user : Properties.getApiConfig().getMongoDbCredentials()) {
                mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
            }
            LOGGER.info("MongoDB connection is established");
            return true;
        } catch (Exception exec) {
            mongoClient.close();
            LOGGER.warn("The connection with MongoDb cannot be established", exec);
            return false;
        }
    }

    /**
     * Get a MongoClient. This will recreate it at most once if needed.
     * @param context servlet context
     * @return connected MongoClient
     * @throws ConnectException if the MongoClient could not connect to the database
     */
    public static MongoClient getClient(ServletContext context) throws ConnectException {
        MongoClient mongoClient = (MongoClient) context.getAttribute(MONGO_CLIENT);

        if (checkMongoConnection(mongoClient)) {
            return mongoClient;
        } else {
            mongoClient = createClient();
            if (checkMongoConnection(mongoClient)) {
                context.setAttribute(MONGO_CLIENT, mongoClient);
                return mongoClient;
            } else {
                throw new ConnectException("The connection with MongoDb cannot be established");
            }
        }
    }

    private static MongoClient createClient() {
        List<MongoCredential> credentials = Properties.getApiConfig().getMongoDbCredentials();
        List<ServerAddress> hosts = Properties.getApiConfig().getMongoDbHosts();

        return new MongoClient(hosts, credentials);
    }
}
