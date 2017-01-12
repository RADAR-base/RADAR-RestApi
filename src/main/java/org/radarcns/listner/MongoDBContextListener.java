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
public class MongoDBContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBContextListener.class);

    public static final String MONGO_CLIENT = "MONGO_CLIENT";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MongoClient mongo = (MongoClient) sce.getServletContext().getAttribute(MONGO_CLIENT);

        if(mongo != null) {
            mongo.close();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MongoClient mongoClient = null;

        try {
            List<MongoCredential> credentials = Properties.getInstance().getMongoDBCredential();

            mongoClient = new MongoClient(Properties.getInstance().getMongoHosts(),credentials);

            if (checkMongoConnection(mongoClient,credentials)) {
                sce.getServletContext().setAttribute(MONGO_CLIENT, mongoClient);

                logger.info("MongoDB connection established");
            }
        }
        catch (com.mongodb.MongoSocketOpenException e){
            if(mongoClient != null) {
                mongoClient.close();
            }

            logger.error(e.getMessage());
        }
    }

    private boolean checkMongoConnection(MongoClient mongoClient, List<MongoCredential> credentials){
        Boolean flag = true;
        try {
            for(MongoCredential user : credentials) {
                mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
            }

        } catch (Exception e) {
            flag = false;

            if(mongoClient != null) {
                mongoClient.close();
            }

            logger.error("Error during connection test",e);
        }

        logger.info("MongoDB connection is {}",flag.toString());

        return flag;
    }

    public static void recoverOrThrow(ServletContext context) throws ConnectException{
        logger.warn("Try to reconnect to MongoDB");

        MongoClient mongoClient = null;
        Boolean flag = true;

        try {
            List<MongoCredential> credentials = Properties.getInstance().getMongoDBCredential();

            mongoClient = new MongoClient(Properties.getInstance().getMongoHosts(),credentials);

            try {
                for(MongoCredential user : credentials) {
                    mongoClient.getDatabase(user.getSource()).runCommand(new Document("ping", 1));
                }

            } catch (Exception e) {
                flag = false;

                if(mongoClient != null) {
                    mongoClient.close();
                }

                logger.error("The connection with MongoDb cannot be established", e);
            }

            logger.info("MongoDB connection is {}",flag.toString());

            if (flag.booleanValue()) {
                context.setAttribute(MONGO_CLIENT, mongoClient);

                logger.info("MongoDB connection established");
            }
            else{
                throw new ConnectException("The connection with MongoDb cannot be established");
            }
        }
        catch (com.mongodb.MongoSocketOpenException e){
            if(mongoClient != null) {
                mongoClient.close();
            }

            logger.error(e.getMessage());

            throw new ConnectException("The connection with MongoDb cannot be established");
        }


    }

}
