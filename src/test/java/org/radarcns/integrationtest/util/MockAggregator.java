package org.radarcns.integrationtest.util;

import java.io.IOException;
import java.util.HashMap;
import org.radarcns.integrationtest.collector.ExpectedArrayValue;
import org.radarcns.integrationtest.collector.ExpectedDoubleValue;
import org.radarcns.integrationtest.util.Parser.Variable;

/**
 * Created by francesco on 15/02/2017.
 */
public class MockAggregator {

    public static ExpectedArrayValue simulateArrayCollector(Parser parser) throws IOException {
        ExpectedArrayValue eav = new ExpectedArrayValue();

        HashMap<Variable, Object> record = parser.next();
        while (record != null){
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
        while (record != null){
            edv.add((Long) record.get(Variable.TIME_WINDOW),
                (Long) record.get(Variable.TIMESTAMP),
                (Double) record.get(Variable.VALUE));

            record = parser.next();
        }

        parser.close();

        return edv;
    }
}
