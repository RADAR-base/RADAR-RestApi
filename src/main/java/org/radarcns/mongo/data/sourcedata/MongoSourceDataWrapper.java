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

package org.radarcns.mongo.data.sourcedata;

import static org.radarcns.mongo.util.MongoHelper.ASCENDING;
import static org.radarcns.mongo.util.MongoHelper.DESCENDING;
import static org.radarcns.mongo.util.MongoHelper.END;
import static org.radarcns.mongo.util.MongoHelper.KEY;
import static org.radarcns.mongo.util.MongoHelper.START;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import org.bson.Document;
import org.radarcns.domain.managementportal.SourceDataDTO;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.mongo.util.MongoHelper.Stat;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic MongoDB Data Access Object for data generated by sensor.
 */
public abstract class MongoSourceDataWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoSourceDataWrapper.class);

    private final SourceDataDTO sourceData;

    /**
     * Constructs a MongoSourceDataWrapper able to query the collections of the sensor for the given
     * sourceType.
     *
     * @param sourceData of the given sourceType that will be consume from this instance
     */
    public MongoSourceDataWrapper(SourceDataDTO sourceData) {
        this.sourceData = sourceData;
    }

    /**
     * Returns the {@code SensorType} related to this instance.
     */
    public String getSourceDataType() {
        return sourceData.getSourceDataType();
    }

    public Double getFrequency() {
        return Double.valueOf(sourceData.getFrequency());
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
    public Dataset getLatestRecord(String projectName, String subject, String source, Header
            header, Stat stat, MongoCollection<Document> collection) {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByProjectAndSubjectAndSource(projectName, subject, source, END,
                        DESCENDING, 1, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @param collection is the mongoDb collection that has to be queried
     * @return data dataset for the given subject and sourceType, otherwise empty dataset
     * @see Dataset
     */
    public Dataset getAllRecords(String projectName, String subject, String source, Header header,
            MongoHelper.Stat stat, MongoCollection<Document> collection) {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByProjectAndSubjectAndSource(projectName, subject, source, START,
                        ASCENDING, null, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
    }

    /**
     * Returns a {@code Dataset} containing alla available values for the couple subject
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param header information used to provide the data context
     * @param stat is the required statistical value
     * @param start is time window start point in millisecond
     * @param end is time window end point in millisecond
     * @param collection is the mongoDb collection that has to be queried
     * @return data-set for the given subject and source within the window, otherwise empty data-set
     * @see Dataset
     */
    public Dataset getAllRecordsInWindow(String projectName, String subject, String source,
            Header header, MongoHelper.Stat stat, Date start, Date end,
            MongoCollection<Document> collection) {
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentsByProjectAndSubjectAndSourceInWindow(projectName, subject, source,
                        start, end, collection);

        return getDataSet(stat.getParam(), RadarConverter.getDescriptiveStatistic(stat), header,
                cursor);
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
    private Dataset getDataSet(String field, DescriptiveStatistic stat, Header header,
            MongoCursor<Document> cursor) {
        Instant start = null;
        Instant end = null;

        LinkedList<DataItem> list = new LinkedList<>();

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            cursor.close();
            return new Dataset(header, list);
        }

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            Document key = (Document) doc.get(KEY);
            Document value = (Document) doc.get(VALUE);

            Date localStart = key.getDate(START);
            Date localEnd = key.getDate(END);
            Instant startInstant;

            if (localStart != null && localEnd != null) {
                startInstant = localStart.toInstant();
                Instant endInstant = localEnd.toInstant();
                if (start == null) {
                    start = startInstant;
                    end = endInstant;
                } else {
                    if (start.isAfter(startInstant)) {
                        start = startInstant;
                    }
                    if (end.isBefore(endInstant)) {
                        end = endInstant;
                    }
                }
            } else {
                startInstant = null;
            }

            list.addLast(new DataItem(
                    documentToDataFormat(value, field, stat, header),
                    startInstant));
        }

        cursor.close();

        header.setEffectiveTimeFrame(new TimeFrame(start, end));

        LOGGER.debug("Found {} value", list.size());

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
                    return topicName.concat("_output");
                case ONE_MIN:
                    return topicName.concat("_output_1min");
                case TEN_MIN:
                    return topicName.concat("_output_10min");
                case ONE_HOUR:
                    return topicName.concat("_output_1h");
                case ONE_DAY:
                    return topicName.concat("_output_1d");
                case ONE_WEEK:
                    return topicName.concat("_output_1w");
                case UNKNOWN:
                default:
                    return topicName.concat("_output");
            }
        }

        throw new IllegalArgumentException("Unknown sourceType type. " + sourceData
                + "is not yest supported.");
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
        return RadarConverter.getSecond(timeWindow) * getFrequency();
    }

    /**
     * Checks whether any record available in the collection for given time window.
     * @param projectName of project
     * @param subjectId of subject
     * @param sourceId of source
     * @param start time
     * @param end time
     * @param collection to query
     * @return 1 if any record available 0 otherwise
     */
    public Integer doesExist(String projectName, String subjectId, String sourceId,
            Date start, Date end, MongoCollection<Document> collection) {

        MongoCursor<Document> cursor = MongoHelper
                .doesExistsByProjectAndSubjectAndSourceInWindowForStartTime(projectName, subjectId,
                        sourceId, start, end, collection);
        return cursor.hasNext() ? 1 : 0;
    }
}