package org.radarcns.integrationtest.collector;

import static org.radarcns.avro.restapi.header.DescriptiveStatistic.average;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.count;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.interquartile_range;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.maximum;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.median;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.minimum;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.quartiles;
import static org.radarcns.avro.restapi.header.DescriptiveStatistic.sum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.dataset.Quartiles;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.avro.restapi.sensor.Acceleration;
import org.radarcns.avro.restapi.sensor.Battery;
import org.radarcns.avro.restapi.sensor.BloodVolumePulse;
import org.radarcns.avro.restapi.sensor.ElectroDermalActivity;
import org.radarcns.avro.restapi.sensor.HeartRate;
import org.radarcns.avro.restapi.sensor.InterBeatInterval;
import org.radarcns.avro.restapi.sensor.Temperature;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.radarcns.integrationtest.util.Parser;
import org.radarcns.integrationtest.util.Parser.ExpectedType;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It computes the expected value for a test case.
 */
public abstract class ExpectedValue<V> {

    private static final Logger logger = LoggerFactory.getLogger(ExpectedValue.class);

    //Timewindow length in milliseconds
    @SuppressWarnings({"checkstyle:AbbreviationAsWordInName","checkstyle:MemberName"})
    protected long DURATION = TimeUnit.SECONDS.toMillis(10);

    protected String user;
    protected String source;

    protected Long lastTimestamp;
    protected V lastValue;
    protected HashMap<Long, V> series;

    /**
     * @return {@code EffectiveTimeFrame} for the simulated inteval.
     * @see {@link org.radarcns.avro.restapi.header.EffectiveTimeFrame}
     */
    public EffectiveTimeFrame getEffectiveTimeFrame() {
        List<Long> windows = new ArrayList<>(series.keySet());
        Collections.sort(windows);

        EffectiveTimeFrame eft = new EffectiveTimeFrame(
                RadarConverter.getISO8601(new Date(windows.get(0))),
                RadarConverter.getISO8601(new Date(windows.get(windows.size() - 1)
                    + DURATION)));

        return eft;
    }

