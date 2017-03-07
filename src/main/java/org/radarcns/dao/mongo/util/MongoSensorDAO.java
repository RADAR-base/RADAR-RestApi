package org.radarcns.dao.mongo.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.Date;
import java.util.LinkedList;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoDB Data Access Object for sensors.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class MongoSensorDAO extends MongoDAO {

    private final Logger logger = LoggerFactory.getLogger(MongoSensorDAO.class);

    /**
     * Returns a {@code Dataset} containing the last seen value for the couple user source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param unit is the measurement unit
     * @param stat is the required statistical value
     * @param collection is the mongoDb collection that has to be queried
     * @return the last seen sensor value stat for the given user and source, otherwise
     *      empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Dataset valueRTByUserSource(String user, String source, Unit unit, Stat stat,
            MongoCollection<Document> collection) throws ConnectException {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSource(user, source, "end", -1, 1,
                collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), unit,
            cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple user source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param unit is the measurement unit
     * @param stat is the required statistical value
     * @param collection is the mongoDb collection that has to be queried
     * @return sensor dataset for the given user and source, otherwise empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    public Dataset valueByUserSource(String user, String source, Unit unit, MongoHelper.Stat stat,
            MongoCollection<Document> collection) throws ConnectException {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSource(user, source,"start", 1, null,
                collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), unit,
            cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple user surce.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param unit is the measurement unit
     * @param stat is the required statistical value
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param collection is the mongoDb collection that has to be queried
     * @return sensor dataset for the given user and source within the start and end time window,
     *      otherwise empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    public Dataset valueByUserSourceWindow(String user, String source, Unit unit,
            MongoHelper.Stat stat, Long start, Long end, MongoCollection<Document> collection)
            throws ConnectException {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSourceWindow(user, source, start, end, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), unit,
                cursor);
    }

    /**
     * Counts the received messages within the time-window [start-end] for the couple user source.
     * @param user is the userID
     * @param source is the sourceID
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param collection is the mongoDb collection that has to be queried
     * @return the number of received messages within the time-window [start-end].
     */
    public double countSamplesByUserSourceWindow(String user, String source, Long start, Long end,
            MongoCollection<Document> collection) throws ConnectException {
        double count = 0;
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSourceWindow(user, source, start, end, collection);

        if (!cursor.hasNext()) {
            logger.debug("Empty cursor");
        }

        while (cursor.hasNext()) {
            Document doc = cursor.next();

            try {
                count += doc.getDouble("count");
            } catch (ClassCastException exec) {
                count += extractCount((Document) doc.get("count"));
            }
        }

        cursor.close();

        return count;
    }

    /**
     * Builds the required {@Code Dataset}.
     *
     * @param field is the mongodb field that has to be extracted
     * @param stat is the statistical functional represented by the extracted field
     * @param unit is the unit of the extracted value
     * @param cursor the mongoD cursor
     * @return sensor dataset for the given input, otherwise empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    private Dataset getDataSet(String field, DescriptiveStatistic stat, Unit unit,
            MongoCursor<Document> cursor) {
        Date start = null;
        Date end = null;

        LinkedList<Item> list = new LinkedList<>();

        if (!cursor.hasNext()) {
            logger.debug("Empty cursor");
            cursor.close();
            return new Dataset(null, list);
        }

        while (cursor.hasNext()) {

            Document doc = cursor.next();

            if (start == null) {
                start = doc.getDate("start");
            }
            end = doc.getDate("end");

            EffectiveTimeFrame etf = new EffectiveTimeFrame(
                    RadarConverter.getISO8601(doc.getDate("start")),
                    RadarConverter.getISO8601(doc.getDate("end")));

            Item item = new Item(docToAvro(doc,field,stat),etf);

            list.addLast(item);
        }

        cursor.close();

        EffectiveTimeFrame etf = new EffectiveTimeFrame(
                RadarConverter.getISO8601(start),
                RadarConverter.getISO8601(end));

        Header header = new Header(stat,unit,etf);

        Dataset hrd = new Dataset(header,list);

        logger.debug("Found {} value",list.size());

        return hrd;
    }

    /**
     * Convert a MongoDB Document to the corresponding Avro object.
     * @param doc is the Bson Document from which we extract the required value to instantiate
     *      an Item
     * @implSpec this function must be override by the subclass
     * @return the required Object
     */
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat) {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * Extract the count information for the given MongoDB document.
     * @param doc is the Bson Document from which we extract the required value to compute the
     *      count value
     * @implSpec this function should be override by the subclass
     * @return the count value
     */
    protected double extractCount(Document doc) {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

}