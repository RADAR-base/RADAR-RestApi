package org.radarcns.listner;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
        mongo.close();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(getMongoDbServer(sce), getMongoDbCredential(sce));

            if (checkMongoConnection(mongoClient)) {
                sce.getServletContext().setAttribute("MONGO_CLIENT", mongoClient);
            }
        }
        catch (com.mongodb.MongoSocketOpenException e){
            if(mongoClient != null) {
                mongoClient.close();
            }
        }

    }

    private boolean checkMongoConnection(MongoClient mongoClient){
        try {
            mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            mongoClient.close();
            logger.info("Error during connection test",e);
        }

        return false;
    }

    private List<ServerAddress> getMongoDbServer(ServletContextEvent sce){
        //MONGODB_HOST is a comma separated list of all mongo db instances
        String paramWebXml = sce.getServletContext().getInitParameter("MONGODB_HOST");
        List<String> values = Arrays.asList(paramWebXml.split(","));

        final List<ServerAddress> result = new LinkedList<>();
        for (String value: values){
            result.add(new ServerAddress(value));
        }

        return result;
    }

    private List<MongoCredential> getMongoDbCredential(ServletContextEvent sce){
        List<MongoCredential> credentials = new ArrayList<>();

        credentials.add(
                MongoCredential.createMongoCRCredential(
                        sce.getServletContext().getInitParameter("MONGODB_USR"),
                        sce.getServletContext().getInitParameter("MONGODB_DB"),
                        sce.getServletContext().getInitParameter("MONGODB_PWD").toCharArray()
                )
        );

        return credentials;
    }

}
