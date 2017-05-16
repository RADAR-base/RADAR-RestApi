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

import static org.radarcns.avro.restapi.header.DescriptiveStatistic.QUARTILES;
import static org.radarcns.integration.model.ExpectedValue.DURATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.restapi.data.Acceleration;
import org.radarcns.avro.restapi.data.DoubleSample;
import org.radarcns.avro.restapi.data.Quartiles;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.integration.aggregator.DoubleArrayCollector;
import org.radarcns.integration.aggregator.DoubleValueCollector;
import org.radarcns.integration.model.ExpectedValue;
import org.radarcns.integration.model.ExpectedValue.StatType;
import org.radarcns.source.SourceCatalog;
import org.radarcns.util.RadarConverter;

/**
 * Produces {@link Dataset} and {@link org.bson.Document} for {@link ExpectedValue}
 */
public class ExpectedDataSetFactory extends ExpectedDocumentFactory {

    private static Map<DescriptiveStatistic, StatType> statMap = new HashMap();

    /**
     * Default constructor initializes the mapping between {@link DescriptiveStatistic} and {@link
     * StatType}.
     */
    public ExpectedDataSetFactory() {
        statMap.put(DescriptiveStatistic.AVERAGE, StatType.AVERAGE);
        statMap.put(DescriptiveStatistic.COUNT, StatType.COUNT);
        statMap.put(DescriptiveStatistic.INTERQUARTILE_RANGE, StatType.INTERQUARTILE_RANGE);
        statMap.put(DescriptiveStatistic.MAXIMUM, StatType.MAXIMUM);
        statMap.put(DescriptiveStatistic.MEDIAN, StatType.MEDIAN);
        statMap.put(DescriptiveStatistic.MINIMUM, StatType.MINIMUM);
        statMap.put(QUARTILES, StatType.QUARTILES);
        statMap.put(DescriptiveStatistic.SUM, StatType.SUM);
    }

    /**
     * It computes the {@code Dataset} resulted from the mock data.
     *
     * @param expectedValue mock data used to test
     * @param userId user identifier
     * @param sourceId source identifier
     * @param sourceType source that has to be simulated
     * @param sensorType sensor that has to be simulated
     * @param statistic function that has to be simulated
     * @param timeFrame time interval between two consecutive samples
     * @return {@code Dataset} resulted by the simulation
     * @see {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public Dataset getDataset(ExpectedValue expectedValue, String userId, String sourceId,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic statistic,
            TimeFrame timeFrame) throws InstantiationException, IllegalAccessException {
        return new Dataset(getHeader(expectedValue, userId, sourceId, sourceType, sensorType,
                statistic, timeFrame), getItem(expectedValue, statistic, sensorType));
    }

    /**
     * It generates the {@code Header} for the resulting {@code Dataset}.
     *
     * @param expectedValue mock data used to test
     * @param userId user identifier
     * @param sourceId source identifier
     * @param sourceType source that has to be simulated
     * @param sensorType sensor that has to be simulated
     * @param statistic function that has to be simulated
     * @param timeFrame time interval between two consecutive samples
     * @return {@link org.radarcns.avro.restapi.header.Header} for a {@link
     * org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public Header getHeader(ExpectedValue expectedValue, String userId, String sourceId,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic statistic,
            TimeFrame timeFrame) {
        return new Header(userId, sourceId, sourceType, sensorType, statistic,
                SourceCatalog.getInstance(sourceType).getMeasurementUnit(sensorType), timeFrame,
                getEffectiveTimeFrame(expectedValue));
    }

    /**
     * @return {@code EffectiveTimeFrame} for the simulated inteval.
     * @see {@link org.radarcns.avro.restapi.header.EffectiveTimeFrame}
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(ExpectedValue expectedValue) {
        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        EffectiveTimeFrame eft = new EffectiveTimeFrame(
                RadarConverter.getISO8601(new Date(windows.get(0))),
                RadarConverter.getISO8601(new Date(windows.get(windows.size() - 1)
                        + DURATION)));

        return eft;
    }


    /**
     * @param value timestamp.
     * @return {@code EffectiveTimeFrame} starting on value and ending {@link
     * ExpectedValue#DURATION} milliseconds after.
     * @see {@link org.radarcns.avro.restapi.header.EffectiveTimeFrame}
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(Long value) {
        return new EffectiveTimeFrame(RadarConverter.getISO8601(new Date(value)),
                RadarConverter.getISO8601(new Date(value + DURATION)));
    }


    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}
     *
     * @param statistic function that has to be simulated  @return {@code List<Item>} for a {@link
     * org.radarcns.avro.restapi.dataset.Dataset}
     * @see {@link org.radarcns.avro.restapi.dataset.Item}.
     **/
    public List<Item> getItem(ExpectedValue expectedValue,
            DescriptiveStatistic statistic, SensorType sensorType)
            throws IllegalAccessException, InstantiationException {

        List<Long> keys = new LinkedList<>(expectedValue.getSeries().keySet());
        Collections.sort(keys);

        switch (expectedValue.getExpectedType()) {
            case ARRAY:
                return getArrayItems(expectedValue, keys, statistic, sensorType);
            case DOUBLE:
                return getSingletonItems(expectedValue, keys, statistic, sensorType);
            default:
                throw new IllegalArgumentException(sensorType.name() + " not supported yet");
        }
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}
     *
     * @param keys {@code Collection} of timewindow initial time
     * @param statistic function that has to be simulated
     * @param sensor @return {@code List<Item>} for a
     *      {@link org.radarcns.avro.restapi.dataset.Dataset}
     * @see {@link org.radarcns.avro.restapi.dataset.Item} containg data data that can be
     *      represented as array of {@code Double}.
     **/
    private List<Item> getArrayItems(ExpectedValue expectedValue,
            Collection<Long> keys, DescriptiveStatistic statistic,
            SensorType sensor) {
        List<Item> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleArrayCollector dac = (DoubleArrayCollector) expectedValue.getSeries().get(key);

            switch (sensor) {
                case ACCELEROMETER:
                    Object content;

                    if (statistic.name().equals(QUARTILES.name())) {
                        List<List<Double>> statValues = (List<List<Double>>) getStatValue(
                                statMap.get(statistic),
                                dac);
                        content = new Acceleration(getQuartile(statValues.get(0)),
                                getQuartile(statValues.get(1)), getQuartile(statValues.get(2)));
                    } else {
                        List<Double> statValues = (List<Double>) getStatValue(
                                statMap.get(statistic),
                                dac);
                        content = new Acceleration(statValues.get(0), statValues.get(1),
                                statValues.get(2));
                    }
                    items.add(new Item(content, getEffectiveTimeFrame(key).getStartDateTime()));
                    break;
                default:
                    throw new IllegalArgumentException(sensor.name()
                            + " is not a supported test case");
            }
        }

