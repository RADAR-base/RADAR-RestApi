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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.SourceDTO;
import org.radarcns.domain.managementportal.SourceDataDTO;
import org.radarcns.domain.restapi.AggregateDataSource;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.AggregatedDataItem;
import org.radarcns.domain.restapi.dataset.AggregatedDataPoints;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.mongo.data.sourcedata.DataFormat;
import org.radarcns.mongo.data.sourcedata.MongoSourceDataWrapper;
import org.radarcns.mongo.util.MongoHelper;
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

    private final ManagementPortalClient managementPortalClient;

    private final SourceCatalog sourceCatalog;

    private final MongoClient mongoClient;

    /**
     * Constructor.
     **/
    @Inject
    public DataSetService(SourceCatalog sourceCatalog,
            ManagementPortalClient managementPortalClient, MongoClient mongoClient)
            throws IOException {
        this.managementPortalClient = managementPortalClient;
        this.sourceCatalog = sourceCatalog;
        this.mongoClient = mongoClient;
        sourceCatalog.getSourceTypes().forEach(sourceType -> {
            Set<SourceDataDTO> sourceTypeConsumer = sourceType.getSourceData();
            sourceTypeConsumer.forEach(sourceData ->
                    mongoSensorMap.put(sourceData.getSourceDataName(),
                            DataFormat.getMongoSensor(sourceData))
            );
        });

        LOGGER.info("DataSetService successfully loaded.");
    }

    /**
     * Returns an empty {@link Dataset} using given parameters.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param sourceId of source
     * @param sensor sourceDataName
     * @param stat statistic
     * @param interval timeWindow
     * @param timeFrame start to end
     * @return an instance of Dataset.
     */
    public static Dataset emptyDataset(String projectName, String subjectId, String sourceId,
            String sensor, DescriptiveStatistic stat, TimeWindow interval, TimeFrame timeFrame) {

        return new Dataset(new Header(projectName, subjectId, sourceId, "UNKNOWN", sensor, stat,
                null, interval, timeFrame, null),
                Collections.emptyList());
    }

    /**
     * Returns an empty {@link AggregatedDataPoints} using given parameters.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param interval timeWindow
     * @param timeFrame startToEnd
     * @param sources requested
     * @return an instance of AggregatedDataPoints
     */
    public static AggregatedDataPoints emptyAggregatedData(String projectName, String subjectId,
            TimeWindow interval, TimeFrame timeFrame, List<AggregateDataSource> sources) {

        return new AggregatedDataPoints(projectName, subjectId, 0, timeFrame, interval, sources,
                Collections.emptyList());
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
     * @return the last seen data value stat for the given subject and source, otherwise empty.
     * @see Dataset
     */
    public Dataset getLastReceivedSample(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow timeWindow)
            throws IOException {
        Instant now = Instant.now();
        TimeFrame timeFrame = new TimeFrame(now.minus(RadarConverter.getDuration(timeWindow)), now);

        Header header = getHeader(projectName, subjectId, sourceId,
                sourceDataName, stat, timeWindow, timeFrame);

        MongoSourceDataWrapper sourceDataWrapper = mongoSensorMap.get(sourceDataName);

        return sourceDataWrapper.getLatestRecord(projectName, subjectId, sourceId, header,
                RadarConverter.getMongoStat(stat), MongoHelper.getCollection(mongoClient,
                        sourceDataWrapper.getCollectionName(timeWindow)));
    }

    /**
     * Returns a {@code Dataset} containing all available values for the couple subject sourceType.
     *
     * @param projectName of the subject
     * @param subjectId of the subject
     * @param sourceId of the source
     * @param sourceDataName of data
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @return dataset for the given subject and sourceType, otherwise empty dataset
     * @see Dataset
     */
    public Dataset getAllDataItems(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow timeWindow)
            throws IOException {
        Header header = getHeader(projectName, subjectId, sourceId,
                sourceDataName, stat, timeWindow, null);

        MongoSourceDataWrapper sensorDao = mongoSensorMap.get(sourceDataName);

        return sensorDao.getAllRecords(projectName, subjectId, sourceId, header,
                RadarConverter.getMongoStat(stat), MongoHelper.getCollection(mongoClient,
                        sensorDao.getCollectionName(timeWindow)));
    }

    /**
     * Returns a {@link Dataset} containing all available values for the couple subject surce.
     *
     * @param projectName of the subject
     * @param subjectId of the subject
     * @param sourceId is the sourceID
     * @param sourceDataName is the required sensor type
     * @param stat is the required statistical value
     * @param timeWindow time frame resolution
     * @param start is time window start point in millisecond
     * @param end is time window end point in millisecond
     * @return dataset for the given subject and source for given query.
     * @see Dataset
     */
    public Dataset getAllRecordsInWindow(String projectName, String subjectId,
            String sourceId, String sourceDataName, DescriptiveStatistic stat,
            TimeWindow timeWindow,
            Date start, Date end) throws IOException {

        SourceDTO source = managementPortalClient.getSource(sourceId);

        TimeFrame timeFrame = new TimeFrame(start, end);

        Header header = getHeader(projectName, subjectId, sourceId,
                sourceCatalog.getSourceData(sourceDataName), stat, timeWindow,
                source.getSourceTypeIdentifier().toString(), timeFrame);

        MongoSourceDataWrapper sensorDao = mongoSensorMap.get(sourceDataName);

        return sensorDao.getAllRecordsInWindow(projectName, subjectId, sourceId, header,
                RadarConverter.getMongoStat(stat), start, end,
                MongoHelper.getCollection(mongoClient,
                        sensorDao.getCollectionName(timeWindow)));
    }

    private Header getHeader(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow timeWindow,
            TimeFrame timeFrame)
            throws IOException {
        SourceDTO source = managementPortalClient.getSource(sourceId);

        return getHeader(projectName, subjectId, sourceId,
                sourceCatalog.getSourceData(sourceDataName), stat, timeWindow,
                source.getSourceTypeIdentifier().toString(), timeFrame);
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
     * @see Dataset
     */
    private Header getHeader(String project, String subject, String source,
            SourceDataDTO sourceData, DescriptiveStatistic stat, TimeWindow timeWindow,
            String sourceType, TimeFrame timeFrame) {
        return new Header(project, subject, source, sourceType, sourceData.getSourceDataType(),
                stat, sourceData.getUnit(), timeWindow, timeFrame, null);
    }

    /**
     * Returns calculated {@link AggregatedDataPoints} using given parameters.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param sources requested
     * @param timeWindow interval
     * @param start time
     * @param end time
     * @return calculated data.
     */
    public AggregatedDataPoints getAggregatedData(String projectName, String subjectId,
            List<AggregateDataSource> sources, TimeWindow timeWindow, Instant start, Instant end) {
        List<AggregatedDataItem> dataItems = calculateIntervals(start, end, timeWindow).stream()
                .map(p -> computeAggregatedDataItem(projectName, subjectId, sources, p, timeWindow))
                .collect(Collectors.toList());

        return new AggregatedDataPoints(projectName, subjectId,
                dataItems.stream().map(AggregatedDataItem::getCount).reduce(Integer::max).get(),
                new TimeFrame(start, end), timeWindow, sources, dataItems);
    }

    private AggregatedDataItem computeAggregatedDataItem(String projectName, String subjectId,
            List<AggregateDataSource> aggregateDataSources, TimeFrame timeFrame,
            TimeWindow timeWindow) {
        Integer count = 0;
        for (AggregateDataSource aggregate : aggregateDataSources) {
            for (String sourceDataName : aggregate.getSourceDataName()) {
                MongoSourceDataWrapper sourceDataWrapper = mongoSensorMap.get(sourceDataName);
                count += sourceDataWrapper.doesExist(projectName, subjectId, aggregate
                                .getSourceId(), Date.from(timeFrame.getStartDateTime()),
                        Date.from(timeFrame.getEndDateTime()),
                        MongoHelper.getCollection(mongoClient,
                                sourceDataWrapper.getCollectionName(timeWindow)));
            }
        }
        return new AggregatedDataItem(count, timeFrame);
    }

    private List<TimeFrame> calculateIntervals(Instant start, Instant end, TimeWindow timeWindow) {
        Instant intervalStartDateTime = start;
        List<TimeFrame> timeFrames = new ArrayList<>();
        while (intervalStartDateTime.plus(RadarConverter.getDuration(timeWindow)).isBefore(end)
                || intervalStartDateTime.plus(RadarConverter.getDuration(timeWindow)).equals(end)) {
            Instant intervalEndTime = intervalStartDateTime
                    .plus(RadarConverter.getDuration(timeWindow));
            timeFrames.add(new TimeFrame(intervalStartDateTime, intervalEndTime));
            intervalStartDateTime = intervalEndTime;
        }
        return timeFrames;
    }
}
