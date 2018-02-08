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
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.INTERQUARTILE_RANGE;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MAXIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.MINIMUM;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.SUM;
import static org.radarcns.mock.model.ExpectedValue.DURATION;

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
import org.radarcns.mongo.util.MongoHelper;
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

            list.add(new Document(MongoHelper.ID,
                    expectedValue.getLastKey().getUserId()
                            + "-" + expectedValue.getLastKey().getSourceId()
                            + "-" + timestamp + "-" + end)
                    .append(MongoHelper.USER_ID, expectedValue.getLastKey().getUserId())
                    .append(MongoHelper.SOURCE_ID, expectedValue.getLastKey().getSourceId())
                    .append(Stat.min.getParam(), getStatValue(MINIMUM, doubleValueCollector))
                    .append(Stat.max.getParam(), getStatValue(MAXIMUM, doubleValueCollector))
                    .append(Stat.sum.getParam(), getStatValue(SUM, doubleValueCollector))
                    .append(Stat.count.getParam(), getStatValue(COUNT, doubleValueCollector))
                    .append(Stat.avg.getParam(), getStatValue(AVERAGE, doubleValueCollector))
                    .append(Stat.quartile.getParam(), extractQuartile((List<Double>) getStatValue(
                            QUARTILES, doubleValueCollector)))
                    .append(Stat.iqr.getParam(), getStatValue(INTERQUARTILE_RANGE,
                            doubleValueCollector))
                    .append(MongoHelper.START, new Date(timestamp))
                    .append(MongoHelper.END, new Date(end)));
        }

        return list;
    }

    private List<Document> getDocumentsByArray(ExpectedValue<?> expectedValue) {

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        List<Document> list = new ArrayList<>(windows.size());

        for (Long timestamp : windows) {
            DoubleArrayCollector doubleArrayCollector = (DoubleArrayCollector) expectedValue
                    .getSeries().get(timestamp);

            long end = timestamp + DURATION;

            list.add(new Document(MongoHelper.ID,
                    expectedValue.getLastKey().getUserId()
                            + "-" + expectedValue.getLastKey().getUserId()
                            + "-" + timestamp + "-" + end)
                    .append(MongoHelper.USER_ID, expectedValue.getLastKey().getUserId())
                    .append(MongoHelper.SOURCE_ID, expectedValue.getLastKey().getSourceId())
                    .append(Stat.min.getParam(), getStatValue(MINIMUM, doubleArrayCollector))
                    .append(Stat.max.getParam(), getStatValue(MAXIMUM, doubleArrayCollector))
                    .append(Stat.sum.getParam(), getStatValue(SUM, doubleArrayCollector))
                    .append(Stat.count.getParam(), getStatValue(COUNT, doubleArrayCollector))
                    .append(Stat.avg.getParam(), getStatValue(AVERAGE, doubleArrayCollector))
                    .append(Stat.quartile.getParam(),
                            extractAccelerationQuartile((List<List<Double>>) getStatValue(
                                    QUARTILES, doubleArrayCollector)))
                    .append(Stat.iqr.getParam(), getStatValue(INTERQUARTILE_RANGE,
                            doubleArrayCollector))
                    .append(MongoHelper.START, new Date(timestamp))
                    .append(MongoHelper.END, new Date(end)));
        }

        return list;
    }

    private Document extractAccelerationQuartile(List<List<Double>> statValue) {
        Document quartile = new Document();
        quartile.put(AccelerationFormat.X_LABEL, extractQuartile(statValue.get(0)));
        quartile.put(AccelerationFormat.Y_LABEL, extractQuartile(statValue.get(1)));
        quartile.put(AccelerationFormat.Z_LABEL, extractQuartile(statValue.get(2)));
        return quartile;
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
