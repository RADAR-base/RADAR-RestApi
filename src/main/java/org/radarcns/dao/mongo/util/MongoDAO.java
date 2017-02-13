package org.radarcns.dao.mongo.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 09/02/2017.
 */
public abstract class MongoDAO {

    private final Logger logger = LoggerFactory.getLogger(MongoDAO.class);

    /**
     * @return all distinct userIDs for the given collection, otherwise empty Collection
     */
    public Collection<String> findAllUser(ServletContext context) throws ConnectException {
        MongoCursor<String> cursor = MongoHelper.findAllUser(getCollection(context));

        ArrayList<String> list = new ArrayList<>();

        if(!cursor.hasNext()){
            logger.debug("Empty cursor");
        }

        while (cursor.hasNext()) {
            list.add(cursor.next());
        }

        cursor.close();
        return list;
    }

    /**
     * @param user is the userID
     * @return all distinct sourceIDs for the given collection, otherwise empty Collection
     */
    public Collection<String> findAllSoucesByUser(String user, ServletContext context) throws ConnectException {
        MongoCursor<String> cursor = MongoHelper.findAllSourceByUser(user, getCollection(context));

        ArrayList<String> list = new ArrayList<>();

        if(!cursor.hasNext()){
            logger.debug("Empty cursor");
        }

        while (cursor.hasNext()) {
            list.add(cursor.next());
        }

        cursor.close();
        return list;
    }

    /**
     * @param context is the servelet context needed to retrieve the mongodb client instance
     * @return the MongoDb collection
     */
    protected MongoCollection<Document> getCollection(ServletContext context) throws ConnectException {
        return MongoHelper.getCollection(context,getCollectionName());
    }

    /**
     * @implSpec this function must be override by the subclass
     * @return the MongoDB Collection name
     */
    protected abstract String getCollectionName();
}
