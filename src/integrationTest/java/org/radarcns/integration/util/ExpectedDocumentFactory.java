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

import static org.radarcns.integration.model.ExpectedValue.DURATION;
import static org.radarcns.integration.model.ExpectedValue.StatType.AVERAGE;
import static org.radarcns.integration.model.ExpectedValue.StatType.COUNT;
import static org.radarcns.integration.model.ExpectedValue.StatType.INTERQUARTILE_RANGE;
import static org.radarcns.integration.model.ExpectedValue.StatType.MAXIMUM;
import static org.radarcns.integration.model.ExpectedValue.StatType.MINIMUM;
import static org.radarcns.integration.model.ExpectedValue.StatType.QUARTILES;
import static org.radarcns.integration.model.ExpectedValue.StatType.SUM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.radarcns.integration.model.ExpectedValue;
import org.radarcns.integration.model.ExpectedValue.StatType;
import org.radarcns.stream.collector.DoubleArrayCollector;
import org.radarcns.stream.collector.DoubleValueCollector;

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
     * @see DoubleValueCollector
     **/
    public List<?> getStatValue(StatType statistic,
            DoubleArrayCollector collectors) {

        switch (statistic) {
            case AVERAGE:
                return collectors.getCollectors().stream()
                        .map(DoubleValueCollector::getAvg).collect(Collectors.toList());
            case COUNT:
                return collectors.getCollectors().stream()
                        .map(DoubleValueCollector::getCount).collect(Collectors.toList());
            case INTERQUARTILE_RANGE:
                return collectors.getCollectors().stream()
                        .map(DoubleValueCollector::getIqr).collect(Collectors.toList());
            case MAXIMUM:
                return collectors.getCollectors().stream()
                        .map(DoubleValueCollector::getMax).collect(Collectors.toList());
            case MEDIAN:
                return collectors.getCollectors().stream()
                        .map(v -> v.getQuartile().get(1)).collect(Collectors.toList());
            case MINIMUM:
                return collectors.getCollectors().stream()
                        .map(DoubleValueCollector::getMin).collect(Collectors.toList());
            case QUARTILES:
                return collectors.getCollectors().stream()
                        .map(DoubleValueCollector::getQuartile).collect(Collectors.toList());
            case SUM:
                return collectors.getCollectors().stream()
                        .map(DoubleValueCollector::getSum).collect(Collectors.toList());
            default:
                throw new IllegalArgumentException(
                        statistic.toString() + " is not supported");
        }
    }

    /**
     * It return the value of the given statistical function.
     *
     * @param statistic function that has to be returned
     * @param collector data aggregator
     * @return the value that has to be stored within a {@code Dataset} {@code Item}
     * @see DoubleValueCollector
     **/
    public Object getStatValue(StatType statistic,
            DoubleValueCollector collector) {

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
                        statistic.toString() + " is not supported");
        }
    }


    private List<Document> getDocumentsBySingle(ExpectedValue expectedValue) {
        LinkedList<Document> list = new LinkedList<>();

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        DoubleValueCollector doubleValueCollector;
        Long end;
        for (Long timestamp : windows) {
            doubleValueCollector = (DoubleValueCollector) expectedValue.getSeries().get(timestamp);

            end = timestamp + DURATION;

            list.add(new Document("_id",
                    expectedValue.getKey().getUserId() + "-" + expectedValue.getKey().getSourceId()
                            + "-" + timestamp + "-" + end)
                    .append("user", expectedValue.getKey().getUserId())
                    .append("source", expectedValue.getKey().getSourceId())
                    .append("min", getStatValue(MINIMUM, doubleValueCollector))
                    .append("max", getStatValue(MAXIMUM, doubleValueCollector))
                    .append("sum", getStatValue(SUM, doubleValueCollector))
                    .append("count", getStatValue(COUNT, doubleValueCollector))
                    .append("avg", getStatValue(AVERAGE, doubleValueCollector))
                    .append("quartile", extractQuartile((List<Double>) getStatValue(
                            QUARTILES, doubleValueCollector)))
                    .append("iqr", getStatValue(INTERQUARTILE_RANGE, doubleValueCollector))
                    .append("start", new Date(timestamp))
                    .append("end", new Date(end)));
        }

        return list;
    }

    private List<Document> getDocumentsByArray(ExpectedValue expectedValue) {
        LinkedList<Document> list = new LinkedList<>();

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        DoubleArrayCollector doubleArrayCollector;
        Long end;
        for (Long timestamp : windows) {
            doubleArrayCollector = (DoubleArrayCollector) expectedValue.getSeries().get(timestamp);

            end = timestamp + DURATION;

            list.add(new Document("_id",
                    expectedValue.getKey().getUserId() + "-" + expectedValue.getKey().getSourceId()
                            + "-" + timestamp + "-" + end)
                    .append("user", expectedValue.getKey().getUserId())
                    .append("source", expectedValue.getKey().getSourceId())
                    .append("min", getStatValue(MINIMUM, doubleArrayCollector))
                    .append("max", getStatValue(MAXIMUM, doubleArrayCollector))
                    .append("sum", getStatValue(SUM, doubleArrayCollector))
                    .append("count", getStatValue(COUNT, doubleArrayCollector))
                    .append("avg", getStatValue(AVERAGE, doubleArrayCollector))
                    .append("quartile",
                            extractAccelerationQuartile((List<List<Double>>) getStatValue(
                                    QUARTILES, doubleArrayCollector)))
                    .append("iqr", getStatValue(INTERQUARTILE_RANGE,
                            doubleArrayCollector))
                    .append("start", new Date(timestamp))
                    .append("end", new Date(end)));
        }

        return list;
    }

    private Document extractAccelerationQuartile(List<List<Double>> statValue) {
        Document quartile = new Document();
        quartile.put("x", extractQuartile(statValue.get(0)));
        quartile.put("y", extractQuartile(statValue.get(1)));
        quartile.put("z", extractQuartile(statValue.get(2)));
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
