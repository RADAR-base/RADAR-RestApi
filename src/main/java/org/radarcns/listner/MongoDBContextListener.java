package org.radarcns.listner;

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
 * Created by Francesco Nobilia on 19/10/2016.
 */
@WebListener
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class MongoDBContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBContextListener.class);

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
            List<MongoCredential> credentials = Properties.getInstance().getMongoDbCredential();

            mongoClient = new MongoClient(Properties.getInstance().getMongoHosts(),credentials);

            if (checkMongoConnection(mongoClient,credentials)) {
                sce.getServletContext().setAttribute(MONGO_CLIENT, mongoClient);

                logger.info("MongoDB connection established");
            }
        } catch (com.mongodb.MongoSocketOpenException exec) {
            if (mongoClient != null) {
                mongoClient.close();
            }

            logger.error(exec.getMessage());
        }
    }

    /**
     * Checks if with the given client and credential is it possible to establish a connection
     *      towards the MongoDB host.
     *
     * @param mongoClient client for MongoDB
     * @param credentials username, password and host
     * @return {@code true} if the connection can be established false otherwise
     */
    private boolean checkMongoConnection(MongoClient mongoClient,
            List<MongoCredential> credentials) {
        Boolean flag = true;
        try {
            for (MongoCredential user : credentials) {
                mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
            }

        } catch (Exception exec) {
            flag = false;

            if (mongoClient != null) {
                mongoClient.close();
            }

            logger.error("Error during connection test",exec);
        }

        logger.info("MongoDB connection is {}",flag.toString());

        return flag;
    }

    /**
     * Tries to recover the connection with MongoDB.
     *
     * @param context useful to retrieve and store the MongoDb client
     * @throws ConnectException if the connection cannot be established
     */
    public static void recoverOrThrow(ServletContext context) throws ConnectException {
        logger.warn("Try to reconnect to MongoDB");

        MongoClient mongoClient = null;
        Boolean flag = true;

        try {
            List<MongoCredential> credentials = Properties.getInstance().getMongoDbCredential();

            mongoClient = new MongoClient(Properties.getInstance().getMongoHosts(),credentials);

            try {
                for (MongoCredential user : credentials) {
                    mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
                }
            } catch (Exception exec) {
                flag = false;

                if (mongoClient != null) {
                    mongoClient.close();
                }

                logger.error("The connection with MongoDb cannot be established", exec);
            }

            if (flag.booleanValue()) {
                context.setAttribute(MONGO_CLIENT, mongoClient);

                logger.info("MongoDB connection established");
            } else {
                throw new ConnectException("The connection with MongoDb cannot be established");
            }
        } catch (com.mongodb.MongoSocketOpenException exec) {
            if (mongoClient != null) {
                mongoClient.close();
            }

            logger.error(exec.getMessage());

            throw new ConnectException("The connection with MongoDb cannot be established");
        }

        logger.info("MongoDB connection is {}", flag.toString());

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
            for (MongoCredential user : Properties.getInstance().getMongoDbCredential()) {
                mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
            }

            flag = true;

        } catch (Exception exec) {
            if (mongoClient != null) {
                mongoClient.close();
            }

            context.setAttribute(MONGO_CLIENT, null);
            logger.error("The connection with MongoDb cannot be established", exec);
        }

        logger.debug("MongoDB connection is {}", flag);

        return false;
    }

}