    /**
     * @param value timestamp.
     * @return {@code EffectiveTimeFrame} starting on value and ending
     *      {@link ExpectedValue#DURATION} milliseconds after.
     * @see {@link org.radarcns.avro.restapi.header.EffectiveTimeFrame}
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(Long value) {
        return new EffectiveTimeFrame(RadarConverter.getISO8601(new Date(value)),
            RadarConverter.getISO8601(new Date(value + DURATION)));
    }

    /**
     * It generates the {@code Header} for the resulting {@code Dataset}.
     * @param statistic function that has to be simulated
     * @param unit values unit
     * @return {@link org.radarcns.avro.restapi.header.Header} for a
     *      {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public Header getHeader(DescriptiveStatistic statistic, Unit unit) {
        return new Header(statistic, unit, getEffectiveTimeFrame());
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}
     *      @see {@link org.radarcns.avro.restapi.dataset.Item}.
     * @param statistic function that has to be simulated
     * @param config useful to understand what {@code ExpectedType} has to be taken into account
     *      @see {@link Parser.ExpectedType}.
     * @return {@code List<Item>} for a {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public List<Item> getItem(DescriptiveStatistic statistic, MockDataConfig config) {

        List<Long> keys = new LinkedList<>(series.keySet());
        Collections.sort(keys);

        ExpectedType type = Parser.getExpectedType(config);
        switch (type) {
            case ARRAY: return getArrayItems(keys, statistic, config);
            case DOUBLE: return getSingletonItems(keys, statistic, config);
            default:
                throw new IllegalArgumentException("Cannot type " + type.getValue());
        }
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}
     *      @see {@link org.radarcns.avro.restapi.dataset.Item} containg sensor data that can be
     *      represented as array of {@code Double}.
     * @param keys {@code Collection} of timewindow initial time
     * @param statistic function that has to be simulated
     * @param config useful to understand what sensor we are simulating.
     * @return {@code List<Item>} for a {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    private List<Item> getArrayItems(Collection<Long> keys, DescriptiveStatistic statistic,
            MockDataConfig config) {
        List<Item> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleArrayCollector dac = (DoubleArrayCollector) series.get(key);

            if (config.getRestCall().contains("/Acc/")) {
                Object content;

                if (statistic.name().equals(quartiles.name())) {
                    List<List<Double>> statValues = (List<List<Double>>) getStat(statistic,
                            dac.getCollectors());
                    content = new Acceleration(getQuartile(statValues.get(0)),
                        getQuartile(statValues.get(1)), getQuartile(statValues.get(2)));
                } else {
                    List<Double> statValues = (List<Double>) getStat(statistic,
                            dac.getCollectors());
                    content = new Acceleration(statValues.get(0), statValues.get(1),
                        statValues.get(2));
                }
                items.add(new Item(content, getEffectiveTimeFrame(key)));
            } else {
                throw new IllegalArgumentException(config.getRestCall()
                    + " is not a supported test case");
            }
        }

        return items;
    }

    /**
     * It generates the {@code List<Item>} for the resulting {@code Dataset}
     *      @see {@link org.radarcns.avro.restapi.dataset.Item} containg sensor data that can be
     *      represented as {@code Double}.
     * @param keys {@code Collection} of timewindow initial time
     * @param statistic function that has to be simulated
     * @param config useful to understand what sensor we are simulating.
     * @return {@code List<Item>} for a {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    private List<Item> getSingletonItems(Collection<Long> keys, DescriptiveStatistic statistic,
            MockDataConfig config) {
        List<Item> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleValueCollector dac = (DoubleValueCollector) series.get(key);
            Object content = getStat(statistic, dac);

            if (config.getRestCall().contains("/B/")) {
                if (statistic.name().equals(quartiles.name())) {
                    content = new Battery(getQuartile((List<Double>) content));
                } else {
                    content = new Battery(content);
                }
            } else if (config.getRestCall().contains("/BVP/")) {
                if (statistic.name().equals(quartiles.name())) {
                    content = new BloodVolumePulse(getQuartile((List<Double>) content));
                } else {
                    content = new BloodVolumePulse(content);
                }
            } else if (config.getRestCall().contains("/EDA/")) {
                if (statistic.name().equals(quartiles.name())) {
                    content = new ElectroDermalActivity(getQuartile((List<Double>) content));
                } else {
                    content = new ElectroDermalActivity(content);
                }
            } else if (config.getRestCall().contains("/HR/")) {
                if (statistic.name().equals(quartiles.name())) {
                    content = new HeartRate(getQuartile((List<Double>) content));
                } else {
                    content = new HeartRate(content);
                }
            } else if (config.getRestCall().contains("/IBI/")) {
                if (statistic.name().equals(quartiles.name())) {
                    content = new InterBeatInterval(getQuartile((List<Double>) content));
                } else {
                    content = new InterBeatInterval(content);
                }
            } else if (config.getRestCall().contains("/T/")) {
                if (statistic.name().equals(quartiles.name())) {
                    content = new Temperature(getQuartile((List<Double>) content));
                } else {
                    content = new Temperature(content);
                }
            } else {
                throw new IllegalArgumentException(config.getRestCall()
                    + " is not a supported test case");
            }

            items.add(new Item(content, getEffectiveTimeFrame(key)));
        }


        return items;
    }

    /**
     * It return the value of the given statistical function.
     * @param statistic function that has to be returned
     * @param collectors array of aggregated data
     *      @see {@link org.radarcns.integrationtest.collector.DoubleValueCollector}
     * @return the set of values that has to be stored within a {@code Dataset} {@code Item}
     *      @see {@link org.radarcns.avro.restapi.dataset.Item}.
     **/
    private List<? extends Object> getStat(DescriptiveStatistic statistic,
                DoubleValueCollector[] collectors) {
        int len = collectors.length;

        List<Double> avgList = new ArrayList<>(len);
        List<Double> countList = new ArrayList<>(len);
        List<Double> iqrList = new ArrayList<>(len);
        List<Double> maxList = new ArrayList<>(len);
        List<Double> medList = new ArrayList<>(len);
        List<Double> minList = new ArrayList<>(len);
        List<Double> sumList = new ArrayList<>(len);
        List<List<Double>> quartileList = new ArrayList<>(len);

        for (DoubleValueCollector collector : collectors) {
            minList.add((Double) getStat(minimum, collector));
            maxList.add((Double) getStat(maximum, collector));
            sumList.add((Double) getStat(sum, collector));
            countList.add((Double) getStat(count, collector));
            avgList.add((Double) getStat(average, collector));
            iqrList.add((Double) getStat(interquartile_range, collector));
            quartileList.add((List<Double>) getStat(quartiles, collector));
            medList.add((Double) getStat(median, collector));
        }

        switch (statistic) {
            case average: return avgList;
            case count: return countList;
            case interquartile_range: return iqrList;
            case maximum: return maxList;
            case median: return medList;
            case minimum: return minList;
            case quartiles: return quartileList;
            case sum: return sumList;
            default: throw new IllegalArgumentException(
                statistic.toString() + " is not supported");
        }
    }

