package org.radarcns.listner;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

import org.bson.Document;
import org.radarcns.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by Francesco Nobilia on 19/10/2016.
 */
@WebListener
public class MongoDBContextListener implements ServletContextListener {

    Logger logger = LoggerFactory.getLogger(MongoDBContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MongoClient mongo = (MongoClient) sce.getServletContext().getAttribute("MONGO_CLIENT");

        if(mongo != null) {
            mongo.close();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MongoClient mongoClient = null;

        Properties prop = new Properties();

        try {

            List<MongoCredential> credentials = prop.getMongoUsers();

            mongoClient = new MongoClient(prop.getMongoHosts(),credentials);

            if (checkMongoConnection(mongoClient,credentials)) {
                sce.getServletContext().setAttribute("MONGO_CLIENT", mongoClient);

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

}
