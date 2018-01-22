/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.dao.mongo.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.dao.mongo.data.sensor.DataFormat;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.restapi.dataset.Item;
import org.radarcns.restapi.header.DescriptiveStatistic;
import org.radarcns.restapi.header.EffectiveTimeFrame;
import org.radarcns.restapi.header.Header;
import org.radarcns.listener.managementportal.SourceCatalog;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic MongoDB Data Access Object for data generated by sensor.
 */
public abstract class MongoSensor extends MongoDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoSensor.class);

    private final Map<String, Map<TimeWindow, String>> deviceCollections;
    private final Map<String, String> collectionToSource;

    private final String sensorType;
    private final DataFormat dataFormat;

    /**
     * Constructs a MongoSensor able to query the collections of the sensor for the given source.
     *
     * @param sensorType sensor of the given source that will be consume from this instance
     */
    public MongoSensor(DataFormat format, String sensorType) {

        deviceCollections = new HashMap<>();
        collectionToSource = new HashMap<>();

        this.sensorType = sensorType;
        this.dataFormat = format;

//        for (String sourceType : SourceCatalog.getInstance().getSupportedSource()) {
//            if (!SourceCatalog.getInstance(sourceType).isSupported(sensorType)) {
//                continue;
//            }
//
//            deviceCollections.put(sourceType,
//                    SourceCatalog.getInstance(sourceType).getCollections().get(sensorType));
//
//            Set<String> names = new HashSet<>(SourceCatalog.getInstance(
//                    sourceType).getCollections().get(sensorType).values());
//
//            for (String name : names) {
//                collectionToSource.put(name, sourceType);
//            }
//        }
    }

    /**
     * Returns the {@code SensorType} related to this instance.
     */
    public String getSensorType() {
        return sensorType;
    }

    /**
     * Returns the {@code DataFormat} related to this instance.
     */
    public DataFormat getDataFormat() {
        return dataFormat;
    }

    /**
     * Returns a {@code Dataset} containing the last seen value for the couple subject source.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param header information used to provide the data context
     * @param collection is the mongoDb collection that has to be queried
     * @return the last seen data value stat for the given subject and source, otherwise
     *      empty dataset
     *
     * @see Dataset
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Dataset valueRTByUserSource(String subject, String source, Header header, Stat stat,
            MongoCollection<Document> collection) throws ConnectException {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSource(subject, source, MongoHelper.END, -1, 1,
                collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject source.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @param collection is the mongoDb collection that has to be queried
     * @return data dataset for the given subject and source, otherwise empty dataset
     *
     * @see Dataset
     */
    public Dataset valueByUserSource(String subject, String source, Header header,
            MongoHelper.Stat stat, MongoCollection<Document> collection) throws ConnectException {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSource(subject, source,MongoHelper.START, 1, null,
                collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject source.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param collection is the mongoDb collection that has to be queried
     * @return data dataset for the given subject and source within the start and end time window,
     *      otherwise empty dataset
     *
     * @see Dataset
     */
    public Dataset valueByUserSourceWindow(String subject, String source, Header header,
            MongoHelper.Stat stat, Long start, Long end, MongoCollection<Document> collection)
            throws ConnectException {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSourceWindow(subject, source, start, end, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Counts the received messages within the time-window [start-end] for the couple subject
     *      source.
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param collection is the mongoDb collection that has to be queried
     * @return the number of received messages within the time-window [start-end].
     */
    public double countSamplesByUserSourceWindow(String subject, String source, Long start,
            Long end, MongoCollection<Document> collection) throws ConnectException {
        double count = 0;
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByUserSourceWindow(subject, source, start, end, collection);

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
        }

        while (cursor.hasNext()) {
            Document doc = (Document) cursor.next();
            count += extractCount(doc);
        }

        cursor.close();

        return count;
    }

    /**
     * Builds the required {@link Dataset}. It adds the {@link EffectiveTimeFrame} to the given
     *      {@link Header}.
     *
     * @param field is the mongodb field that has to be extracted
     * @param stat is the statistical functional represented by the extracted field
     * @param header information to provide the context of the data set
     * @param cursor the mongoD cursor
     * @return data dataset for the given input, otherwise empty dataset
     *
     * @see Dataset
     */
    private Dataset getDataSet(String field, DescriptiveStatistic stat, Header header,
            MongoCursor<Document> cursor) {
        Date start = null;
        Date end = null;

        LinkedList<Item> list = new LinkedList<>();

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            cursor.close();
            return new Dataset(null, list);
        }

        while (cursor.hasNext()) {
            Document doc = cursor.next();

            Date localStart = doc.getDate(MongoHelper.START);
            Date localEnd = doc.getDate(MongoHelper.END);

            if (start == null) {
                start = localStart;
                end = localEnd;
            } else {
                if (start.after(localStart)) {
                    start = localStart;
                }
                if (end.before(localEnd)) {
                    end = localEnd;
                }
            }

            Item item = new Item(docToAvro(doc, field, stat, header),
                    RadarConverter.getISO8601(doc.getDate(MongoHelper.START)));

            list.addLast(item);
        }

        cursor.close();

        EffectiveTimeFrame etf = new EffectiveTimeFrame(
                RadarConverter.getISO8601(start),
                RadarConverter.getISO8601(end));

        header.setEffectiveTimeFrame(etf);

        Dataset hrd = new Dataset(header, list);

        LOGGER.debug("Found {} value", list.size());

        return hrd;
    }

    /**
     * Finds source type for the given subject using the source catalog.
     *
     * @param collection name
     */
    @Override
    public String getSourceType(String collection) {
        if (!collectionToSource.containsKey(collection)) {
            throw new IllegalArgumentException(collection + " is an unknown collection");
        }

        return collectionToSource.get(collection);
    }

    /**
     * Returns the required mongoDB collection name for the given source type.
     *
     * @param source source type
     * @param interval useful to identify which collection has to be queried. A sensor has a
     *      collection for each time frame or time window
     * @return the MongoDB Collection name
     */
    @Override
    public String getCollectionName(String source, TimeWindow interval) {
        if (deviceCollections.containsKey(source)) {
            return deviceCollections.get(source).get(interval);
        }

        throw new IllegalArgumentException("Unknown source type. " + source
            + "is not yest supported.");
    }

    /**
     * Returns all available collections.
     *
     * @return the MongoDB Collection name
     */
    @Override
    public Set<String> getCollectionNames() {
        return collectionToSource.keySet();
    }

    /**
     * Convert a {@link Document} to the corresponding
     *      {@link org.apache.avro.specific.SpecificRecord}.
     *
     * @param doc {@link Document} storing data used to create the related {@link Item}
     * @param field key of the value that has to be extracted from the {@link Document}
     * @param stat {@link DescriptiveStatistic} represented by the resulting {@link Item}
     * @param header {@link Header} used to provide the data context
     *
     * @implSpec this function must be override by the subclass
     *
     * @return the {@link DataFormat} related to the sensor
     */
    protected Object docToAvro(Document doc, String field, DescriptiveStatistic stat,
            Header header) {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * Extract the count information for the given MongoDB document.
     * @param doc is the Bson Document from which we extract the required value to compute the
     *      count value
     * @implSpec this function should be override by the subclass
     * @return the count value
     */
    protected int extractCount(Document doc) {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }
}