    /**
     * It return the value of the given statistical function.
     * @param statistic function that has to be returned
     * @param collector data aggregator
     *      @see {@link org.radarcns.integrationtest.collector.DoubleValueCollector}
     * @return the value that has to be stored within a {@code Dataset} {@code Item}
     *      @see {@link org.radarcns.avro.restapi.dataset.Item}.
     **/
    private Object getStat(DescriptiveStatistic statistic,
            DoubleValueCollector collector) {

        switch (statistic) {
            case average: return collector.getAvg();
            case count: return collector.getCount();
            case interquartile_range: return collector.getIqr();
            case maximum: return collector.getMax();
            case median: return collector.getQuartile().get(1);
            case minimum: return collector.getMin();
            case quartiles: return collector.getQuartile();
            case sum: return collector.getSum();
            default: throw new IllegalArgumentException(
                statistic.toString() + " is not supported");
        }
    }

    /**
     * @param list of {@code Double} values representing a quartile.
     * @return the value that has to be stored within a {@code Dataset} {@code Item}
     *      @see {@link org.radarcns.avro.restapi.dataset.Quartiles}.
     **/
    private Quartiles getQuartile(List<Double> list) {
        return new Quartiles(list.get(0), list.get(1), list.get(2));
    }

    /**
     * It computes the {@code Dataset} resulted from the mock data.
     * @param statistic function that has to be simulated
     * @param config useful to understand which sensor we are simulating
     * @return {@code Dataset} resulted by the simulation
     *      @see {@link org.radarcns.avro.restapi.dataset.Dataset}
     **/
    public Dataset getDataset(DescriptiveStatistic statistic, MockDataConfig config) {
        Unit unit;

        if (config.getRestCall().contains("/Acc/")) {
            unit = Unit.g;
        } else if (config.getRestCall().contains("/B/")) {
            unit = Unit.percentage;
        } else if (config.getRestCall().contains("/BVP/")) {
            unit = Unit.nW;
        } else if (config.getRestCall().contains("/EDA/")) {
            unit = Unit.microsiemens;
        } else if (config.getRestCall().contains("/HR/")) {
            unit = Unit.hz;
        } else if (config.getRestCall().contains("/IBI/")) {
            unit = Unit.sec;
        } else if (config.getRestCall().contains("/T/")) {
            unit = Unit.celsius;
        } else {
            throw new IllegalArgumentException(config.getRestCall()
                + " is not a supported test case");
        }

        return new Dataset(getHeader(statistic, unit), getItem(statistic, config));
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        String result = "";
        for (Long interval : series.keySet()) {
            result += interval.toString() + "-" + (interval + DURATION) + '\t'
                + series.get(interval).toString() + "\n";
        }
        return result;
    }

    /**
     * It checks if two {@code Dataset} are equals except for threshold expressed in percentage.
     * @param expected first component that to has to be compared
     * @param test second component that to has to be compared
     * @param tolerance {@code Double} value are subjected to approximations during computations,
     *      for these reason the expected value and the real one may differ. This difference in
     *      percentage has to be smaller than this threshold.
     * @param verbose {@code true} if log messages have to be printed out
     * @return {@code true} if datasets match, false otherwise
     **/
    public static boolean compareDatasets(Object expected, Object test, double tolerance,
            boolean verbose) {
        if (expected == null || test == null) {
            throw new IllegalArgumentException("Null values cannot be compared");
        }

        if (!(expected instanceof Dataset) || !(test instanceof Dataset)) {
            if (verbose) {
                logger.info("Inputs are not instance of {}", Dataset.class.getCanonicalName());
            }
            return false;
        }

        Dataset dataA = (Dataset) expected;
        Dataset dataB = (Dataset) test;

        Header headerA = dataA.getHeader();
        Header headerB = dataB.getHeader();

        if (!headerA.getDescriptiveStatistic().name().equals(
                headerB.getDescriptiveStatistic().name()) ) {
            if (verbose) {
                logger.info("Different DescriptiveStatistic. Expected: {} - Find: {}",
                        headerA.getDescriptiveStatistic().name(),
                        headerB.getDescriptiveStatistic().name());
            }
            return false;
        }

        if (!headerA.getUnit().name().equals(headerB.getUnit().name())) {
            if (verbose) {
                logger.info("Different Unit. Expected: {} - Find: {}",
                        headerA.getUnit().name(), headerB.getUnit().name());
            }
            return false;
        }

        if (!headerA.getDescriptiveStatistic().name().equals(
                headerB.getDescriptiveStatistic().name())) {
            if (verbose) {
                logger.info("Different DescriptiveStatistic. Expected: {} - Find: {}",
                        headerA.getDescriptiveStatistic().name(),
                        headerB.getDescriptiveStatistic().name());
            }
            return false;
        }

        if (!compareEffectiveTimeFrame(headerA.getEffectiveTimeFrame(),
                headerB.getEffectiveTimeFrame())) {
            if (verbose) {
                logger.info("Different EffectiveTimeFrame. Expected: {} - Find: {}",
                        headerA.getEffectiveTimeFrame().toString(),
                        headerB.getEffectiveTimeFrame().toString());
            }
            return false;
        }

        return compareItems(dataA.getDataset(), dataB.getDataset(), headerA, tolerance,
            verbose);
    }

