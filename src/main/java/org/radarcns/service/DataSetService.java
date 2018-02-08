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

package org.radarcns.service;

import com.mongodb.MongoClient;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.SourceData;
import org.radarcns.domain.restapi.Source;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.EffectiveTimeFrame;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.mongo.data.sensor.DataFormat;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.mongo.util.MongoSourceDataWrapper;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Data Access Object database independent.
 */
public class DataSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    /**
     * Map containing actual implementations of each data DAO.
     **/
    private final Map<String, MongoSourceDataWrapper> mongoSensorMap = new HashMap<>();

    private SourceMonitorService sourceMonitorService;

    private ManagementPortalClient managementPortalClient;

    private SourceCatalog sourceCatalog;

    private MongoClient mongoClient;

    private SourceService sourceService;

    /**
     * Constructor.
     **/
    @Inject
    public DataSetService(SourceCatalog sourceCatalog, SourceMonitorService sourceMonitorService,
            ManagementPortalClient managementPortalClient, MongoClient mongoClient,
            SourceService sourceService)
            throws IOException {
        this.sourceMonitorService = sourceMonitorService;
        this.managementPortalClient = managementPortalClient;
        this.sourceCatalog = sourceCatalog;
        this.mongoClient = mongoClient;
        this.sourceService = sourceService;
        sourceCatalog.getSourceTypes().forEach(sourceType -> {
            List<SourceData> sourceTypeConsumer = sourceType.getSourceData();
            sourceTypeConsumer.forEach(sourceData ->
                mongoSensorMap.put(sourceData.getSourceDataName(), DataFormat.getMongoSensor
                        (sourceData))
            );
        });

        LOGGER.info("DataSetService successfully loaded.");
    }

    /**
     * Returns a {@code Dataset} containing the last seen value for the subject in source.
     *
     * @param projectName is of the subject
     * @param subjectId is the subject
     * @param sourceId is the source
     * @param sourceDataName of the data
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @return the last seen data value stat for the given subject and sourceType, otherwise empty
     * dataset
     * @see Dataset
     */
    public Dataset getLastReceivedSample(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow timeWindow)
            throws IOException {
        org.radarcns.domain.managementportal.Source source = managementPortalClient
                .getSource(sourceId);
        EffectiveTimeFrame effectiveTimeFrame = sourceMonitorService
                .getEffectiveTimeFrame(projectName, subjectId, sourceId, sourceCatalog
                        .getSourceType(source.getSourceTypeProducer(),
                                source.getSourceTypeModel(),
                                source.getSourceTypeCatalogVersion()));
        Header header = getHeader(projectName, subjectId, sourceId,
                sourceCatalog.getSourceData(sourceDataName), stat, timeWindow,
                source.getSourceTypeIdentifier().toString(), effectiveTimeFrame);

        MongoSourceDataWrapper sourceDataWrapper = mongoSensorMap.get(sourceDataName);

        return sourceDataWrapper.getLatestRecord(projectName, subjectId, sourceId, header,
                RadarConverter.getMongoStat(stat), MongoHelper.getCollection(mongoClient,
                        sourceDataWrapper.getCollectionName(timeWindow)));
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject
     * sourceType.
     *
     * @param projectName of the subject
     * @param subjectId of the subject
     * @param sourceId of the source
     * @param sourceDataName of data
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @return data dataset for the given subject and sourceType, otherwise empty dataset
     * @see Dataset
     */
    public Dataset getAllDataItems(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow timeWindow)
            throws IOException {
        org.radarcns.domain.managementportal.Source source = managementPortalClient.getSource
                (sourceId);

        EffectiveTimeFrame effectiveTimeFrame = sourceMonitorService
                .getEffectiveTimeFrame(projectName, subjectId, sourceId, sourceCatalog
                        .getSourceType(source.getSourceTypeProducer(),
                                source.getSourceTypeModel(),
                                source.getSourceTypeCatalogVersion()));

        Header header = getHeader(projectName, subjectId, sourceId, sourceCatalog.getSourceData
                        (sourceDataName), stat,
                timeWindow, source.getSourceTypeIdentifier().toString(), effectiveTimeFrame);

        MongoSourceDataWrapper sensorDao = mongoSensorMap.get(sourceDataName);

        return sensorDao.getAllRecords(projectName, subjectId, sourceId, header,
                RadarConverter.getMongoStat(stat), MongoHelper.getCollection(mongoClient,
                        sensorDao.getCollectionName(timeWindow)));
    }

    /**
     * Returns a {@link Dataset} containing alla available values for the couple subject surce.
     *
     * @param projectName of the subject
     * @param subjectId of the subject
     * @param sourceId is the sourceID
     * @param sourceDataName is the required sensor type
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @param start is time window start point in millisecond
     * @param end is time window end point in millisecond
     * @return data dataset for the given subject and sourceType within the start and end time
     * window, otherwise empty dataset
     * @see Dataset
     */
    public Dataset getAllRecordsInWindow(String projectName, String subjectId,
            String sourceId, String sourceDataName, DescriptiveStatistic stat,
            TimeWindow timeWindow,
            Long start, Long end) throws IOException {

        org.radarcns.domain.managementportal.Source source = managementPortalClient.getSource
                (sourceId);

        EffectiveTimeFrame effectiveTimeFrame = new EffectiveTimeFrame(start, end);

        Header header = getHeader(projectName, subjectId, sourceId,
                sourceCatalog.getSourceData(sourceDataName), stat, timeWindow,
                source.getSourceTypeIdentifier().toString(), effectiveTimeFrame);

        MongoSourceDataWrapper sensorDao = mongoSensorMap.get(sourceDataName);

        return sensorDao.getAllRecordsInWindow(projectName, subjectId, sourceId, header,
                RadarConverter.getMongoStat(stat), start, end,
                MongoHelper.getCollection(mongoClient,
                        sensorDao.getCollectionName(timeWindow)));
    }

    /**
     * Counts the received messages within the time-window [start-end] for the couple subject
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param start is time window start point in millisecond
     * @param end is time window end point in millisecond
     * @param sensorType is the required sensor type
     * @param sourceType is the required sourceType type
     * @return the number of received messages within the time-window [start-end].
     */
    public double count(String subject, String source, Long start,
            Long end, String sensorType, String sourceType, MongoClient client)
            throws ConnectException {
        MongoSourceDataWrapper sensorDao = mongoSensorMap.get(sensorType);

        return sensorDao.countSamplesByUserSourceWindow(subject, source, start, end,
                MongoHelper.getCollection(client,
                        sensorDao.getCollectionName(TimeWindow.TEN_SECOND)));
    }

    /**
     * Returns {@link EffectiveTimeFrame} during which the {@code subject} have sent data.
     *
     * @param subject subject identifier
     * @return {@link EffectiveTimeFrame} reporting the interval during which the subject has sent
     * data into the platform
     * @throws ConnectException if the connection with MongoDb cannot be established
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(String projectName, String subject)
            throws IOException {
        long start = Long.MAX_VALUE;
        long end = Long.MIN_VALUE;

        List<Source> sources = sourceService.getAllSourcesOfSubject(projectName, subject);

        for (Source source : sources) {
            start = Math.min(start, RadarConverter.getISO8601(source.getEffectiveTimeFrame()
                    .getStartDateTime()).getTime());
            end = Math.min(start, RadarConverter.getISO8601(source.getEffectiveTimeFrame()
                    .getEndDateTime()).getTime());
        }

        return new EffectiveTimeFrame(RadarConverter.getISO8601(start),
                RadarConverter.getISO8601(end));
    }

    /**
     * Returns the singleton.
     *
     * @return the singleton {@code DataSetService} INSTANCE
     */
    public String getCollectionName(String sensorType, TimeWindow timeWindow) {
        return mongoSensorMap.get(sensorType).getCollectionName(timeWindow);
    }

    /**
     * Returns all supported {@code SensorType}s.
     *
     * @return collection of all {@code SensorType} for which a Data Access Object has been defined.
     */
    public Collection<String> getSupportedSensor() {
        return mongoSensorMap.keySet();
    }


    /**
     * Returns a {@link Header} that can be used to constract a {@link Dataset}.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param sourceData is sourceData involved in the operation
     * @param stat {@link DescriptiveStatistic} stating the required statistical value
     * @param timeWindow time window is the time interval between two consecutive samples
     * @return {@link Header} related to the given inputs
     * @throws ConnectException if the connection with MongoDb cannot be established
     * @see Dataset
     */
    private Header getHeader(String project, String subject, String source, SourceData sourceData,
            DescriptiveStatistic stat, TimeWindow timeWindow, String sourceType,
            EffectiveTimeFrame effectiveTimeFrame) throws IOException {

        return new Header(project, subject, source, sourceType, sourceData.getSourceDataType(),
                stat, sourceData.getUnit(), timeWindow, effectiveTimeFrame);
    }
}
