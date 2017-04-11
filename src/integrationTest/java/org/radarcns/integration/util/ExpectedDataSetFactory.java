package org.radarcns.integration.util;

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
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.sensor.Acceleration;
import org.radarcns.avro.restapi.sensor.Battery;
import org.radarcns.avro.restapi.sensor.BloodVolumePulse;
import org.radarcns.avro.restapi.sensor.ElectroDermalActivity;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.avro.restapi.sensor.InterBeatInterval;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.sensor.Temperature;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.integration.model.ExpectedValue;
import org.radarcns.integration.model.ExpectedValue.StatType;
import org.radarcns.source.SourceCatalog;
import org.radarcns.stream.aggregator.DoubleArrayCollector;
import org.radarcns.stream.aggregator.DoubleValueCollector;
import org.radarcns.util.RadarConverter;

/**
 * Produces {@link Dataset} and {@link org.bson.Document} for {@link ExpectedValue}
 */
public class ExpectedDataSetFactory extends ExpectedDocumentFactory {

    private static Map<DescriptiveStatistic, StatType> statMap = new HashMap();

    /**
     * Default constructor initializes the mapping between {@link DescriptiveStatistic} and {@link
     * StatType}
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
     * @param statistic function that has to be simulated
     * @param source the simulated source device
     * @param sensor the simulated sensor of the source device
     * @return {@code Dataset} resulted by the simulation
     * @see {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public Dataset getDataset(ExpectedValue expectedValue, DescriptiveStatistic statistic,
            SourceType source,
            SensorType sensor) throws InstantiationException, IllegalAccessException {
        return new Dataset(getHeader(expectedValue, statistic,
                SourceCatalog.getInstance(source).getMeasurementUnit(sensor)),
                getItem(expectedValue, statistic, sensor));
    }

    /**
     * It generates the {@code Header} for the resulting {@code Dataset}.
     *
     * @param statistic function that has to be simulated
     * @param unit values unit
     * @return {@link org.radarcns.avro.restapi.header.Header} for a {@link
     * org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public Header getHeader(ExpectedValue expectedValue,
            DescriptiveStatistic statistic, Unit unit) {
        return new Header(statistic, unit, getEffectiveTimeFrame(expectedValue));
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
     * @param sensor @return {@code List<Item>} for a {@link org.radarcns.avro.restapi.dataset.Dataset}
     * @see {@link org.radarcns.avro.restapi.dataset.Item} containg sensor data that can be
     * represented as array of {@code Double}.
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
                                dac.getCollectors());
                        content = new Acceleration(getQuartile(statValues.get(0)),
                                getQuartile(statValues.get(1)), getQuartile(statValues.get(2)));
                    } else {
                        List<Double> statValues = (List<Double>) getStatValue(
                                statMap.get(statistic),
                                dac.getCollectors());
                        content = new Acceleration(statValues.get(0), statValues.get(1),
                                statValues.get(2));
                    }
                    items.add(new Item(content, getEffectiveTimeFrame(key)));
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
     * @see {@link org.radarcns.avro.restapi.dataset.Quartiles}.
     **/
    private Quartiles getQuartile(List<Double> list) {
        return new Quartiles(list.get(0), list.get(1), list.get(2));
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}
     *
     * @param keys {@code Collection} of timewindow initial time
     * @param statistic function that has to be simulated
     * @param sensor @return {@code List<Item>} for a {@link org.radarcns.avro.restapi.dataset.Dataset}
     * @see {@link org.radarcns.avro.restapi.dataset.Item} containg sensor data that can be
     * represented as {@code Double}.
     **/
    private List<Item> getSingletonItems(ExpectedValue expectedValue,
            Collection<Long> keys, DescriptiveStatistic statistic,
            SensorType sensor) throws InstantiationException, IllegalAccessException {
        List<Item> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleValueCollector dac = (DoubleValueCollector) expectedValue.getSeries().get(key);

            Object content = getContent(getStatValue(statMap.get(statistic), dac), statistic,
                    getSensorClass(sensor));

            items.add(new Item(content, getEffectiveTimeFrame(key)));
        }

        return items;
    }


    private <T extends SpecificRecord> T getContent(Object object, DescriptiveStatistic stat,
            Class<T> tClass) throws IllegalAccessException, InstantiationException {
        T content;

        switch (stat) {
            case QUARTILES:
                content = tClass.newInstance();
                content.put(content.getSchema().getField("value").pos(),
                        getQuartile((List<Double>) object));
                break;
            default:
                content = tClass.newInstance();
                content.put(content.getSchema().getField("value").pos(), object);
        }

        return content;
    }

    private Class getSensorClass(SensorType sensor) {
        switch (sensor) {
            case ACCELEROMETER:
                return Acceleration.class;
            case BATTERY:
                return Battery.class;
            case BLOOD_VOLUME_PULSE:
                return BloodVolumePulse.class;
            case ELECTRODERMAL_ACTIVITY:
                return ElectroDermalActivity.class;
            case INTER_BEAT_INTERVAL:
                return InterBeatInterval.class;
            case HEART_RATE:
                return HeartRate.class;
            case THERMOMETER:
                return Temperature.class;
            default:
                throw new IllegalArgumentException(sensor.name()
                        + " is not a supported test case");
        }
    }
}
