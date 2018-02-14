package org.radarcns.integration.util;

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

import static org.radarcns.domain.restapi.header.DescriptiveStatistic.AVERAGE;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MAXIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MINIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.SUM;
import static org.radarcns.mock.model.ExpectedValue.DURATION;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.mock.model.ExpectedValue;
import org.radarcns.mongo.data.sourcedata.AccelerationFormat;
import org.radarcns.mongo.util.MongoHelper.Stat;
import org.radarcns.stream.collector.DoubleArrayCollector;
import org.radarcns.stream.collector.DoubleValueCollector;

/**
 * It computes the expected Documents for a test case i.e. {@link ExpectedValue}.
 */
public class ExpectedDocumentFactory {

    //private static final Logger LOGGER = LoggerFactory.getLogger(ExpectedDocumentFactory.class);

    /**
     * It return the value of the given statistical function.
     *
     * @param statistic function that has to be returned
     * @param collectors array of aggregated data
     * @return the set of values that has to be stored within a {@code Dataset} {@code Item}
     * @see DoubleValueCollector
     **/
    public List<?> getStatValue(DescriptiveStatistic statistic,
            DoubleArrayCollector collectors) {

        List<DoubleValueCollector> subCollectors = collectors.getCollectors();
        List<Object> subList = new ArrayList<>(subCollectors.size());
        for (DoubleValueCollector collector : subCollectors) {
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
     * @see DoubleValueCollector
     **/
    public Object getStatValue(DescriptiveStatistic statistic, DoubleValueCollector collector) {
        switch (statistic) {
            case AVERAGE:
                return collector.getAvg();
            case COUNT:
                return collector.getCount();
            case INTERQUARTILE_RANGE:
                return collector.getIqr();
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
                        statistic.toString() + " is not supported by DoubleValueCollector");
        }
    }


    private List<Document> getDocumentsBySingle(ExpectedValue<?> expectedValue) {

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        List<Document> list = new ArrayList<>(windows.size());

        for (Long timestamp : windows) {
            DoubleValueCollector doubleValueCollector = (DoubleValueCollector) expectedValue
                    .getSeries().get(timestamp);

            long end = timestamp + DURATION;

            list.add(buildDocument(expectedValue.getLastKey().getProjectId(),
                    expectedValue.getLastKey().getUserId(),
                    expectedValue.getLastKey().getSourceId(), new Date(timestamp), new Date(end),
                    getDocument("batteryLevel", doubleValueCollector)));
        }

        return list;
    }

    private Document getDocument(String name, DoubleValueCollector doubleValueCollector) {
        return new Document()
                .append(NAME, name)
                .append(Stat.min.getParam(), getStatValue(MINIMUM, doubleValueCollector))
                .append(Stat.max.getParam(), getStatValue(MAXIMUM, doubleValueCollector))
                .append(Stat.sum.getParam(), getStatValue(SUM, doubleValueCollector))
                .append(Stat.count.getParam(), getStatValue(COUNT, doubleValueCollector))
                .append(Stat.avg.getParam(), getStatValue(AVERAGE, doubleValueCollector))
                .append(Stat.quartile.getParam(), extractQuartile((List<Double>) getStatValue(
                        QUARTILES, doubleValueCollector)));
    }

    public static Document buildKeyDocument(String projectName, String subjectId, String sourceId,
            Object start, Object end) {
        return new Document().append(PROJECT_ID, projectName)
                .append(USER_ID, subjectId)
                .append(SOURCE_ID, sourceId)
                .append(START, start)
                .append(END, end);
    }

    public static Document buildDocument(String projectName, String subjectId, String sourceId,
            Object start, Object end, Document value) {
        return new Document().append(ID, "{"
                + PROJECT_ID + ":" + projectName + ","
                + USER_ID + ":" + subjectId + ","
                + SOURCE_ID + ":" + sourceId + ","
                + START + ":" + start + ","
                + END + ":" + end + "}")
                .append(KEY, buildKeyDocument(projectName, subjectId, sourceId, start, end))
                .append(VALUE, value);
    }

    private List<Document> getDocumentsByArray(ExpectedValue<?> expectedValue) {

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        List<Document> list = new ArrayList<>(windows.size());

        for (Long timestamp : windows) {
            DoubleArrayCollector doubleArrayCollector = (DoubleArrayCollector) expectedValue
                    .getSeries().get(timestamp);

            long end = timestamp + DURATION;

            List<Document> documents = new ArrayList<>();

            documents.add(getDocument("x", doubleArrayCollector.getCollectors().get(0)));
            documents.add(getDocument("y", doubleArrayCollector.getCollectors().get(1)));
            documents.add(getDocument("z", doubleArrayCollector.getCollectors().get(2)));

            list.add(buildDocument(expectedValue.getLastKey().getProjectId(),
                    expectedValue.getLastKey().getUserId(),
                    expectedValue.getLastKey().getSourceId(), new Date(timestamp), new Date(end),
                    new Document().append(FIELDS, documents)));
        }

        return list;
    }

    private static List<Document> extractQuartile(List<Double> component) {
        return Arrays.asList(
                new Document("25", component.get(0)),
                new Document("50", component.get(1)),
                new Document("75", component.get(2)));
    }

    /**
     * Produces {@link List} of {@link Document}s for given {@link ExpectedValue}.
     *
     * @param expectedValue for test
     * @return {@link List} of {@link Document}s
     */
    public List<Document> produceExpectedData(ExpectedValue expectedValue) {
        Map series = expectedValue.getSeries();
        if (series.isEmpty()) {
            return Collections.emptyList();
        }
        Object firstCollector = series.values().iterator().next();
        if (firstCollector instanceof DoubleArrayCollector) {
            return getDocumentsByArray(expectedValue);
        } else {
            return getDocumentsBySingle(expectedValue);
        }
    }
}
