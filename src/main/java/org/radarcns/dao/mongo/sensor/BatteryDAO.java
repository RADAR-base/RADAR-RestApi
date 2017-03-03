package org.radarcns.dao.mongo.sensor;

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.Battery;
import org.radarcns.dao.mongo.util.MongoSensorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for Battery level values.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class BatteryDAO extends MongoSensorDAO {

    private final Logger logger = LoggerFactory.getLogger(BatteryDAO.class);

    private static final BatteryDAO instance = new BatteryDAO();

    public static BatteryDAO getInstance() {
        return instance;
    }

    private BatteryDAO() {
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        if (stat.equals(DescriptiveStatistic.MEDIAN)
                || stat.equals(DescriptiveStatistic.QUARTILES)) {

            ArrayList<Document> quartilesList = (ArrayList<Document>) doc.get(field);

            if (stat.equals(DescriptiveStatistic.QUARTILES)) {
                return new Battery( new Quartiles(
                        quartilesList.get(0).getDouble("25"),
                        quartilesList.get(1).getDouble("50"),
                        quartilesList.get(2).getDouble("75")));
            } else if (stat.equals(DescriptiveStatistic.MEDIAN)) {
                return new Battery(quartilesList.get(1).getDouble("50"));
            }

        } else {
            return new Battery(doc.getDouble(field));
        }

        logger.warn("Returning null value for the tuple: <{},{},{}>",field,stat,doc.toJson());
        return null;
    }

    @Override
    public String getEmpaticaCollection() {
        return "android_empatica_e4_battery_level_output";
    }
}