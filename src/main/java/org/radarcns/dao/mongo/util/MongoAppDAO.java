package org.radarcns.dao.mongo.util;

import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic DAO to return Android App status information.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class MongoAppDAO extends MongoDAO {

    private final Logger logger = LoggerFactory.getLogger(MongoAppDAO.class);

    /**
     * Returns an {@code Application} initialised with the extracted value.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return the last seen status update for the given user and source, otherwise null
     */
    //TODO add an Application as input that can be used as a "reuse" (see DatumReader#read).
    public Application valueByUserSource(String user, String source, ServletContext context)
            throws ConnectException {

        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSource(user, source, null, -1, 1,
                    getCollection(context));

        if (!cursor.hasNext()) {
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