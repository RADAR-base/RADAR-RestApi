package org.radarcns.old.cvs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.radarcns.old.config.MockDataConfig;
import org.radarcns.old.util.Parser;
import org.radarcns.old.util.Parser.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
/**
 * CSV files must be validate before using since MockAggregator can handle only files containing
 *      unique User_ID and Source_ID and having increasing timestamp at each raw.
 */
public class CSVValidator {

    private static final Logger logger = LoggerFactory.getLogger(CSVValidator.class);

    /**
     * Verify whether the CSV file can be used or not.
     * @param config configuration item containing the CSV file path.
     * @throws IllegalArgumentException if the CSV file does not respect the constrains.
     */
    public static void validate(MockDataConfig config)
        throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {

        Parser parser = new Parser(config);

        int line = 1;
        String mex = null;

        Map<Variable, Object> map = parser.next();
        Map<Variable, Object> last = map;
        while (map != null) {
            line++;

            if (!last.get(Variable.USER).toString().equals(map.get(Variable.USER).toString())) {
                mex = "It is possible to test only one user at time.";
            } else if ( !last.get(Variable.SOURCE).toString().equals(
                    map.get(Variable.SOURCE).toString()) ) {
                mex = "It is possible to test only one source at time.";
            } else if ( !( ((Long)map.get(Variable.TIMESTAMP)).longValue()
                    >= ((Long)last.get(Variable.TIMESTAMP)).longValue() ) ) {
                mex = Variable.TIMESTAMP.toString() + " must increase raw by raw.";
            } else if ( map.get(Variable.VALUE) == null ) {
                mex = Variable.VALUE.toString() + "value to test must be specified.";
            }

            if ( mex != null) {
                mex += " " + config.getDataFile() + " is invalid. Error at line " + line;
                logger.error(mex);
                throw new IllegalArgumentException(mex);
            }

            last = map;
            map = parser.next();
        }

        parser.close();
    }

}
