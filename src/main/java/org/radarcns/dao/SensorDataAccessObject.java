package org.radarcns.dao;

/*
 * Copyright 2016 King's College London and The Hyve
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

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.subject.Subject;
import org.radarcns.dao.mongo.data.sensor.DataFormat;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoSensor;
import org.radarcns.source.SourceCatalog;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Data Accesss Object database independent.
 */
public class SensorDataAccessObject {

    /** Logger. **/
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataAccessObject.class);

    /** Map containing actual implementations of each data DAO. **/
    private final Map<SensorType, MongoSensor> hooks;

    /** Singleton INSTANCE. **/
    private static SensorDataAccessObject INSTANCE;

    /** Constructor. **/
    private SensorDataAccessObject() {
        hooks = new HashMap<>();

        for (SensorType sensor : SourceCatalog.getInstance().getSupportedSensor()) {
            hooks.put(sensor, DataFormat.getMongoSensor(sensor));
        }

        LOGGER.info("SensorDataAccessObject successfully loaded.");
    }

    /**
     * Static initializer.
     */
    static {
        INSTANCE = new SensorDataAccessObject();
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code SensorDataAccessObject} INSTANCE
     */
    public static SensorDataAccessObject getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the singleton Data Access Object associated with the sensor for the given source.
     *
     * @param sensorType sensor of interest
     * @return {@code MongoSensor} associated with the requested sensor for the given source
     */
    public static MongoSensor getInstance(SensorType sensorType) {
        return INSTANCE.hooks.get(sensorType);
    }

    /**
     * Returns a {@code Dataset} containing the last seen value for the couple subject source.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeFrame time frame resolution
     * @param sensorType is the required sensor type
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return the last seen data value stat for the given subject and source, otherwise
     *      empty dataset
     *
     * @see Dataset
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Dataset valueRTBySubjectSource(String subject, String source, DescriptiveStatistic stat,
            TimeFrame timeFrame, SensorType sensorType, ServletContext context)
            throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDataAccessObject.getSourceType(source, client);

        if (sourceType == null) {
            return new Dataset(null, new LinkedList<Item>());
        }

        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType);

        Header header = new Header(subject, source, sourceType, sensorType, stat, unit,
                timeFrame, null);

        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.valueRTByUserSource(subject, source, header,
                    RadarConverter.getMongoStat(stat), MongoHelper.getCollection(context,
                        sensorDao.getCollectionName(sourceType, timeFrame)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject source.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeFrame time frame resolution
     * @param sensorType is the required sensor type
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return data dataset for the given subject and source, otherwise empty dataset
     *
     * @see Dataset
     */
    public Dataset valueBySubjectSource(String subject, String source, DescriptiveStatistic stat,
            TimeFrame timeFrame, SensorType sensorType, ServletContext context)
            throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDataAccessObject.getSourceType(source, client);

        if (sourceType == null) {
            return new Dataset(null, new LinkedList<Item>());
        }

        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType);

        Header header = new Header(subject, source, sourceType, sensorType, stat, unit,
                timeFrame, null);

        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.valueByUserSource(subject, source, header,
                RadarConverter.getMongoStat(stat), MongoHelper.getCollection(context,
                    sensorDao.getCollectionName(sourceType, timeFrame)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject surce.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeFrame time frame resolution
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param sensorType is the required sensor type
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return data dataset for the given subject and source within the start and end time window,
     *      otherwise empty dataset
     *
     * @see Dataset
     */
    public Dataset valueBySubjectSourceWindow(String subject, String source,
            DescriptiveStatistic stat, TimeFrame timeFrame, Long start, Long end,
            SensorType sensorType, ServletContext context) throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDataAccessObject.getSourceType(source, client);

        if (sourceType == null) {
            return new Dataset(null, new LinkedList<Item>());
        }

        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType);

        Header header = new Header(subject, source, sourceType, sensorType, stat, unit,
                timeFrame, null);

        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.valueByUserSourceWindow(subject, source, header,
                RadarConverter.getMongoStat(stat), start, end, MongoHelper.getCollection(context,
                    sensorDao.getCollectionName(sourceType, timeFrame)));
    }

    /**
     * Counts the received messages within the time-window [start-end] for the couple subject
     *      source.
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param sensorType is the required sensor type
     * @param sourceType is the required source type
     * @return the number of received messages within the time-window [start-end].
     */
    public double countSamplesByUserSourceWindow(String subject, String source, Long start,
            Long end, SensorType sensorType, SourceType sourceType, MongoClient client)
            throws ConnectException {
        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.countSamplesByUserSourceWindow(subject, source, start, end,
                MongoHelper.getCollection(client,
                sensorDao.getCollectionName(sourceType, TimeFrame.TEN_SECOND)));
    }

    /**
     * Finds all subjects checking all available collections.
     *
     * @param client MongoDB client
     * @return a {@code Set<String>} containing all Subject Identifier
     * @throws ConnectException if MongoDB is not available
     *
     */
    public Set<String> findAllUsers(MongoClient client) throws ConnectException {
        Set<String> subjects = new HashSet<>();

        for (MongoSensor mongoSensor : hooks.values()) {
            subjects.addAll(mongoSensor.findAllUser(client));
        }

        return subjects;
    }

    /**
     * Returns all available sources for the given subject.
     *
     * @param subject subject identifier.
     * @param client MongoDb client
     * @return a {@code Set<Source>} containing all {@link Source} used by the given {@code subject}
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link Subject}
     * @see {@link Source}
     */
    public Set<Source> findAllSourcesBySubject(String subject, MongoClient client)
            throws ConnectException {
        Set<Source> sources = new HashSet<>();

        for (MongoSensor mongoSensor : hooks.values()) {
            sources.addAll(mongoSensor.findAllSourcesByUser(subject, client));
        }

        return sources;
    }

    /**
     * Finds the source type for the given sourceID.
     *
     * @param source source identifier
     * @param client {@link MongoClient} used to connect to the database
     * @return a study {@code SourceType}
     *
     * @throws ConnectException if MongoDB is not available
     *
     * @see SourceType
     */
    public SourceType findSourceType(String source, MongoClient client) throws ConnectException {
        SourceType type =  null;

        for (MongoSensor mongoSensor : hooks.values()) {
            type = mongoSensor.findSourceType(source, client);

            if (type != null) {
                return type;
            }
        }

        return type;
    }

    /**
     * Returns {@link EffectiveTimeFrame} during which the {@code subject} have sent data.
     *
     * @param subject subject identifier
     * @param client {@link MongoClient} used to connect to the database
     *
     * @return {@link EffectiveTimeFrame} reporting the interval during which the subject has sent
     *      data into the platform
     *
     * @throws ConnectException if the connection with MongoDb cannot be established
     */
    public EffectiveTimeFrame getUserEffectiveTimeFrame(String subject, MongoClient client)
            throws ConnectException {
        long start = Long.MAX_VALUE;
        long end = Long.MIN_VALUE;

        boolean min = true;

        Set<Source> sources = findAllSourcesBySubject(subject, client);

        for (MongoSensor mongoSensor : hooks.values()) {
            for (Source source : sources) {
                start = Math.min(start,
                        mongoSensor.getTimestamp(subject, source.getId(), min, client).getTime());
                end = Math.max(end,
                    mongoSensor.getTimestamp(subject, source.getId(), !min, client).getTime());
            }
        }

        return new EffectiveTimeFrame(RadarConverter.getISO8601(start),
                RadarConverter.getISO8601(end));
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code SensorDataAccessObject} INSTANCE
     */
    public String getCollectionName(SourceType sourceType, SensorType sensorType,
            TimeFrame timeFrame) {
        return hooks.get(sensorType).getCollectionName(sourceType, timeFrame);
    }

    /**
     * Returns all supported {@code SensorType}s.
     * @return collection of all {@code SensorType} for which a Data Access Object has been
     *      defined.
     */
    public Collection<SensorType> getSupportedSensor() {
        return hooks.keySet();
    }
}
