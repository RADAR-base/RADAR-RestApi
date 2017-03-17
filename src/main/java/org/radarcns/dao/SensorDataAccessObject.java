package org.radarcns.dao;

/*
 *  Copyright 2016 Kings College London and The Hyve
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.dao.mongo.SourceDAO;
import org.radarcns.dao.mongo.sensor.AccelerationDAO;
import org.radarcns.dao.mongo.sensor.BatteryDAO;
import org.radarcns.dao.mongo.sensor.BloodVolumePulseDAO;
import org.radarcns.dao.mongo.sensor.ElectrodermalActivityDAO;
import org.radarcns.dao.mongo.sensor.HeartRateDAO;
import org.radarcns.dao.mongo.sensor.InterBeatIntervalDAO;
import org.radarcns.dao.mongo.sensor.TemperatureDAO;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoSensorDAO;
import org.radarcns.source.SourceCatalog;
import org.radarcns.util.RadarConverter;

/**
 * Generic Data Accesss Object database independent.
 */
public class SensorDataAccessObject {

    /** Logger. **/
    //private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataAccessObject.class);

    /** Map containing actual implementations of each sensor DAO. **/
    private final HashMap<SensorType, MongoSensorDAO> hooks;

    /** Singleton instance. **/
    private static SensorDataAccessObject instance;

    /** Constructor. **/
    private SensorDataAccessObject() {
        hooks = new HashMap<>();

        hooks.put(SensorType.ACCELEROMETER, AccelerationDAO.getInstance());
        hooks.put(SensorType.BATTERY, BatteryDAO.getInstance());
        hooks.put(SensorType.BLOOD_VOLUME_PULSE, BloodVolumePulseDAO.getInstance());
        hooks.put(SensorType.ELECTRODERMAL_ACTIVITY, ElectrodermalActivityDAO.getInstance());
        hooks.put(SensorType.HEART_RATE, HeartRateDAO.getInstance());
        hooks.put(SensorType.INTER_BEAT_INTERVAL, InterBeatIntervalDAO.getInstance());
        hooks.put(SensorType.THERMOMETER, TemperatureDAO.getInstance());
    }

    /**
     * Static initializer.
     */
    static {
        instance = new SensorDataAccessObject();
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code SensorDataAccessObject} instance
     */
    public static SensorDataAccessObject getInstance() {
        return instance;
    }

    /**
     * Returns a {@code Dataset} containing the last seen value for the couple user source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param context is the servlet context needed to retrieve the database client instance
     * @return the last seen sensor value stat for the given user and source, otherwise
     *      empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Dataset valueRTByUserSource(String user, String source, DescriptiveStatistic stat,
            SensorType sensor, ServletContext context) throws ConnectException {
        MongoSensorDAO sensorDao = hooks.get(sensor);

        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDAO.getSourceType(source, client);
        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensor);

        return sensorDao.valueRTByUserSource(user, source, unit, RadarConverter.getMongoStat(stat),
                MongoHelper.getCollection(context, sensorDao.getCollectionName(sourceType)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple user source.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param context is the servlet context needed to retrieve the database client instance
     * @return sensor dataset for the given user and source, otherwise empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    public Dataset valueByUserSource(String user, String source, DescriptiveStatistic stat,
            SensorType sensor, ServletContext context) throws ConnectException {
        MongoSensorDAO sensorDao = hooks.get(sensor);

        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDAO.getSourceType(source, client);
        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensor);

        return sensorDao.valueByUserSource(user, source, unit, RadarConverter.getMongoStat(stat),
            MongoHelper.getCollection(context, sensorDao.getCollectionName(sourceType)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple user surce.
     *
     * @param user is the userID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param start is time window start point in millisecond
     * @param end  is time window end point in millisecond
     * @param context is the servlet context needed to retrieve the database client instance
     * @return sensor dataset for the given user and source within the start and end time window,
     *      otherwise empty dataset
     *
     * @see {@link import org.radarcns.avro.restapi.dataset.Dataset;}
     */
    public Dataset valueByUserSourceWindow(String user, String source, DescriptiveStatistic stat,
            Long start, Long end, SensorType sensor, ServletContext context)
            throws ConnectException {
        MongoSensorDAO sensorDao = hooks.get(sensor);

        MongoClient client = MongoHelper.getClient(context);

        SourceType sourceType = SourceDAO.getSourceType(source, client);
        Unit unit = SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensor);

        return sensorDao.valueByUserSourceWindow(user, source, unit,
            RadarConverter.getMongoStat(stat), start, end,
            MongoHelper.getCollection(context, sensorDao.getCollectionName(sourceType)));
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
        MongoSensorDAO sensorDao = hooks.get(sensorType);

        return sensorDao.countSamplesByUserSourceWindow(user, source, start, end,
            MongoHelper.getCollection(client, sensorDao.getCollectionName(sourceType)));
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
        for (MongoSensorDAO dataAccessObject : hooks.values()) {



            users.addAll(dataAccessObject.findAllUser(client));
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
    public Set<Source> findAllSoucesByUser(String user, MongoClient client)
        throws ConnectException {
        Set<Source> sources = new HashSet<>();

        for (MongoSensorDAO dataAccessObject : hooks.values()) {
            sources.addAll(dataAccessObject.findAllSourcesByUser(user, client));
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
        for (MongoSensorDAO dataAccessObject : hooks.values()) {
            type = dataAccessObject.findSourceType(source, client);

            if (type != null) {
                return type;
            }
        }

        return type;
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code SensorDataAccessObject} instance
     */
    public String getCollectionName(SourceType sourceType, SensorType sensorType) {
        return hooks.get(sensorType).getCollectionName(sourceType);
    }
}
