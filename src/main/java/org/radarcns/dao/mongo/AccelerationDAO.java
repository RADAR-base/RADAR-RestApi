package org.radarcns.dao.mongo;

import java.util.ArrayList;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.Acceleration;
import org.radarcns.dao.mongo.util.MongoSensorDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class AccelerationDAO extends MongoSensorDAO {

    private final Logger logger = LoggerFactory.getLogger(AccelerationDAO.class);

    private final static AccelerationDAO instance = new AccelerationDAO();

    public static AccelerationDAO getInstance(){
        return instance;
    }

    @Override
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        if(stat.equals(DescriptiveStatistic.median) || stat.equals(DescriptiveStatistic.quartiles)){
            Document component = (Document) doc.get(field);

            ArrayList<Document> x = (ArrayList<Document>) component.get("x");
            ArrayList<Document> y = (ArrayList<Document>) component.get("y");
            ArrayList<Document> z = (ArrayList<Document>) component.get("z");

            if(stat.equals(DescriptiveStatistic.quartiles)) {
                return new Acceleration(
                    new Quartiles(
                        x.get(0).getDouble("25"),
                        x.get(1).getDouble("50"),
                        x.get(2).getDouble("75")),
                    new Quartiles(
                        y.get(0).getDouble("25"),
                        y.get(1).getDouble("50"),
                        y.get(2).getDouble("75")),
                    new Quartiles(
                        z.get(0).getDouble("25"),
                        z.get(1).getDouble("50"),
                        z.get(2).getDouble("75")));
            }
            else if(stat.equals(DescriptiveStatistic.median)){
                return new Acceleration(
                        x.get(1).getDouble("50"),
                        y.get(1).getDouble("50"),
                        z.get(1).getDouble("50"));
            }

        }
        else{
            logger.debug(doc.toJson());
            Document data = (Document) doc.get(field);
            logger.debug(data.toJson());
            return new Acceleration(
                    data.getDouble("x"),
                    data.getDouble("y"),
                    data.getDouble("z"));
        }

        logger.warn("Returning null value for the tuple: <{},{},{}>",field,stat,doc.toJson());
        return null;
    }

    @Override
    protected double extractCount(Document doc){
        return (doc.getDouble("x") + doc.getDouble("y") + doc.getDouble("z"))/3.0d;
    }

    @Override
    protected String getCollectionName() {
        return "android_empatica_e4_acceleration_output";
    }
}