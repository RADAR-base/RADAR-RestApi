package org.radarcns.listner;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import static java.util.Collections.singletonList;

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
        try {
            mongoClient = new MongoClient(getMongoDbServer(sce), getMongoDbCredential(sce));

            if (checkMongoConnection(mongoClient,sce.getServletContext().getInitParameter("MONGODB_DB"))) {
                sce.getServletContext().setAttribute("MONGO_CLIENT", mongoClient);
                logger.info("MongoDB connection established");
            }
        }
        catch (com.mongodb.MongoSocketOpenException e){
            if(mongoClient != null) {
                mongoClient.close();
            }
        }

    }

    private boolean checkMongoConnection(MongoClient mongoClient, String dbName){
        Boolean flag = false;
        try {
            mongoClient.getDatabase(dbName).runCommand(new Document("ping", 1));
            flag = true;
        } catch (Exception e) {
            mongoClient.close();
            logger.info("Error during connection test",e);
        }

        logger.info("MongoDB connection id {}",flag.toString());

        return flag;
    }

    private List<ServerAddress> getMongoDbServer(ServletContextEvent sce){
        //MONGODB_HOST is a comma separated list of all mongo db instances
        String serverWebXml = sce.getServletContext().getInitParameter("MONGODB_HOST");
        List<String> servers = Arrays.asList(serverWebXml.split(","));

        /*String portWebXml = sce.getServletContext().getInitParameter("MONGODB_PORT");
        List<String> ports = Arrays.asList(portWebXml.split(","));*/

        /*if(servers.size() != ports.size()){
            throw new InvalidParameterException("Server list and port list have different cardinality");
        }*/

        final List<ServerAddress> result = new LinkedList<>();
        for (int i=0; i<servers.size(); i++){
            result.add(new ServerAddress(servers.get(i)));
        }

        return result;
    }

    private List<MongoCredential> getMongoDbCredential(ServletContextEvent sce){
        return singletonList(MongoCredential.createCredential(
                    sce.getServletContext().getInitParameter("MONGODB_USR"),
                    sce.getServletContext().getInitParameter("MONGODB_DB"),
                    sce.getServletContext().getInitParameter("MONGODB_PWD").toCharArray()));
    }

}
