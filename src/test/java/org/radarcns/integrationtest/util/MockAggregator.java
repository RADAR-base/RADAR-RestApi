package org.radarcns.integrationtest.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.radarcns.integrationtest.collector.ExpectedArrayValue;
import org.radarcns.integrationtest.collector.ExpectedDoubleValue;
import org.radarcns.integrationtest.collector.ExpectedValue;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.radarcns.integrationtest.util.Parser.ExpectedType;
import org.radarcns.integrationtest.util.Parser.Variable;

/**
 * Created by francesco on 15/02/2017.
 */
public class MockAggregator {

    public static ExpectedArrayValue simulateArrayCollector(Parser parser) throws IOException {
        ExpectedArrayValue eav = new ExpectedArrayValue();

        HashMap<Variable, Object> record = parser.next();

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

    public static ExpectedDoubleValue simulateSingletonCollector(Parser parser) throws IOException {
        ExpectedDoubleValue edv = new ExpectedDoubleValue();

        HashMap<Variable, Object> record = parser.next();

        if (record != null) {
            edv.setUser((String) record.get(Variable.USER));
            edv.setSource((String) record.get(Variable.SOURCE));
        }

        while (record != null){
            edv.add((Long) record.get(Variable.TIME_WINDOW),
                (Long) record.get(Variable.TIMESTAMP),
                (Double) record.get(Variable.VALUE));

            record = parser.next();
        }

        parser.close();

        return edv;
    }

    public static Map<MockDataConfig, ExpectedValue> simulateSingleton(List<MockDataConfig> configs)
        throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException,
        InvocationTargetException {
        Map<MockDataConfig, ExpectedValue> exepctedValue = new HashMap<>();

        for ( MockDataConfig config : configs ) {
            Parser parser =  new Parser(config);
            if ( parser.getExpecedType().equals(ExpectedType.DOUBLE) ){
                exepctedValue.put(config, MockAggregator.simulateSingletonCollector(parser));
            }
        }

        return exepctedValue;
    }

    public static  Map<MockDataConfig, ExpectedValue> simulateArray(List<MockDataConfig> configs)
        throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException,
        InvocationTargetException {
        Map<MockDataConfig, ExpectedValue> exepctedValue = new HashMap<>();

        for ( MockDataConfig config : configs ) {
            Parser parser =  new Parser(config);
            if ( parser.getExpecedType().equals(ExpectedType.ARRAY) ){
                exepctedValue.put(config, MockAggregator.simulateArrayCollector(parser));
            }
        }

        return exepctedValue;
    }

    public static Map<MockDataConfig, ExpectedValue> getSimulations(List<MockDataConfig> configs)
        throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException,
        InvocationTargetException {
        Map<MockDataConfig, ExpectedValue> map = new HashMap<>();
        map.putAll(simulateSingleton(configs));
        map.putAll(simulateArray(configs));

        return map;
    }

}
