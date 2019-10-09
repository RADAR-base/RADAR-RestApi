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

package org.radarcns.integration.util;

import static org.radarcns.domain.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MAXIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MINIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.SUM;
import static org.radarbase.mock.model.ExpectedValue.DURATION;
import static org.radarcns.mongo.util.MongoHelper.END;
import static org.radarcns.mongo.util.MongoHelper.FIELDS;
import static org.radarcns.mongo.util.MongoHelper.ID;
import static org.radarcns.mongo.util.MongoHelper.KEY;
import static org.radarcns.mongo.util.MongoHelper.NAME;
import static org.radarcns.mongo.util.MongoHelper.PROJECT_ID;
import static org.radarcns.mongo.util.MongoHelper.SOURCE_ID;
import static org.radarcns.mongo.util.MongoHelper.START;
import static org.radarcns.mongo.util.MongoHelper.USER_ID;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarbase.mock.model.ExpectedValue;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.mongo.util.MongoHelper.Stat;
import org.radarbase.stream.collector.AggregateListCollector;
import org.radarbase.stream.collector.NumericAggregateCollector;
import org.radarcns.util.TimeScale;

/**
 * It computes the expected Documents for a test case i.e. {@link ExpectedValue}.
 */
public class ExpectedDocumentFactory {

    /**
     * It return the value of the given statistical function.
     *
     * @param statistic function that has to be returned
     * @param collectors array of aggregated data
     * @return the set of values that has to be stored within a {@code Dataset} {@code Item}
     * @see NumericAggregateCollector
     **/
    public List<?> getStatValue(DescriptiveStatistic statistic,
            AggregateListCollector collectors) {

        List<NumericAggregateCollector> subCollectors = collectors.getCollectors();
        List<Object> subList = new ArrayList<>(subCollectors.size());
        for (NumericAggregateCollector collector : subCollectors) {
            subList.add(getStatValue(statistic, collector));
        }
        return subList;
    }

    /**
     * It return the value of the given statistical function.
     *
     * @param statistic function that has to be returned
     * @param collector data aggregator
     * @return the value that has to be stored within a {@code Dataset} {@code Item}
     * @see NumericAggregateCollector
     **/
    public Object getStatValue(DescriptiveStatistic statistic, NumericAggregateCollector collector) {
        switch (statistic) {
            case AVERAGE:
                return collector.getMean();
            case COUNT:
                return collector.getCount();
            case INTERQUARTILE_RANGE:
                return collector.getInterQuartileRange();
            case MAXIMUM:
                return collector.getMax();
            case MEDIAN:
                return collector.getQuartile().get(1);
            case MINIMUM:
                return collector.getMin();
            case QUARTILES:
                return collector.getQuartile();
            case SUM:
                return collector.getSum();
            default:
                throw new IllegalArgumentException(
                        statistic.toString() + " is not supported by NumericAggregateCollector");
        }
    }


    private List<Document> getDocumentsBySingle(ObservationKey key,
        ExpectedValue<?> expectedValue,
        TimeWindow timeWindow) {

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        List<Document> list = new ArrayList<>(windows.size());

        for (Long timestamp : windows) {
            NumericAggregateCollector doubleValueCollector = (NumericAggregateCollector) expectedValue
                    .getSeries().get(timestamp);
            Instant start = Instant.ofEpochMilli(timestamp);
            Instant end = start.plus(TimeScale.getDuration(timeWindow));
            list.add(buildDocument(key, start, end,
                    getDocumentFromNumericAggregateCollector("batteryLevel", doubleValueCollector)));
        }

        return list;
    }

    private Document getDocumentFromNumericAggregateCollector(String name,
            NumericAggregateCollector doubleValueCollector) {
        return new Document()
                .append(NAME, name)
                .append(Stat.min.getParam(), getStatValue(MINIMUM, doubleValueCollector))
                .append(Stat.max.getParam(), getStatValue(MAXIMUM, doubleValueCollector))
                .append(Stat.sum.getParam(), getStatValue(SUM, doubleValueCollector))
                .append(Stat.count.getParam(), getStatValue(COUNT, doubleValueCollector))
                .append(Stat.avg.getParam(), getStatValue(AVERAGE, doubleValueCollector))
                .append(Stat.quartile.getParam(), getStatValue(QUARTILES, doubleValueCollector));
    }

