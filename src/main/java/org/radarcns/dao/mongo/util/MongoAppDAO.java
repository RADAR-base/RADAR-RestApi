package org.radarcns.dao.mongo.util;

import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public abstract class MongoAppDAO extends MongoDAO {

    private final Logger logger = LoggerFactory.getLogger(MongoAppDAO.class);

    /**
     * @param user is the userID
     * @param source is the sourceID
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return the last seen status update for the given user and source, otherwise null
     */
    public Application valueByUserSource(String user, String source, ServletContext context) throws ConnectException{

        MongoCursor<Document> cursor = MongoHelper
            .findDocumentByUserSource(user, source, null, -1, 1, getCollection(context));

        if(!cursor.hasNext()){
            logger.debug("Empty cursor");
            cursor.close();
            return null;
        }

        Document doc = cursor.next();
        cursor.close();

        return getApplication(doc);
    }

    protected abstract Application getApplication(Document doc);

}