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

package org.radarcns.mongo.data.passive;

import static org.radarcns.mongo.util.MongoHelper.ASCENDING;
import static org.radarcns.mongo.util.MongoHelper.DESCENDING;
import static org.radarcns.mongo.util.MongoHelper.END;
import static org.radarcns.mongo.util.MongoHelper.KEY;
import static org.radarcns.mongo.util.MongoHelper.START;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.radarcns.domain.managementportal.SourceDataDTO;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DataSetHeader;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.mongo.util.MongoHelper.Stat;
import org.radarcns.util.RadarConverter;
import org.radarcns.util.TimeScale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic MongoDB Data Access Wrapper for SourceData.
 */
public abstract class SourceDataMongoWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceDataMongoWrapper.class);

    private final SourceDataDTO sourceData;

    /**
     * Constructs a SourceDataMongoWrapper able to query the collections of the sensor for the given
     * sourceType.
     *
     * @param sourceData of the given sourceType that will be consume from this instance
     */
    public SourceDataMongoWrapper(SourceDataDTO sourceData) {
        this.sourceData = sourceData;
    }

    /**
     * Returns relevant {@link SourceDataDTO} of the wrapper.
     * @return sourceDataDto used
     */
    public SourceDataDTO getSourceData() {
        return this.sourceData;
    }

    /**
     * Returns sourceDataName of the wrapper.
     * @return sourceDataName of the wrapper
     */
    public String getSourceDataName() {
        return this.sourceData.getSourceDataName();
    }

    /**
     * Returns the {@code DataFormat} related to this instance.
     */
    public abstract DataFormat getDataFormat();

    /**
     * Returns a {@code Dataset} containing the last seen value for the couple subject sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param stat is the required statistical value
     * @param header information used to provide the data context
     * @param collection is the mongoDb collection that has to data-set.
     * @see Dataset
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Dataset getLatestRecord(String projectName, String subject, String source, DataSetHeader
            header, Stat stat, MongoCollection<Document> collection) {
        try (MongoCursor<Document> cursor = MongoHelper.findDocumentBySource(
                collection, projectName, subject, source, KEY + "." + END, DESCENDING, 1)) {
            return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                    cursor);
        }
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject
     * sourceType.
     *
     * @param collection is the mongoDb collection that has to be queried
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @return data dataset for the given subject and sourceType, otherwise empty dataset
     * @see Dataset
     */
    public Dataset getAllRecords(MongoCollection<Document> collection, String projectName,
            String subject, String source, DataSetHeader header, Stat stat) {
        try (MongoCursor<Document> cursor = MongoHelper.findDocumentBySource(
                collection, projectName, subject, source, KEY + "." + START, ASCENDING, null)) {
            return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                    cursor);
        }
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject
     * sourceType.
     *
     * @param collection is the mongoDb collection that has to be queried
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @param timeFrame is time window
     * @return data-set for the given subject and source within the window, otherwise empty data-set
     * @see Dataset
     */
    public Dataset getAllRecordsInWindow(MongoCollection<Document> collection, String projectName,
            String subject, String source, DataSetHeader header, Stat stat, TimeFrame timeFrame) {
        try (MongoCursor<Document> cursor = MongoHelper.findDocumentsBySource(
                collection, projectName, subject, source, timeFrame)) {
            return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                    cursor);
        }
    }

    /**
     * Builds the required {@link Dataset}. It adds the {@link TimeFrame} to the given {@link
     * Header}.
     *
     * @param field is the mongodb field that has to be extracted
     * @param stat is the statistical functional represented by the extracted field
     * @param header information to provide the context of the data set
     * @param cursor the mongoD cursor
     * @return data dataset for the given input, otherwise empty dataset
     * @see Dataset
     */
    private Dataset getDataSet(String field, DescriptiveStatistic stat, DataSetHeader header,
            MongoCursor<Document> cursor) {

        TimeFrame timeFrame = null;

        List<DataItem> list = new ArrayList<>();

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            return new Dataset(header, list);
        }

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Document key = (Document) doc.get(KEY);

            TimeFrame currentFrame = new TimeFrame(key.getDate(START), key.getDate(END));
            timeFrame = TimeFrame.span(timeFrame, currentFrame);

            list.add(new DataItem(
                    documentToDataFormat((Document) doc.get(VALUE), field, stat, header),
                    currentFrame.getStartDateTime()));
        }

        header.setEffectiveTimeFrame(timeFrame);

        LOGGER.debug("Found {} value(s)", list.size());

        return new Dataset(header, list);
    }

    /**
     * Returns the required mongoDB collection name for the given timeWindow of this source-data.
     *
     * @param interval of data-set query.
     * @return the MongoDB Collection name for given {@link TimeWindow}
     */
    public String getCollectionName(TimeWindow interval) {
        if (sourceData.getTopic() != null && !sourceData.getTopic().isEmpty()) {
            String topicName = sourceData.getTopic();
            switch (interval) {
                case TEN_SECOND:
                    return topicName.concat("_10sec");
                case ONE_MIN:
                    return topicName.concat("_1min");
                case TEN_MIN:
                    return topicName.concat("_10min");
                case ONE_HOUR:
                    return topicName.concat("_1hour");
                case ONE_DAY:
                    return topicName.concat("_1day");
                case ONE_WEEK:
                    return topicName.concat("_1week");
                case UNKNOWN:
                default:
                    return null;
            }
        }

        throw new IllegalArgumentException("Unknown sourceType type. " + sourceData
                + "is not yest supported.");
    }

    /**
     * Returns the required mongoDB collection name for the given timeWindow of this source-data.
     *
     * @param timeScale of data-set query.
     * @return the MongoDB Collection name for given time window.
     */
    public String getCollectionName(TimeScale timeScale) {
        return getCollectionName(timeScale.getTimeWindow());
    }

    /**
     * Convert a {@link Document} to the corresponding SpecificRecord. This function must be
     * override by the subclass
     *
     * @param doc {@link Document} storing data used to create the related {@link DataItem}
     * @param field key of the value that has to be extracted from the {@link Document}
     * @param stat {@link DescriptiveStatistic} represented by the resulting {@link DataItem}
     * @param header {@link Header} used to provide the data context
     * @return the {@link DataFormat} related to the sensor
     */
    protected Object documentToDataFormat(Document doc, String field, DescriptiveStatistic stat,
            Header header) {
        throw new UnsupportedOperationException("This function must be override by the subclass");
    }

    /**
     * Extract the count information for the given MongoDB document. This function should be
     * overridden by the subclass.
     *
     * @param doc the document from the count should be extracted.
     * @return the count value
     */
    protected abstract int extractCount(Document doc);

    public Double getExpectedRecordCount(TimeWindow timeWindow) {
        return RadarConverter.getExpectedMessages(timeWindow,
                Double.valueOf(this.sourceData.getFrequency()));
    }

    /**
     * Checks whether any record available in the collection for given time window.
     *
     * @param collection to query
     * @param projectName of project
     * @param subjectId of subject
     * @param sourceId of source
     * @param timeFrame time
     * @return {@code true} if any record available {@code false} otherwise
     */
    public boolean anyRecordsExist(MongoCollection<Document> collection, String projectName,
            String subjectId, String sourceId, TimeFrame timeFrame) {
        return MongoHelper.hasDataForSource(collection, projectName, subjectId,
                sourceId, timeFrame);
    }
}