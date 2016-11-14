package org.radarcns.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import javax.servlet.ServletContext;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class MongoDAO {

    private static final Logger logger = LoggerFactory.getLogger(MongoDAO.class);

    //Enumerate all possible topics
    public enum Stat {
        avg("avg"), count("count"), iqr("iqr"), max("max"), median("quartile"), min("min"), quartile("quartile"), sum("sum");

        private final String param;

        Stat(String param) {
            this.param = param;
        }

        public String getParam() {
            return param;
        }
    }

    protected static MongoCursor<Document> findDocumentByUserAndWindow(String user, Long start, Long end, MongoCollection<Document> collection){
        FindIterable<Document> result = collection.find(
                Filters.and(
                        eq("user",user),
                        gte("start",new Date(start)),
                        lte("end",new Date(end)))).sort(new BasicDBObject("start",1));;

        return result.iterator();
    }

    protected static MongoCursor<Document> findDocumentByUser(String user, MongoCollection<Document> collection){
        FindIterable<Document> result = collection.find(eq("user",user));

        return result.iterator();
    }

    protected static MongoCursor<Document> findDocumentByUser(String user, String sortBy, int order, Integer limit, MongoCollection<Document> collection){
        FindIterable<Document> result;

        if(sortBy == null)
            result = collection.find(eq("user",user));
        else
            result = collection.find(eq("user",user)).sort(new BasicDBObject(sortBy,order));

        if(limit != null)
            result = result.limit(limit);

        return result.iterator();
    }

    public static MongoCollection<Document> getCollection(ServletContext context, String collection){
        MongoClient mongoClient = (MongoClient) context.getAttribute("MONGO_CLIENT");
        MongoDatabase database = mongoClient.getDatabase("hotstorage");

        return database.getCollection(collection);
    }
}