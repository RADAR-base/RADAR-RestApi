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

import static org.radarcns.domain.restapi.TimeWindow.ONE_DAY;
import static org.radarcns.domain.restapi.TimeWindow.ONE_HOUR;
import static org.radarcns.domain.restapi.TimeWindow.ONE_MIN;
import static org.radarcns.domain.restapi.TimeWindow.ONE_WEEK;
import static org.radarcns.domain.restapi.TimeWindow.TEN_MIN;
import static org.radarcns.domain.restapi.TimeWindow.TEN_SECOND;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import org.bson.Document;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.SourceDTO;
import org.radarcns.domain.managementportal.SourceDataDTO;
import org.radarcns.domain.restapi.AggregateDataSource;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.AggregatedDataPoints;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.format.SourceData;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.mongo.data.passive.SourceDataMongoWrapper;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.exception.BadGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Data Access Object database independent.
 */
public class DataSetService {

    private static final int MAXIMUM_NUMBER_OF_WINDOWS = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final ManagementPortalClient managementPortalClient;

    private final SourceCatalog sourceCatalog;

    private final MongoClient mongoClient;

    private static final List<Map.Entry<TimeWindow, Double>> TIME_WINDOW_LOG = Stream
            .of(TEN_SECOND, ONE_MIN, TEN_MIN, ONE_HOUR, ONE_DAY, ONE_WEEK)
            .map(w -> pair(w, Math.log(RadarConverter.getSecond(w))))
            .collect(Collectors.toList());

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

