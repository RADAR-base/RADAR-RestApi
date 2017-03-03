package org.radarcns.integrationTest.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.util.List;
import org.radarcns.config.Properties;

/**
 * Created by francesco on 03/03/2017.
 */
public class Utility {

    public static MongoClient getMongoClient() {
        List<MongoCredential> credentials = Properties.getInstance().getMongoDbCredential();
        return new MongoClient(Properties.getInstance().getMongoHosts(),credentials);
    }

    /**
     * @param value Long value that has to be converted.
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT representing the
     *      initial time of a Kafka time window.
     **/
    public static Long getStartTimeWindow(Long value) {
        Double timeDouble = value.doubleValue() / 10000d;
        return timeDouble.longValue() * 10000;
    }

}
