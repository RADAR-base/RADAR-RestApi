package org.radarcns.dao.mongo;

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.InterBeatInterval;
import org.radarcns.dao.mongo.util.MongoSensorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class InterBeatIntervalDAO extends MongoSensorDAO {

    private final Logger logger = LoggerFactory.getLogger(InterBeatIntervalDAO.class);

    private final static InterBeatIntervalDAO instance = new InterBeatIntervalDAO();

    public static InterBeatIntervalDAO getInstance(){
        return instance;
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        if(stat.equals(DescriptiveStatistic.median) || stat.equals(DescriptiveStatistic.quartiles)){

            ArrayList<Document> quartiles_list = (ArrayList<Document>) doc.get(field);

            if(stat.equals(DescriptiveStatistic.quartiles)) {
                return new InterBeatInterval(new Quartiles(
                        quartiles_list.get(0).getDouble("25"),
                        quartiles_list.get(1).getDouble("50"),
                        quartiles_list.get(2).getDouble("75")));
            }
            else if(stat.equals(DescriptiveStatistic.median)){
                return new InterBeatInterval(quartiles_list.get(1).getDouble("50"));
            }

        }
        else{
            return new InterBeatInterval(doc.getDouble(field));
        }

        logger.warn("Returning null value for the tuple: <{},{},{}>",field,stat,doc.toJson());
        return null;
    }

    @Override
    protected String getCollectionName() {
        return "android_empatica_e4_inter_beat_interval_output";
    }
}