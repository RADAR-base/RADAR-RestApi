package org.radarcns.dao.mongo.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;

import javax.servlet.ServletContext;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public abstract class MongoSensorDAO {

    private final Logger logger = LoggerFactory.getLogger(MongoSensorDAO.class);

    /**
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return the last seen sensor value stat for the given user and source, otherwise null
     */
    public Dataset valueRTByUserSource(String user, String source, MongoDAO.Stat stat, ServletContext context) {

        MongoCursor<Document> cursor = MongoDAO.findDocumentByUserSource(user, source, "end", -1, 1, getCollection(context));

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), Unit.beats_per_min,cursor);
    }

    /**
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return sensor dataset for the given user and source, otherwise null
     */
    public Dataset valueByUserSource(String user, String source, MongoDAO.Stat stat, ServletContext context) {

        MongoCursor<Document> cursor = MongoDAO.findDocumentByUserSource(user, source,"start", 1, null, getCollection(context));

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), Unit.beats_per_min, cursor);
    }

    /**
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return sensor dataset for the given user and source within the start and end time window, otherwise null
     */
    public Dataset valueByUserSourceWindow(String user, String source, MongoDAO.Stat stat, Long start, Long end, ServletContext context) {

        MongoCursor<Document> cursor = MongoDAO.findDocumentByUserSourceWindow(user, source, start, end, getCollection(context));

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), Unit.beats_per_min, cursor);
    }

    /**
     * @param field is the mongodb field that has to be extracted
     * @param stat is the statistical functional represented by the extracted field
     * @param unit is the unit of the extracted value
     * @param cursor the mongoD cursor
     * @return sensor dataset for the given input, otherwise null
     */
    private Dataset getDataSet(String field, DescriptiveStatistic stat, Unit unit, MongoCursor<Document> cursor){
        Date start = null;
        Date end = null;

        LinkedList<Item> list = new LinkedList<>();

        if(!cursor.hasNext()){
            logger.info("Empty cursor");
            cursor.close();
            return null;
        }

        while (cursor.hasNext()) {

            Document doc = cursor.next();

            if(start == null){
                start = doc.getDate("start");
            }
            end = doc.getDate("end");

            EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(doc.getDate("start")), RadarConverter.getISO8601(doc.getDate("end")));

            Item item = new Item(docToAvro(doc,field,stat),etf);

            list.addLast(item);
        }

        cursor.close();

        EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(start),RadarConverter.getISO8601(end));

        Header header = new Header(stat,unit,etf);

        Dataset hrd = new Dataset(header,list);

        logger.info("Found {} value",list.size());

        return hrd;
    }

    /**
     * @param context is the servelet context needed to retrieve the mongodb client instance
     * @return the MongoDb collection
     */
    private MongoCollection<Document> getCollection(ServletContext context){
        return MongoDAO.getCollection(context,getCollectionName());
    }

    /**
     * @param doc is the Bson Document from which we extract the required value to instantiate an Item
     * @implSpec this function must be override by the subclass
     * @return the required Object
     */
    protected Object docToAvro(Document doc,String field,DescriptiveStatistic stat){
        throw new UnsupportedOperationException("This function must be override by the ");
    }

    /**
     * @implSpec this function must be override by the subclass
     * @return the MongoDB Collection name
     */
    protected String getCollectionName(){
        throw new UnsupportedOperationException("This function must be override by the ");
    }

}