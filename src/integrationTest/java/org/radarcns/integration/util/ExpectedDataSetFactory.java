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

import static org.radarcns.mock.model.ExpectedValue.DURATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.format.Acceleration;
import org.radarcns.domain.restapi.format.Quartiles;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.domain.restapi.header.EffectiveTimeFrame;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.mock.model.ExpectedValue;
import org.radarcns.stream.collector.DoubleArrayCollector;
import org.radarcns.stream.collector.DoubleValueCollector;
import org.radarcns.util.RadarConverter;

/**
 * Produces {@link Dataset} and {@link org.bson.Document} for {@link ExpectedValue}.
 */
public class ExpectedDataSetFactory extends ExpectedDocumentFactory {

    /**
     * It computes the {@code Dataset} resulted from the mock data.
     *
     * @param expectedValue mock data used to test
     * @param subjectId subject identifier
     * @param sourceId source identifier
     * @param sourceType sourceType that has to be simulated
     * @param sensorType sensor that has to be simulated
     * @param statistic function that has to be simulated
     * @param timeWindow time interval between two consecutive samples
     * @return {@code Dataset} resulted by the simulation
     * @see Dataset
     */
    public Dataset getDataset(ExpectedValue expectedValue, String projectName, String subjectId,
            String sourceId, String sourceType, String sensorType, DescriptiveStatistic statistic,
            TimeWindow timeWindow) throws InstantiationException, IllegalAccessException {

        Header header = getHeader(expectedValue, projectName, subjectId, sourceId, sourceType,
                sensorType, statistic, timeWindow);

        return new Dataset(header, getItem(expectedValue, header));
    }

    /**
     * It generates the {@code Header} for the resulting {@code Dataset}.
     *
     * @param expectedValue mock data used to test
     * @param subjectId subject identifier
     * @param sourceId source identifier
     * @param sourceType sourceType that has to be simulated
     * @param sensorType sensor that has to be simulated
     * @param statistic function that has to be simulated
     * @param timeWindow time interval between two consecutive samples
     * @return {@link Header} for a {@link Dataset}
     */
    public Header getHeader(ExpectedValue expectedValue, String projectName, String subjectId,
            String sourceId, String sourceType, String sensorType, DescriptiveStatistic statistic,
            TimeWindow timeWindow) {
        return new Header(projectName, subjectId, sourceId, sourceType, sensorType, statistic,
                null, timeWindow,
                getEffectiveTimeFrame(expectedValue));
    }

    /**
     * Get the effective interval for a value.
     *
     * @return {@code EffectiveTimeFrame} for the simulated inteval.
     * @see EffectiveTimeFrame
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(ExpectedValue<?> expectedValue) {
        List<Long> windows = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(windows);

        return new EffectiveTimeFrame(
                RadarConverter.getISO8601(new Date(windows.get(0))),
                RadarConverter.getISO8601(new Date(windows.get(windows.size() - 1)
                        + DURATION)));
    }


    /**
     * Get the effective time frame for a single time stamp.
     *
     * @param value timestamp.
     * @return {@code EffectiveTimeFrame} starting on value and ending {@link
     * ExpectedValue#DURATION} milliseconds after.
     * @see EffectiveTimeFrame
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(Long value) {
        return new EffectiveTimeFrame(RadarConverter.getISO8601(new Date(value)),
                RadarConverter.getISO8601(new Date(value + DURATION)));
    }


    /**
     * It generates the {@code List<Item>} for the resulting {@link Dataset}.
     *
     * @param header {@link Header} used to provide data context
     * @return {@code List<Item>} for a {@link Dataset}
     * @see DataItem
     **/
    public List<DataItem> getItem(ExpectedValue<?> expectedValue, Header header)
            throws IllegalAccessException, InstantiationException {

        if (expectedValue.getSeries().isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> keys = new ArrayList<>(expectedValue.getSeries().keySet());
        Collections.sort(keys);
        Object singleExpectedValue = expectedValue.getSeries().get(keys.get(0));

        if (singleExpectedValue instanceof DoubleArrayCollector) {
            return getArrayItems(expectedValue, keys, header.getDescriptiveStatistic(),
                    header.getSourceDataType());
        } else if (singleExpectedValue instanceof DoubleValueCollector) {
            return getSingletonItems(expectedValue, keys, header.getDescriptiveStatistic());
        } else {
            throw new IllegalArgumentException(header.getSourceDataType() + " not supported yet");
        }
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}.
     *
     * @param keys {@code Collection} of timewindow initial time
     * @param statistic function that has to be simulated
     * @param sensor @return {@code List<Item>} for a dataset
     */
    private List<DataItem> getArrayItems(ExpectedValue expectedValue,
            Collection<Long> keys, DescriptiveStatistic statistic,
            String sensor) {
        List<DataItem> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleArrayCollector dac = (DoubleArrayCollector) expectedValue.getSeries().get(key);

            switch (sensor) {
                case "ACCELEROMETER":
                    Object content;

                    if (statistic.name().equals(DescriptiveStatistic.QUARTILES.name())) {
                        List<List<Double>> statValues = (List<List<Double>>) getStatValue(
                                statistic, dac);
                        content = new Acceleration(getQuartile(statValues.get(0)),
                                getQuartile(statValues.get(1)), getQuartile(statValues.get(2)));
                    } else {
                        List<Double> statValues = (List<Double>) getStatValue(statistic, dac);
                        content = new Acceleration(statValues.get(0), statValues.get(1),
                                statValues.get(2));
                    }
                    items.add(new DataItem(content, getEffectiveTimeFrame(key).getStartDateTime()));
                    break;
                default:
                    throw new IllegalArgumentException(sensor + " is not a supported test case");
            }
        }

        return items;
    }

    /**
     * Quartile object from a list of three doubles.
     *
     * @param list of {@code Double} values representing a quartile.
     * @return the value that has to be stored within a {@code Dataset} {@code Item}
     * @see Quartiles
     **/
    private Quartiles getQuartile(List<Double> list) {
        return new Quartiles(list.get(0), list.get(1), list.get(2));
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}.
     *
     * @param keys {@code Collection} of timewindow initial time
     * @param statistic function that has to be simulated
     * @return {@code List<Item>} for a data set represented as {@code Double}.
     **/
    private List<DataItem> getSingletonItems(ExpectedValue expectedValue,
            Collection<Long> keys, DescriptiveStatistic statistic)
            throws InstantiationException, IllegalAccessException {
        List<DataItem> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleValueCollector dac = (DoubleValueCollector) expectedValue.getSeries().get(key);

            Object content = getContent(getStatValue(statistic, dac), statistic);

            items.add(new DataItem(content, getEffectiveTimeFrame(key).getStartDateTime()));
        }

        return items;
    }


    private <T> T getContent(Object object, DescriptiveStatistic stat)
            throws IllegalAccessException, InstantiationException {
        T content;

        switch (stat) {
            case QUARTILES:
                content = (T) getQuartile((List<Double>) object);
                break;
            default:
                content = (T) object;
                break;
        }

        return content;
    }
}
