package org.radarcns.dao.mongo;

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.SensorDataAccessObject;

/**
 * Data Access Object for user management.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class UserDAO {

    //private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    /**
     * Finds all users checking all available collections.
     *
     * @param client MongoDB client
     * @return a study {@code Cohort}
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link org.radarcns.avro.restapi.user.Cohort}
     */
    public static Cohort findAllUsers(MongoClient client) throws ConnectException {

        List<Patient> patients = new LinkedList<>();

        Set<String> users = new HashSet<>(
                SensorDataAccessObject.getInstance().findAllUsers(client));

        users.addAll(AndroidDAO.getInstance().findAllUser(client));

        for (String user : users) {
            patients.add(SourceDAO.findAllSoucesByUser(user, client));
        }

        return new Cohort(0, patients);
    }

}