    private static Document buildKeyDocument(ObservationKey key, Instant start, Instant end) {
        return new Document().append(PROJECT_ID, key.getProjectId())
                .append(USER_ID, key.getUserId())
                .append(SOURCE_ID, key.getSourceId())
                .append(START, Date.from(start))
                .append(END, Date.from(end));
    }

    /**
     * Builds a {@link Document} from given parameter values.
     *
     * @param key record key
     * @param start of the measurement
     * @param end of the measurement
     * @param value document
     * @return built document
     */
    public static Document buildDocument(ObservationKey key,
            Instant start, Instant end, Document value) {
        return new Document().append(ID, buildId(key, start, end))
                .append(KEY, buildKeyDocument(key, start, end))
                .append(VALUE, value);
    }

    private static String buildId(ObservationKey key,
            Instant start, Instant end) {
        return '{'
                + PROJECT_ID + ':' + key.getProjectId() + ','
                + USER_ID + ':' + key.getUserId() + ','
                + SOURCE_ID + ':' + key.getSourceId() + ','
                + START + ':' + (start.toEpochMilli() / 1000d) + ','
                + END + ':' + (end.toEpochMilli() / 1000d) + '}';
    }

    /**
     * Builds a {@link Document} from given parameter values for source_statistics.
     *
     * @param projectName of the subject
     * @param subjectId of the subject
     * @param sourceId of the source
     * @param start of the measurement
     * @param end of the measurement
     * @return built document
     */
    public static Document getDocumentsForStatistics(String projectName, String subjectId,
            String sourceId, Instant start, Instant end) {
        ObservationKey key = new ObservationKey(projectName, subjectId, sourceId);
        return new Document().append(ID, buildId(key, start, end))
                .append(KEY, buildObservationKeyDocument(projectName, subjectId, sourceId))
                .append(VALUE, new Document()
                        .append(START, Date.from(start))
                        .append(END, Date.from(end)));
    }

    private static Document buildObservationKeyDocument(String projectName, String subjectId,
            String sourceId) {
        return new Document().append(PROJECT_ID, projectName)
                .append(USER_ID, subjectId)
                .append(SOURCE_ID, sourceId);
    }

    private List<Document> getDocumentsByArray(ObservationKey key,
        ExpectedValue<?> expectedValue) {

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        List<Document> list = new ArrayList<>(windows.size());

        for (Long timestamp : windows) {
            AggregateListCollector doubleArrayCollector = (AggregateListCollector) expectedValue
                    .getSeries().get(timestamp);

            List<Document> documents = new ArrayList<>();

            documents.add(
                    getDocumentFromNumericAggregateCollector("x",
                            doubleArrayCollector.getCollectors().get(0)));
            documents.add(
                    getDocumentFromNumericAggregateCollector("y",
                            doubleArrayCollector.getCollectors().get(1)));
            documents.add(
                    getDocumentFromNumericAggregateCollector("z",
                            doubleArrayCollector.getCollectors().get(2)));

            long end = timestamp + DURATION;

            list.add(buildDocument(key,
                    Instant.ofEpochMilli(timestamp),
                    Instant.ofEpochMilli(end),
                    new Document().append(FIELDS, documents)));
        }

        return list;
    }

    /**
     * Produces {@link List} of {@link Document}s for given {@link ExpectedValue}.
     *
     *
     * @param key
     * @param expectedValue for test
     * @return {@link List} of {@link Document}s
     */
    public List<Document> produceExpectedDocuments(ObservationKey key,
        ExpectedValue<?> expectedValue, TimeWindow
        timeWindow) {
        Map<Long, ?> series = expectedValue.getSeries();
        if (series.isEmpty()) {
            return Collections.emptyList();
        }
        Object firstCollector = series.values().iterator().next();
        if (firstCollector instanceof AggregateListCollector) {
            return getDocumentsByArray(key, expectedValue);
        } else {
            return getDocumentsBySingle(key, expectedValue, timeWindow);
        }
    }
}