        LOGGER.info("DataSetService successfully loaded.");
    }

    /**
     * Returns an empty {@link Dataset} using given parameters.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param sourceId of source
     * @param sourceDataName sourceDataName
     * @param stat statistic
     * @param interval timeWindow
     * @param timeFrame start to end
     * @return an instance of Dataset.
     */
    public static Dataset emptyDataset(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow interval,
            TimeFrame timeFrame) {

        return new Dataset(
                new Header(projectName, subjectId, sourceId, "UNKNOWN", sourceDataName, stat,
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

        SourceDataMongoWrapper sourceDataWrapper = this.sourceCatalog
                .getSourceDataWrapper(sourceDataName);

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

        SourceDataMongoWrapper sourceDataWrapper = this.sourceCatalog
                .getSourceDataWrapper(sourceDataName);

        return sourceDataWrapper.getAllRecords(MongoHelper.getCollection(mongoClient,
                sourceDataWrapper.getCollectionName(timeWindow)), projectName, subjectId, sourceId,
                header, RadarConverter.getMongoStat(stat));
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

        SourceDataMongoWrapper sourceDataWrapper = this.sourceCatalog
                .getSourceDataWrapper(sourceDataName);

        Header header = getHeader(projectName, subjectId, sourceId,
                sourceDataWrapper.getSourceData(), stat, timeWindow,
                source.getSourceTypeIdentifier().toString(), timeFrame);

        return sourceDataWrapper.getAllRecordsInWindow(MongoHelper.getCollection(mongoClient,
                sourceDataWrapper.getCollectionName(timeWindow)), projectName, subjectId, sourceId,
                header, RadarConverter.getMongoStat(stat), timeFrame
        );
    }

    private Header getHeader(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeWindow timeWindow,
            TimeFrame timeFrame)
            throws IOException {
        SourceDTO source = managementPortalClient.getSource(sourceId);

        return getHeader(projectName, subjectId, sourceId,
                this.sourceCatalog.getSourceDataWrapper(sourceDataName).getSourceData(), stat,
                timeWindow, source.getSourceTypeIdentifier().toString(), timeFrame);
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
     * Returns calculated {@link AggregatedDataPoints} using given parameters. The result is also
     * updated with the the relevant sourceDataType of sourceData requested.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param sources requested
     * @param timeWindow interval size
     * @param timeFrame time frame to look withing
     * @return calculated data.
     */
    public AggregatedDataPoints getDistinctData(String projectName, String subjectId,
            List<AggregateDataSource> sources, TimeWindow timeWindow, TimeFrame timeFrame) {
        checkTimeFrameSize(timeFrame, timeWindow, MAXIMUM_NUMBER_OF_WINDOWS);

        // fill up sourceData.type field
        try {
            for (AggregateDataSource source : sources) {
                for (SourceData sourceData : source.getSourceData()) {
                    SourceDataDTO sourceDataDto = this.sourceCatalog.getSourceDataWrapper(sourceData
                            .getName()).getSourceData();
                    sourceData.setType(sourceDataDto.getSourceDataType());
                }
            }
        } catch (IOException exe) {
            throw new BadGatewayException(exe);
        }

        List<DataItem> dataItems = calculateIntervals(timeFrame, timeWindow)
                .map(t -> collectDistinctSources(projectName, subjectId, sources, t, timeWindow))
                .collect(Collectors.toList());

        Integer maximumValue = dataItems.stream()
                .map(p -> ((Number) p.getValue()).intValue())
                .reduce(Integer::max)
                .orElse(null);

        return new AggregatedDataPoints(projectName, subjectId,
                maximumValue, timeFrame, timeWindow, sources, dataItems);
    }

    private DataItem collectDistinctSources(String projectName, String subjectId,
            List<AggregateDataSource> aggregateDataSources, TimeFrame timeFrame,
            TimeWindow timeWindow) {
        int count = aggregateDataSources.stream()
                .map(aggregate -> (int) aggregate.getSourceData().stream()
                        .map(sourceData -> {
                            try {
                                return this.sourceCatalog
                                        .getSourceDataWrapper(sourceData.getName());
                            } catch (IOException exe) {
                                throw new BadGatewayException(exe);
                            }
                        })
                        .filter(wrapper -> {
                            MongoCollection<Document> collection = MongoHelper.getCollection(
                                    mongoClient, wrapper.getCollectionName(timeWindow));

                            return wrapper.anyRecordsExist(collection, projectName, subjectId,
                                    aggregate.getSourceId(), timeFrame);
                        })
                        .count())
                .reduce(0, Integer::sum);

        return new DataItem(count, timeFrame.getStartDateTime());
    }

    private Stream<TimeFrame> calculateIntervals(TimeFrame timeFrame, TimeWindow timeWindow) {
        TemporalAmount window = RadarConverter.getDuration(timeWindow);
        return Stream.iterate(
                windowTimeFrame(timeFrame.getStartDateTime(), window),
                t -> windowTimeFrame(t.getEndDateTime(), window))
                .limit(windowsInTimeFrame(timeFrame, timeWindow));
    }

    private static TimeFrame windowTimeFrame(Instant start, TemporalAmount duration) {
        return new TimeFrame(start, start.plus(duration));
    }

    private static long windowsInTimeFrame(TimeFrame timeFrame, TimeWindow timeWindow) {
        Duration duration = timeFrame.getDuration();
        if (duration == null) {
            throw new IllegalStateException("Start or end time of time frame unknown.");
        }
        return (long) Math.floor(duration.getSeconds()
                / (double) RadarConverter.getSecond(timeWindow));
    }

    /**
     * Get the time window that closest matches given time frame.
     *
     * @param timeFrame time frame to compute time window for
     * @param numberOfWindows number of time windows that should ideally be returned.
     * @return closest match with given time frame.
     */
    public TimeWindow getFittingTimeWindow(TimeFrame timeFrame, int numberOfWindows) {
        double logSeconds = Math.log(timeFrame.getDuration().getSeconds() / numberOfWindows);
        return TIME_WINDOW_LOG.stream()
                .map(e -> pair(e.getKey(), Math.abs(logSeconds - e.getValue())))
                .reduce((e1, e2) -> e1.getValue() < e2.getValue() ? e1 : e2)
                .orElseThrow(() -> new AssertionError("No close time window found"))
                .getKey();
    }

    /**
     * Checks that for a given time frame with given time window, the number of data points does not
     * exceed a maximum.
     *
     * @param timeFrame time frame to request
     * @param timeWindow time window granularity
     * @param maximumSize maximum number of data points
     * @throws BadRequestException if the number of data points would exceed maximum size.
     */
    public void checkTimeFrameSize(TimeFrame timeFrame, TimeWindow timeWindow, int maximumSize) {
        long requestedTimeFrames = windowsInTimeFrame(timeFrame, timeWindow);
        if (requestedTimeFrames > maximumSize) {
            throw new BadRequestException(
                    "Cannot request more than " + maximumSize + " time windows. Requested "
                            + requestedTimeFrames + " with time frame " + timeFrame
                            + " and time window " + timeWindow + '.');
        }
    }

    private static <K, V> Map.Entry<K, V> pair(K key, V value) {
        return new SimpleImmutableEntry<>(key, value);
    }
}
