package org.radarcns.dao;

/*
 *  Copyright 2016 King's College London and The Hyve
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
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
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
     * Returns a {@code Dataset} containing the last seen value for the couple user source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeFrame time frame resolution
     * @param sensorType is the required sensor type
     * @param context is the servlet context needed to retrieve the database client INSTANCE
     * @return the last seen data value stat for the given user and source, otherwise
     *      empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Dataset valueRTByUserSource(String user, String source, DescriptiveStatistic stat,
            TimeFrame timeFrame, SensorType sensorType, ServletContext context)
            throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);
        SourceType sourceType = SourceDataAccessObject.getSourceType(source, client);
        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType);

        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.valueRTByUserSource(user, source, unit, RadarConverter.getMongoStat(stat),
                timeFrame, MongoHelper.getCollection(context,
                        sensorDao.getCollectionName(sourceType, timeFrame)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple user source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeFrame time frame resolution
     * @param sensorType is the required sensor type
     * @param context is the servlet context needed to retrieve the database client INSTANCE
     * @return data dataset for the given user and source, otherwise empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    public Dataset valueByUserSource(String user, String source, DescriptiveStatistic stat,
            TimeFrame timeFrame, SensorType sensorType, ServletContext context)
            throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDataAccessObject.getSourceType(source, client);
        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType);

        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.valueByUserSource(user, source, unit, RadarConverter.getMongoStat(stat),
            timeFrame, MongoHelper.getCollection(
                context, sensorDao.getCollectionName(sourceType, timeFrame)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple user surce.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param timeFrame time frame resolution
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param sensorType is the required sensor type
     * @param context is the servlet context needed to retrieve the database client INSTANCE
     * @return data dataset for the given user and source within the start and end time window,
     *      otherwise empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    public Dataset valueByUserSourceWindow(String user, String source, DescriptiveStatistic stat,
            TimeFrame timeFrame, Long start, Long end, SensorType sensorType,
            ServletContext context) throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDataAccessObject.getSourceType(source, client);
        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType);

        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.valueByUserSourceWindow(user, source, unit,
            RadarConverter.getMongoStat(stat), timeFrame, start, end,
            MongoHelper.getCollection(context, sensorDao.getCollectionName(sourceType, timeFrame)));
    }

    /**
     * Counts the received messages within the time-window [start-end] for the couple user source.
     * @param user is the userID
     * @param source is the sourceID
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param sensorType is the required sensor type
     * @param sourceType is the required source type
     * @return the number of received messages within the time-window [start-end].
     */
    public double countSamplesByUserSourceWindow(String user, String source, Long start, Long end,
            SensorType sensorType, SourceType sourceType, MongoClient client)
            throws ConnectException {
        MongoSensor sensorDao = hooks.get(sensorType);

        return sensorDao.countSamplesByUserSourceWindow(user, source, start, end,
                MongoHelper.getCollection(client,
                sensorDao.getCollectionName(sourceType, TimeFrame.TEN_SECOND)));
    }

    /**
     * Finds all users checking all available collections.
     *
     * @param client MongoDB client
     * @return a study {@code Cohort}
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link org.radarcns.avro.restapi.user.Cohort}
     */
    public Set<String> findAllUsers(MongoClient client) throws ConnectException {
        Set<String> users = new HashSet<>();

        for (MongoSensor mongoSensor : hooks.values()) {
            users.addAll(mongoSensor.findAllUser(client));
        }

        return users;
    }

    /**
     * Returns all available sources for the given patient.
     *
     * @param user user identifier.
     * @param client MongoDb client
     * @return a {@code Patient} object
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link org.radarcns.avro.restapi.user.Patient}
     */
    public Set<Source> findAllSourcesByUser(String user, MongoClient client)
            throws ConnectException {
        Set<Source> sources = new HashSet<>();

        for (MongoSensor mongoSensor : hooks.values()) {
            sources.addAll(mongoSensor.findAllSourcesByUser(user, client));
        }

        return sources;
    }

    /**
     * Finds the source type for the given sourceID
     *
     * @param source SourceID
     * @param client MongoDB client
     * @return a study {@code SourceType}
     *
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link org.radarcns.avro.restapi.source.SourceType}
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
     * Returns the singleton.
     * @return the singleton {@code SensorDataAccessObject} INSTANCE
     */
    public String getCollectionName(SourceType sourceType, SensorType sensorType,
            TimeFrame timeFrame) {
        return hooks.get(sensorType).getCollectionName(sourceType, timeFrame);
    }

    /**
     * Returns all supported {@code SensorType}s.
     * @return collection of all {@code SensorType} for which a Data Access Object has been defined.
     */
    public Collection<SensorType> getSupportedSensor() {
        return hooks.keySet();
    }
}
