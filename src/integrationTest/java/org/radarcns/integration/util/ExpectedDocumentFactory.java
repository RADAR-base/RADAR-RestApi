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
import static org.radarcns.integration.model.ExpectedValue.StatType.MEDIAN;
import static org.radarcns.integration.model.ExpectedValue.StatType.MINIMUM;
import static org.radarcns.integration.model.ExpectedValue.StatType.QUARTILES;
import static org.radarcns.integration.model.ExpectedValue.StatType.SUM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.bson.Document;
import org.radarcns.dao.mongo.data.sensor.AccelerationFormat;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.integration.aggregator.DoubleArrayCollector;
import org.radarcns.integration.aggregator.DoubleValueCollector;
import org.radarcns.integration.model.ExpectedValue;
import org.radarcns.integration.model.ExpectedValue.StatType;

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
     * @see {@link DoubleValueCollector}
     **/
    public List<? extends Object> getStatValue(StatType statistic,
            DoubleArrayCollector collectors) {

        int len = collectors.getCollectors().length;

        List<Double> avgList = new ArrayList<>(len);
        List<Double> countList = new ArrayList<>(len);
        List<Double> iqrList = new ArrayList<>(len);
        List<Double> maxList = new ArrayList<>(len);
        List<Double> medList = new ArrayList<>(len);
        List<Double> minList = new ArrayList<>(len);
        List<Double> sumList = new ArrayList<>(len);
        List<List<Double>> quartileList = new ArrayList<>(len);

        for (DoubleValueCollector collector : collectors.getCollectors()) {
            minList.add((Double) getStatValue(MINIMUM, collector));
            maxList.add((Double) getStatValue(MAXIMUM, collector));
            sumList.add((Double) getStatValue(SUM, collector));
            countList.add((Double) getStatValue(COUNT, collector));
            avgList.add((Double) getStatValue(AVERAGE, collector));
            iqrList.add((Double) getStatValue(INTERQUARTILE_RANGE, collector));
            quartileList.add((List<Double>) getStatValue(QUARTILES, collector));
            medList.add((Double) getStatValue(MEDIAN, collector));
        }

        switch (statistic) {
            case AVERAGE:
                return avgList;
            case COUNT:
                return countList;
            case INTERQUARTILE_RANGE:
                return iqrList;
            case MAXIMUM:
                return maxList;
            case MEDIAN:
                return medList;
            case MINIMUM:
                return minList;
            case QUARTILES:
                return quartileList;
            case SUM:
                return sumList;
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
     * @see {@link .DoubleValueCollector}
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
                return collector.getQuartile()[1];
            case MINIMUM:
                return collector.getMin();
            case QUARTILES:
                List<Double> temp = new ArrayList<>();
                for (int i = 0; i < collector.getQuartile().length; i++) {
                    temp.add(collector.getQuartile()[i]);
                }
                return temp;
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

            list.add(new Document(MongoHelper.ID,
                    expectedValue.getUser() + "-" + expectedValue.getSource() + "-" + timestamp
                            + "-" + end)
                    .append(MongoHelper.USER, expectedValue.getUser())
                    .append(MongoHelper.SOURCE, expectedValue.getSource())
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

    private List<Document> getDocumentsByArray(ExpectedValue expectedValue) {
        LinkedList<Document> list = new LinkedList<>();

        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        DoubleArrayCollector doubleArrayCollector;
        Long end;
        for (Long timestamp : windows) {
            doubleArrayCollector = (DoubleArrayCollector) expectedValue.getSeries().get(timestamp);

            end = timestamp + DURATION;

            list.add(new Document(MongoHelper.ID,
                    expectedValue.getUser() + "-" + expectedValue.getSource() + "-" + timestamp
                            + "-" + end)
                    .append(MongoHelper.USER, expectedValue.getUser())
                    .append(MongoHelper.SOURCE, expectedValue.getSource())
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
        return Arrays.asList(new Document[]{
                new Document("25", component.get(0)),
                new Document("50", component.get(1)),
                new Document("75", component.get(2))
        });
    }

    /**
     * Produces {@link List} of {@link Document}s for given {@link ExpectedValue}.
     * @param expectedValue for test
     * @return {@link List} of {@link Document}s
     */
    public List<Document> produceExpectedData(ExpectedValue expectedValue) {
        switch (expectedValue.getExpectedType()) {
            case ARRAY:
                return getDocumentsByArray(expectedValue);
            default:
                return getDocumentsBySingle(expectedValue);
        }
    }
}
