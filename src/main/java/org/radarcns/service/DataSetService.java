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

import static org.radarcns.util.ThrowingFunction.tryOrRethrow;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
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
import org.radarcns.domain.restapi.header.DataSetHeader;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.mongo.data.passive.SourceDataMongoWrapper;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.radarcns.util.TimeScale;
import org.radarcns.webapp.exception.BadGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Data Access Object database independent.
 */
public class DataSetService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetService.class);

    private final ManagementPortalClient managementPortalClient;

    private final SourceCatalog sourceCatalog;

    private final MongoClient mongoClient;

    /**
     * Constructor.
     **/
    @Inject
    public DataSetService(SourceCatalog sourceCatalog,
            ManagementPortalClient managementPortalClient, MongoClient mongoClient) {
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
                new DataSetHeader(projectName, subjectId, sourceId, "UNKNOWN",
                        sourceDataName, stat, null, interval, timeFrame, null),
                Collections.emptyList());
    }

    /**
     * Returns an empty {@link AggregatedDataPoints} using given parameters.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param timeScale time scale
     * @param sources requested
     * @return an instance of AggregatedDataPoints
     */
    public static AggregatedDataPoints emptyAggregatedData(String projectName, String subjectId,
            TimeScale timeScale, List<AggregateDataSource> sources) {

        return new AggregatedDataPoints(projectName, subjectId, 0,
                timeScale, sources, Collections.emptyList());
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
        TimeScale timeScale = new TimeScale(
                new TimeFrame(now.minus(TimeScale.getDuration(timeWindow)), now),
                timeWindow);

        DataSetHeader header = getHeader(projectName, subjectId, sourceId,
                sourceDataName, stat, timeScale);

        SourceDataMongoWrapper sourceData = this.sourceCatalog
                .getSourceDataWrapper(sourceDataName);

        return sourceData.getLatestRecord(projectName, subjectId, sourceId, header,
                RadarConverter.getMongoStat(stat),
                MongoHelper.getCollection(mongoClient, sourceData.getCollectionName(timeWindow)));
    }

    /**
     * Returns a {@link Dataset} containing all available values for the couple subject source.
     *
     * @param projectName of the subject
     * @param subjectId of the subject
     * @param sourceId is the sourceID
     * @param sourceDataName is the required sensor type
     * @param stat is the required statistical value
     * @param timeScale time frame resolution
     * @return dataset for the given subject and source for given query.
     * @see Dataset
     */
    public Dataset getAllRecordsInWindow(String projectName, String subjectId,
            String sourceId, String sourceDataName, DescriptiveStatistic stat, TimeScale timeScale)
            throws IOException {

        SourceDTO source = managementPortalClient.getSource(sourceId);

        SourceDataMongoWrapper sourceData = this.sourceCatalog.getSourceDataWrapper(sourceDataName);

        DataSetHeader header = getHeader(projectName, subjectId, sourceId,
                sourceData.getSourceData(), stat, timeScale,
                source.getSourceTypeIdentifier().toString());

        return sourceData.getAllRecordsInWindow(
                MongoHelper.getCollection(mongoClient, sourceData.getCollectionName(timeScale)),
                projectName, subjectId, sourceId, header, RadarConverter.getMongoStat(stat),
                timeScale.getTimeFrame());
    }

    private DataSetHeader getHeader(String projectName, String subjectId, String sourceId,
            String sourceDataName, DescriptiveStatistic stat, TimeScale timeScale)
            throws IOException {
        SourceDTO source = managementPortalClient.getSource(sourceId);

        return getHeader(projectName, subjectId, sourceId,
                this.sourceCatalog.getSourceDataWrapper(sourceDataName).getSourceData(), stat,
                timeScale, source.getSourceTypeIdentifier().toString());
    }

    /**
     * Returns a {@link Header} that can be used to constract a {@link Dataset}.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param sourceData is sourceData involved in the operation
     * @param stat {@link DescriptiveStatistic} stating the required statistical value
     * @return {@link Header} related to the given inputs
     * @see Dataset
     */
    private DataSetHeader getHeader(String project, String subject, String source,
            SourceDataDTO sourceData, DescriptiveStatistic stat, TimeScale timeScale,
            String sourceType) {
        return new DataSetHeader(project, subject, source, sourceType,
                sourceData.getSourceDataType(), stat, sourceData.getUnit(),
                timeScale.getTimeWindow(), timeScale.getTimeFrame(),
                null);
    }

    /**
     * Returns calculated {@link AggregatedDataPoints} using given parameters. The result is also
     * updated with the the relevant sourceDataType of sourceData requested.
     *
     * @param projectName of project
     * @param subjectId of subject
     * @param sources requested
     * @param timeScale time scale
     * @return calculated data.
     */
    public AggregatedDataPoints getDistinctData(String projectName, String subjectId,
            List<AggregateDataSource> sources, TimeScale timeScale) {
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

        List<DataItem> dataItems = timeScale.streamIntervals()
                .map(t -> collectDistinctSources(projectName, subjectId, sources, t,
                        timeScale.getTimeWindow()))
                .collect(Collectors.toList());

        Integer maximumValue = dataItems.isEmpty() ? null : dataItems.stream()
                .mapToInt(p -> ((Number)p.getValue()).intValue())
                .max()
                .orElseThrow(() -> new IllegalStateException("Data items are empty"));

        return new AggregatedDataPoints(projectName, subjectId,
                maximumValue, timeScale, sources, dataItems);
    }

    private DataItem collectDistinctSources(String projectName, String subjectId,
            List<AggregateDataSource> aggregateDataSources, TimeFrame timeFrame,
            TimeWindow timeWindow) {
        int count = aggregateDataSources.stream()
                .mapToInt(aggregate -> (int) aggregate.getSourceData().stream()
                        .map(tryOrRethrow(s -> this.sourceCatalog.getSourceDataWrapper(s.getName()),
                                BadGatewayException::new))
                        .filter(wrapper -> {
                            MongoCollection<Document> collection = MongoHelper.getCollection(
                                    mongoClient, wrapper.getCollectionName(timeWindow));

                            return wrapper.anyRecordsExist(collection, projectName, subjectId,
                                    aggregate.getSourceId(), timeFrame);
                        })
                        .count())
                .sum();

        return new DataItem(count, timeFrame.getStartDateTime());
    }
}
