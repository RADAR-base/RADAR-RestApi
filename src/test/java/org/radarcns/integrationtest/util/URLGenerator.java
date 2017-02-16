package org.radarcns.integrationtest.util;

import java.util.LinkedList;
import java.util.List;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 15/02/2017.
 */
public class URLGenerator {

    private static final Logger logger = LoggerFactory.getLogger(URLGenerator.class);

    private final static String USER_PLACEHOLDER = "{userID}";
    private final static String SOURCE_PLACEHOLDER = "{sourceID}";
    private final static String STAT_PLACEHOLDER = "{stat}";

    private final static String SERVER = "http://52.210.59.174:8080/";
    private final static String PATH = "radar/api";

    public static List<String> generate(String userID, String sourceID, MockDataConfig config) {
        List<String> list = new LinkedList<>();

        String testPath = config.getRestCall();
        testPath = testPath.replace(USER_PLACEHOLDER, userID);
        testPath = testPath.replace(SOURCE_PLACEHOLDER, sourceID);

        Stat[] statFunctions = MongoHelper.Stat.values();
        for ( int i=0; i<statFunctions.length; i++ ) {
            list.add(SERVER + PATH + testPath.replace(STAT_PLACEHOLDER, statFunctions[i].name()));
        }

        return list;
    }

    public static String generate(String userID, String sourceID, DescriptiveStatistic stat,
        MockDataConfig config) {
        String testPath = config.getRestCall();
        testPath = testPath.replace(USER_PLACEHOLDER, userID);
        testPath = testPath.replace(SOURCE_PLACEHOLDER, sourceID);

        MongoHelper.Stat statistic = RadarConverter.getDescriptiveStatistic(stat);

        return SERVER + PATH + testPath.replace(STAT_PLACEHOLDER, statistic.name());
    }
}