    /**
     * It checks if two {@code List<Item>} are equals except for threshold expressed in percentage.
     * @param listA first component that to has to be compared
     * @param listB second component that to has to be compared
     * @param tolerance {@code Double} value are subjected to approximations during computations,
     *      for these reason the expected value and the real one may differ. This difference in
     *      percentage has to be smaller than this threshold.
     * @param verbose {@code true} if log messages have to be printed out
     * @return {@code true} if datasets match, false otherwise
     **/
    private static boolean compareItems(List<Item> listA, List<Item> listB, Header header,
            double tolerance, boolean verbose) {
        if (listA.size() != listB.size()) {
            if (verbose) {
                logger.info("Dataset with different size. Expected: {} - Find: {}", listA.size(),
                        listB.size());

                for (Item item : listA) {
                    logger.info(item.toString());
                }
                logger.info("--------------------------------------");
                for (Item item : listB) {
                    logger.info(item.toString());
                }
            }

            return false;
        }

        for (int i = 0; i < listA.size(); i++) {
            Item itemA = listA.get(i);
            Item itemB = listB.get(i);

            if (!compareEffectiveTimeFrame(itemA.getEffectiveTimeFrame(),
                    itemB.getEffectiveTimeFrame())) {
                if (verbose) {
                    logger.info("Different EffectiveTimeFrame. Expected: {} - Find: {}",
                            itemA.getEffectiveTimeFrame().toString(),
                            itemB.getEffectiveTimeFrame().toString());
                }
                return false;
            }

            SpecificRecord valueA = (SpecificRecord)itemA.getValue();
            SpecificRecord valueB = (SpecificRecord)itemB.getValue();
            if (valueA.getSchema().getName().equals("Acceleration")
                    && valueB.getSchema().getName().equals("Acceleration")) {
                Acceleration accA = (Acceleration) valueA;
                Acceleration accB = (Acceleration) valueB;
                if (header.getDescriptiveStatistic().name().equals(
                        DescriptiveStatistic.quartiles.name())) {
                    if (!( compareQuartiles((Quartiles) accA.getX(), (Quartiles) accB.getX(),
                            tolerance) && compareQuartiles((Quartiles) accA.getY(),
                            (Quartiles) accB.getY(), tolerance) && compareQuartiles(
                            (Quartiles) accA.getZ(), (Quartiles) accB.getZ(), tolerance))) {
                        if (verbose) {
                            logger.info("Different Quartiles. Expected: {} - Find: {}",
                                    accA.toString(), accB.toString());
                        }
                        return false;
                    }
                } else {
                    if (!( accA.getX().equals(accB.getX()) && accA.getY().equals(accB.getY())
                            && accA.getZ().equals(accB.getZ()))) {

                        boolean toleranceA = isPassable((Double)accB.getX(), (Double)accA.getX(),
                                tolerance);
                        boolean toleranceB = isPassable((Double)accB.getY(), (Double)accA.getY(),
                                tolerance);
                        boolean toleranceC = isPassable((Double)accB.getZ(), (Double)accA.getZ(),
                                tolerance);

                        if (!(toleranceA && toleranceB && toleranceC)) {
                            if (verbose) {
                                logger.info("Different Values. Expected: {} - Find: {}",
                                        accA.toString(), accB.toString());
                            }
                            return false;
                        }
                    }
                }
            } else if ((valueA.getSchema().getName().equals("Battery")
                    && valueB.getSchema().getName().equals("Battery"))
                    || (valueA.getSchema().getName().equals("BloodVolumePulse")
                    && valueB.getSchema().getName().equals("BloodVolumePulse"))
                    || (valueA.getSchema().getName().equals("ElectroDermalActivity")
                    && valueB.getSchema().getName().equals("ElectroDermalActivity"))
                    || (valueA.getSchema().getName().equals("HeartRate")
                    && valueB.getSchema().getName().equals("HeartRate"))
                    || (valueA.getSchema().getName().equals("InterBeatInterval")
                    && valueB.getSchema().getName().equals("InterBeatInterval"))
                    || (valueA.getSchema().getName().equals("Temperature")
                    && valueB.getSchema().getName().equals("Temperature"))) {
                if (header.getDescriptiveStatistic().name().equals(
                        DescriptiveStatistic.quartiles.name())) {
                    Quartiles quartilesA = (Quartiles) valueA.get(
                            valueA.getSchema().getField("value").pos());
                    Quartiles quartilesB = (Quartiles) valueB.get(
                            valueB.getSchema().getField("value").pos());
                    if ( !compareQuartiles(quartilesA, quartilesB, tolerance) ) {
                        if (verbose) {
                            logger.info("Different Quartiles. Expected: {} - Find: {}",
                                    valueA.toString(), valueB.toString());
                        }
                        return false;
                    }
                } else {
                    Double doubleA = (Double) valueA.get(
                            valueA.getSchema().getField("value").pos());
                    Double doubleB = (Double) valueB.get(
                            valueB.getSchema().getField("value").pos());
                    if (!doubleA.equals(doubleB) && !isPassable(doubleA, doubleB, tolerance)) {
                        return false;
                    }
                }
            } else {
                throw new IllegalArgumentException(valueA.getSchema().getName() + " and "
                    + valueA.getSchema().getName() + " are not supported test cases");
            }
        }

        return true;
    }

