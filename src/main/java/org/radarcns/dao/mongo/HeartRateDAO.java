package org.radarcns.dao.mongo;

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.dao.mongo.util.MongoSensorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for HeartRate values.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class HeartRateDAO extends MongoSensorDAO {

    private final Logger logger = LoggerFactory.getLogger(HeartRateDAO.class);

    private static final HeartRateDAO instance = new HeartRateDAO();

    public static HeartRateDAO getInstance() {
        return instance;
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        if (stat.equals(DescriptiveStatistic.median)
                || stat.equals(DescriptiveStatistic.quartiles)) {

            ArrayList<Document> quartilesList = (ArrayList<Document>) doc.get(field);

            if (stat.equals(DescriptiveStatistic.quartiles)) {
                return new HeartRate(new Quartiles(
                        quartilesList.get(0).getDouble("25"),
                        quartilesList.get(1).getDouble("50"),
                        quartilesList.get(2).getDouble("75")));
            } else if (stat.equals(DescriptiveStatistic.median)) {
                return new HeartRate(quartilesList.get(1).getDouble("50"));
            }

        } else {
            return new HeartRate(doc.getDouble(field));
        }

        logger.warn("Returning null value for the tuple: <{},{},{}>",field,stat,doc.toJson());
        return null;
    }

    @Override
    protected String getCollectionName() {
        return "android_empatica_e4_heartrate";
    }
}