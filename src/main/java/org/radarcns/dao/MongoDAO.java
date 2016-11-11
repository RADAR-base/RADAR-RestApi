package org.radarcns.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.function.BooleanSupplier;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class MongoDAO {

    public static Logger logger = LoggerFactory.getLogger(MongoDAO.class);

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

    protected static MongoCursor<Document> findDocumentByUser(String user, String sortBy, int order, MongoCollection<Document> collection){
        FindIterable<Document> result;

        if(sortBy == null)
            result = collection.find(eq("user",user));
        else
            result = collection.find(eq("user",user)).sort(new BasicDBObject(sortBy,order));

        return result.iterator();
    }
}