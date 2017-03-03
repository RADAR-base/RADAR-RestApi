package org.radarcns.dao.mongo;

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoDAO;

/**
 * Data Access Object for user management.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SourceDAO {

    //private static final Logger logger = LoggerFactory.getLogger(SourceDAO.class);

    /**
     * Given a sourceID, it finds what is the associated source type.
     *
     * @param source is the SourceID
     * @param client MongoDB client
     * @return {@code SourceType} associated with the given source
     *
     * @throws ConnectException if MongoDb instance is not available
     */
    public static SourceType getSourceType(String source, MongoClient client)
            throws ConnectException {
        SourceType type = MongoDAO.getSourceType(source, client);

        if (type == null) {
            type = SensorDataAccessObject.getInstance().findSourceType(source, client);

            if (type == null) {
                type = AndroidDAO.getInstance().findSourceType(source, client);
            }

            if (type != null) {
                MongoDAO.witeSourceType(source, type, client);
            }
        }

        return type;
    }


    /**
     * Returns all available sources for the given patient.
     *
     * @param user user identifier.
     * @param client MongoDb client
     * @return a {@code Patient} object
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link org.radarcns.avro.restapi.user.Patient}
     */
    public static Patient findAllSoucesByUser(String user, MongoClient client)
            throws ConnectException {
        Set<Source> sources = new HashSet<>();

        sources.addAll(SensorDataAccessObject.getInstance().findAllSoucesByUser(user, client));
        sources.addAll(AndroidDAO.getInstance().findAllSoucesByUser(user, client));

        return new Patient(user, new LinkedList<>(sources));
    }

}
