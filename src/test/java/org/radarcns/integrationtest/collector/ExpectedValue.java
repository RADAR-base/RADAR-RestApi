package org.radarcns.integrationtest.collector;

import static org.radarcns.avro.restapi.header.DescriptiveStatistic.quartiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

/**
 * Created by francesco on 15/02/2017.
 */
public abstract class ExpectedValue<V> {

    //Timewindow length in milliseconds
    protected long DURATION = 10000;

    protected String user;
    protected String source;

    protected Long lastTimestamp;
    protected V lastValue;
    protected HashMap<Long, V> series;

    public EffectiveTimeFrame getEffectiveTimeFrame(){
        List<Long> windows = new ArrayList<>(series.keySet());
        java.util.Collections.sort(windows);

        EffectiveTimeFrame eft = new EffectiveTimeFrame(windows.get(0).toString(),
            Long.toString(windows.get(windows.size() - 1) + DURATION));

        return eft;
    }

    public EffectiveTimeFrame getEffectiveTimeFrame(Long value){
        return new EffectiveTimeFrame(value.toString(),
            Long.toString(value + DURATION));
    }

    public Header getHeader(DescriptiveStatistic statistic, Unit unit){
        return new Header(statistic, unit, getEffectiveTimeFrame());
    }

    public List<Item> getItem(DescriptiveStatistic statistic, MockDataConfig config){
        ExpectedType type = Parser.getExpectedType(config);
        switch (type) {
            case ARRAY: return getArrayItems(series.keySet(), statistic, config);
            case DOUBLE: return getSingletonItems(series.keySet(), statistic, config);
            default:
                throw new IllegalArgumentException("Cannot type " + type.getValue());
        }
    }

    private List<Item> getArrayItems(Collection<Long> keys, DescriptiveStatistic statistic,
        MockDataConfig config) {
        List<Item> items = new LinkedList<>();

        for (Long key : keys) {
            DoubleArrayCollector dac = (DoubleArrayCollector) series.get(key);

            if (config.getRestCall().contains("/Acc/")) {
                Object content;

                if ( statistic.name().equals(quartiles.name()) ){
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
                throw new IllegalArgumentException(config.getRestCall() +
                    " is not a supported test case");
            }
        }

        return items;
    }

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
                throw new IllegalArgumentException(config.getRestCall() +
                    " is not a supported test case");
            }

            items.add(new Item(content, getEffectiveTimeFrame(key)));
        }


        return items;
    }

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
            minList.add(collector.getMin());
            maxList.add(collector.getMax());
            sumList.add(collector.getSum());
            countList.add(collector.getCount());
            avgList.add(collector.getAvg());
            iqrList.add(collector.getIqr());
            quartileList.add(collector.getQuartile());
            medList.add(collector.getQuartile().get(1));
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

    private Quartiles getQuartile(List<Double> list){
        return new Quartiles(list.get(0), list.get(1), list.get(2));
    }

    public Dataset getDataset(DescriptiveStatistic statistic, MockDataConfig config) {
        Unit unit;

        if (config.getRestCall().contains("/Acc/")) {
            unit = Unit.g;
        } else if (config.getRestCall().contains("/B/")) {
            unit = Unit.dimensionless;
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
            throw new IllegalArgumentException(config.getRestCall() +
                " is not a supported test case");
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
    public String toString(){
        String result = "";
        for ( Long interval : series.keySet() ){
            result += interval.toString() + "-" + (interval + DURATION) + '\t' +
                series.get(interval).toString() + "\n";
        }
        return result;
    }

}