    /**
     * It checks if two {@code Double} are equals except for threshold expressed in percentage.
     * @param valueA first component that to has to be compared
     * @param valueB second component that to has to be compared
     * @param tolerance {@code Double} value are subjected to approximations during computations,
     *      for these reason the expected value and the real one may differ. This difference in
     *      percentage has to be smaller than this threshold.
     * @return {@code true} if they match, false otherwise
     **/
    private static boolean isPassable(Double valueA, Double valueB, double tolerance) {
        if (tolerance == 0.0) {
            return valueA.equals(valueB);
        }

        if (valueA > valueB) {
            return tolerance >= ( 1.0 - valueA / valueB );
        } else if (valueB > valueA) {
            return tolerance >= ( 1.0 - valueB / valueA );
        } else {
            return valueA.equals(valueB);
        }
    }

    /**
     * Compare two {@code EffectiveTimeFrame} values.
     *      @see {@link org.radarcns.avro.restapi.header.EffectiveTimeFrame}
     * @param etfa first component that to has to be compared
     * @param etfb second component that to has to be compared
     * @return {@code true} if they match, false otherwise
     **/
    private static boolean compareEffectiveTimeFrame(EffectiveTimeFrame etfa,
            EffectiveTimeFrame etfb) {
        return etfa.getStartDateTime().equals(etfb.getStartDateTime())
                && etfa.getEndDateTime().equals(etfb.getEndDateTime());
    }

    /**
     * It checks if two {@code Quartiles} are equals except for threshold expressed in percentage.
     *      @see {@link org.radarcns.avro.restapi.dataset.Quartiles}.
     * @param quartileA first component that to has to be compared
     * @param quartileB second component that to has to be compared
     * @param tolerance {@code Double} value are subjected to approximations during computations,
     *      for these reason the expected value and the real one may differ. This difference in
     *      percentage has to be smaller than this threshold.
     * @return {@code true} if they match, false otherwise
     **/
    private static boolean compareQuartiles(Quartiles quartileA, Quartiles quartileB,
                double tolerance) {
        if (quartileA.getFirst().equals(quartileB.getFirst())
                && quartileA.getSecond().equals(quartileB.getSecond())
                && quartileA.getThird().equals(quartileB.getThird())) {
            return true;
        }

        boolean toleranceA = isPassable(quartileA.getFirst(), quartileB.getFirst(), tolerance);
        boolean toleranceB = isPassable(quartileA.getSecond(), quartileB.getSecond(), tolerance);
        boolean toleranceC = isPassable(quartileA.getThird(), quartileB.getThird(), tolerance);

        return toleranceA && toleranceB && toleranceC;
    }

}