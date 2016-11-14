package org.radarcns.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.radarcns.avro.DescriptiveStatistic;
import org.radarcns.avro.EffectiveTimeFrame;
import org.radarcns.avro.HeartRate;
import org.radarcns.avro.HeartRateDataSet;
import org.radarcns.avro.HeartRateItem;
import org.radarcns.avro.HeartRateValue;
import org.radarcns.avro.Unit;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;

import javax.servlet.ServletContext;

import static org.radarcns.dao.MongoDAO.findDocumentByUser;
import static org.radarcns.dao.MongoDAO.findDocumentByUserAndWindow;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class MongoHeartRateDAO {

    private static final Logger logger = LoggerFactory.getLogger(MongoHeartRateDAO.class);

    /**
     * @param user is the userID
     * @param stat is the required statistical value
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return the last seen HR stat for the given UserID, otherwise null
     */
    public static HeartRate valueRTByUser(String user, MongoDAO.Stat stat, ServletContext context) {

        MongoCursor<Document> cursor = findDocumentByUser(user,"end",-1,1,getCollection(context));

        return getValue(stat.getParam(),RadarConverter.getDescriptiveStatistic(stat),Unit.beats_per_min,cursor);
    }

    /**
     * @param user is the userID
     * @param stat is the required statistical value
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return all HR dataset for the given userID, otherwise null
     */
    public static HeartRateDataSet valueByUser(String user, MongoDAO.Stat stat, ServletContext context) {

        MongoCursor<Document> cursor = findDocumentByUser(user,"start",1,null,getCollection(context));

        return getDataSet(stat.getParam(),RadarConverter.getDescriptiveStatistic(stat),Unit.beats_per_min,cursor);
    }

    /**
     * @param user is the userID
     * @param stat is the required statistical value
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param context is the servlet context needed to retrieve the mongoDb client istance
     * @return all HR dataset for the given userID, otherwise null
     */
    public static HeartRateDataSet valueByUserWindow(String user, MongoDAO.Stat stat, Long start, Long end, ServletContext context) {

        MongoCursor<Document> cursor = findDocumentByUserAndWindow(user,start,end,getCollection(context));

        return getDataSet(stat.getParam(),RadarConverter.getDescriptiveStatistic(stat),Unit.beats_per_min,cursor);
    }

    /**
     * @param field is the mongodb field that has to be extracted
     * @param stat is the statistical functional represented by the extracted field
     * @param unit is the unit of the extracted value
     * @param cursor the mongoD cursor
     * @return a HearRate dataset for the given input, otherwise null
     */
    private static HeartRateDataSet getDataSet(String field, DescriptiveStatistic stat, Unit unit, MongoCursor<Document> cursor){
        Date start = null;
        Date end = null;

        LinkedList<HeartRateItem> list = new LinkedList<>();

        if(!cursor.hasNext()){
            logger.info("Empty cursor");
            return null;
        }

        while (cursor.hasNext()) {

            Document doc = cursor.next();

            if(start == null){
                start = doc.getDate("start");
            }
            end = doc.getDate("end");

            EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(doc.getDate("start")),RadarConverter.getISO8601(doc.getDate("end")));

            list.addLast(new HeartRateItem(doc.getDouble(field),etf));
        }

        cursor.close();

        EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(start),RadarConverter.getISO8601(end));

        HeartRateDataSet hrd = new HeartRateDataSet(list,etf,stat,unit);

        logger.info("Found {} value",list.size());

        return hrd;
    }

    /**
     * @param field is the mongodb field that has to be extracted
     * @param stat is the statistical functional represented by the extracted field
     * @param unit is the unit of the extracted value
     * @param cursor the mongoD cursor
     * @return a HearRate single value for the given input, otherwise null
     */
    private static HeartRate getValue(String field, DescriptiveStatistic stat, Unit unit, MongoCursor<Document> cursor){
        HeartRate hr = null;

        if (cursor.hasNext()) {

            Document doc = cursor.next();

            EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(doc.getDate("start")),RadarConverter.getISO8601(doc.getDate("end")));
            HeartRateValue hrv = new HeartRateValue(doc.getDouble(field),unit);

            hr = new HeartRate(hrv,etf,stat);
        }

        cursor.close();

        if(hr == null) {
            logger.info("No HR value found");
        }

        return hr;
    }

    /**
     * @param context is the servelet context needed to retrieve the mongodb client instance
     * @return the HearRate MongoDb collection
     */
    private static MongoCollection<Document> getCollection(ServletContext context){
        return MongoDAO.getCollection(context,"heartrate");
    }

}