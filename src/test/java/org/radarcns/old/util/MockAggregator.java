package org.radarcns.old.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.radarcns.old.collector.ExpectedArrayValue;
import org.radarcns.old.collector.ExpectedDoubleValue;
import org.radarcns.old.collector.ExpectedValue;
import org.radarcns.old.config.MockDataConfig;
import org.radarcns.old.util.Parser.ExpectedType;
import org.radarcns.old.util.Parser.Variable;

/**
 * The MockAggregator simulates the behaviour of a Kafka Streams application based on time window.
 * It supported accumulators are <ul>
 *      <li>array of {@code Double}
 *      <li>singleton {@code Double}
 *  </ul>
 */
public class MockAggregator {

    /**
     * @param parser class that reads a CVS file line by line returning an {@code Map} .
     * @return {@code ExpectedArrayValue} the simulated results computed using the input parser.
     * @see {@link org.radarcns.old.collector.ExpectedArrayValue}
     **/
    public static ExpectedArrayValue simulateArrayCollector(Parser parser) throws IOException {
        ExpectedArrayValue eav = new ExpectedArrayValue();

        Map<Variable, Object> record = parser.next();

        if (record != null) {
            eav.setUser((String) record.get(Variable.USER));
            eav.setSource((String) record.get(Variable.SOURCE));
        }

        while ( record != null ) {
            eav.add((Long) record.get(Variable.TIME_WINDOW),
                    (Long) record.get(Variable.TIMESTAMP),
                    (Double[]) record.get(Variable.VALUE));

            record = parser.next();
        }

        parser.close();

        return eav;
    }

    /**
     * @param parser class that reads a CVS file line by line returning an {@code HashMap}.
     * @return {@code ExpectedDoubleValue} the simulated results computed using the input parser.
     * @see {@link org.radarcns.old.collector.ExpectedDoubleValue}
     **/
    public static ExpectedDoubleValue simulateSingletonCollector(Parser parser) throws IOException {
        ExpectedDoubleValue edv = new ExpectedDoubleValue();

        Map<Variable, Object> record = parser.next();

        if (record != null) {
            edv.setUser((String) record.get(Variable.USER));
            edv.setSource((String) record.get(Variable.SOURCE));
        }

        while (record != null) {
            edv.add((Long) record.get(Variable.TIME_WINDOW),
                    (Long) record.get(Variable.TIMESTAMP),
                    (Double) record.get(Variable.VALUE));

            record = parser.next();
        }

        parser.close();

        return edv;
    }

    /**
     * Given a list of configurations, it simulates all of them that has
     *      {@link Parser.ExpectedType#DOUBLE} as expected type.
     * @param configs list containing all configurations that have to be tested.
     * @return {@code Map} of key {@code MockDataConfig} and value {@code ExpectedValue}.
     * @see {@link org.radarcns.old.collector.ExpectedDoubleValue}.
     **/
    public static Map<MockDataConfig, ExpectedValue> simulateSingleton(List<MockDataConfig> configs)
        throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException,
        InvocationTargetException {
        Map<MockDataConfig, ExpectedValue> exepctedValue = new HashMap<>();

        for (MockDataConfig config : configs) {
            Parser parser =  new Parser(config);
            if (parser.getExpecedType().equals(ExpectedType.DOUBLE)) {
                exepctedValue.put(config, MockAggregator.simulateSingletonCollector(parser));
            }
        }

        return exepctedValue;
    }

    /**
     * Given a list of configurations, it simulates all of them that has
     *      {@link Parser.ExpectedType#ARRAY} as expected type.
     * @param configs list containing all configurations that have to be tested.
     * @return {@code Map} of key {@code MockDataConfig} and value {@code ExpectedValue}.
     * @see {@link org.radarcns.old.collector.ExpectedDoubleValue}.
     **/
    public static  Map<MockDataConfig, ExpectedValue> simulateArray(List<MockDataConfig> configs)
        throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException,
        InvocationTargetException {
        Map<MockDataConfig, ExpectedValue> exepctedValue = new HashMap<>();

        for (MockDataConfig config : configs) {
            Parser parser =  new Parser(config);
            if (parser.getExpecedType().equals(ExpectedType.ARRAY)) {
                exepctedValue.put(config, MockAggregator.simulateArrayCollector(parser));
            }
        }

        return exepctedValue;
    }

    /**
     * Given a list of configurations, it simulates all possible test case scenarios.
     * @param configs list containing all configurations that have to be tested.
     * @return {@code Map} of key {@code MockDataConfig} and value {@code ExpectedValue}.
     * @see {@link org.radarcns.old.collector.ExpectedDoubleValue}.
     **/
    public static Map<MockDataConfig, ExpectedValue> getSimulations(List<MockDataConfig> configs)
        throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException,
        InvocationTargetException {
        Map<MockDataConfig, ExpectedValue> map = new HashMap<>();
        map.putAll(simulateSingleton(configs));
        map.putAll(simulateArray(configs));

        return map;
    }

}
