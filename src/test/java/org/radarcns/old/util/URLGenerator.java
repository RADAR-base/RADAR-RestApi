package org.radarcns.old.util;

import java.util.LinkedList;
import java.util.List;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.old.config.MockDataConfig;

/**
 * URL generator for the HTTP client.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class URLGenerator {

    //private static final Logger logger = LoggerFactory.getLogger(URLGenerator.class);

    private static final String USER_PLACEHOLDER = "{user}";
    private static final String SOURCE_PLACEHOLDER = "{source}";
    private static final String STAT_PLACEHOLDER = "{stat}";

    private static final String SERVER = "http://52.210.59.174:8080/";
    private static final String PATH = "radar/api";

    /**
     * @param user userID used to generate the URL
     * @param source sourceID used to generate the URL
     * @param config contains the based URL template.
     *          @see {@link org.radarcns.old.config.MockDataConfig}
     * @return String list containing all URLa that have to be tested based on the config input
     *          and all available statistical functions.
     *          @see {@link org.radarcns.avro.restapi.header.DescriptiveStatistic}
     * */
    public static List<String> generate(String user, String source, MockDataConfig config) {
        List<String> list = new LinkedList<>();

        String testPath = config.getRestCall();
        testPath = testPath.replace(USER_PLACEHOLDER, user);
        testPath = testPath.replace(SOURCE_PLACEHOLDER, source);

        DescriptiveStatistic[] statFunctions = DescriptiveStatistic.values();
        for (int i = 0; i < statFunctions.length; i++) {
            list.add(SERVER + PATH + testPath.replace(STAT_PLACEHOLDER, statFunctions[i].name()));
        }

        return list;
    }

    /**
     * @param user userID used to generate the URL
     * @param source sourceID used to generate the URL
     * @param stat value that has to be return
     *          @see {@link org.radarcns.avro.restapi.header.DescriptiveStatistic}
     * @param config contains the based URL template.
     *          @see {@link org.radarcns.old.config.MockDataConfig}
     * @return the URL that has to be tested
     * */
    public static String generate(String user, String source, DescriptiveStatistic stat,
            MockDataConfig config) {
        String testPath = config.getRestCall();
        testPath = testPath.replace(USER_PLACEHOLDER, user);
        testPath = testPath.replace(SOURCE_PLACEHOLDER, source);

        return SERVER + PATH + testPath.replace(STAT_PLACEHOLDER, stat.name());
    }
}