        return items;
    }

    /**
     * @param list of {@code Double} values representing a quartile.
     * @return the value that has to be stored within a {@code Dataset} {@code Item}
     * @see {@link org.radarcns.avro.restapi.data.Quartiles}.
     **/
    private Quartiles getQuartile(List<Double> list) {
        return new Quartiles(list.get(0), list.get(1), list.get(2));
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}
     *
     * @param keys {@code Collection} of timewindow initial time
     * @param statistic function that has to be simulated
     * @param sensor @return {@code List<Item>} for a
     *      {@link org.radarcns.avro.restapi.dataset.Dataset}
     * @see {@link org.radarcns.avro.restapi.dataset.Item} containg data data that can be
     *      represented as {@code Double}.
     **/
    private List<Item> getSingletonItems(ExpectedValue expectedValue,
            Collection<Long> keys, DescriptiveStatistic statistic,
            SensorType sensor) throws InstantiationException, IllegalAccessException {
        List<Item> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleValueCollector dac = (DoubleValueCollector) expectedValue.getSeries().get(key);

            Object content = getContent(getStatValue(statMap.get(statistic), dac), statistic,
                    getSensorClass(sensor));

            items.add(new Item(content, getEffectiveTimeFrame(key).getStartDateTime()));
        }

        return items;
    }


    private <T extends SpecificRecord> T getContent(Object object, DescriptiveStatistic stat,
            Class<T> sampleClass) throws IllegalAccessException, InstantiationException {
        T content;

        switch (stat) {
            case QUARTILES:
                content = sampleClass.newInstance();
                content.put(content.getSchema().getField("value").pos(),
                        getQuartile((List<Double>) object));
                break;
            default:
                content = sampleClass.newInstance();
                content.put(content.getSchema().getField("value").pos(), object);
                break;
        }

        return content;
    }

    private Class getSensorClass(SensorType sensor) {
        switch (sensor) {
            case ACCELEROMETER:
                return Acceleration.class;
            case BATTERY:
                return DoubleSample.class;
            case BLOOD_VOLUME_PULSE:
                return DoubleSample.class;
            case ELECTRODERMAL_ACTIVITY:
                return DoubleSample.class;
            case INTER_BEAT_INTERVAL:
                return DoubleSample.class;
            case HEART_RATE:
                return DoubleSample.class;
            case THERMOMETER:
                return DoubleSample.class;
            default:
                throw new IllegalArgumentException(sensor.name()
                        + " is not a supported test case");
        }
    }
}
