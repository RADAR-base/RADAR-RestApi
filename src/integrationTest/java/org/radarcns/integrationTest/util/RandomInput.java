package org.radarcns.integrationTest.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.integrationTest.aggregator.ExpectedDoubleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All supported sources specifications.
 */
public class RandomInput {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomInput.class);

    private static final Map<SourceType, Dataset> datesets = new HashMap<>();
    private static final Map<SourceType, List<Document>> documents = new HashMap<>();

    private static void randomDoubleValue(String user, String source,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic stat, int samples)
            throws InstantiationException, IllegalAccessException {
        ExpectedDoubleValue instance = new ExpectedDoubleValue(user, source);

        Long start = new Date().getTime();

        for (int i = 0; i < samples; i++) {
            instance.add(Utility.getStartTimeWindow(start), start,
                    ThreadLocalRandom.current().nextDouble());

            start += TimeUnit.SECONDS.toMillis(
                    ThreadLocalRandom.current().nextInt(1,12));
        }

        datesets.put(sourceType, instance.getDataset(stat, sourceType, sensorType));
        documents.put(sourceType, instance.getDocuments());
    }

    public static Dataset getDatasetRandom(String user, String source,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic stat, int samples)
            throws InstantiationException, IllegalAccessException {
        switch (sourceType) {
            case ANDROID: break;
            case BIOVOTION: break;
            case EMPATICA: return getDataset(user, source, sourceType, sensorType, stat, samples);
            case PEBBLE: break;
            default: break;
        }

        throw new UnsupportedOperationException(sourceType.name() + " is not"
            + " currently supported.");
    }

    public static List<Document> getDocumentsRandom(String user, String source,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic stat, int samples)
            throws InstantiationException, IllegalAccessException {
        switch (sourceType) {
            case ANDROID: break;
            case BIOVOTION: break;
            case EMPATICA: return getDocument(user, source, sourceType, sensorType, stat, samples);
            case PEBBLE: break;
            default: break;
        }

        throw new UnsupportedOperationException(sourceType.name() + " is not"
            + " currently supported.");
    }

    private static Dataset getDataset(String user, String source,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic stat, int samples)
            throws InstantiationException, IllegalAccessException {
        if (datesets.get(sourceType) == null) {
            randomDoubleValue(user, source, sourceType, sensorType, stat, samples);
        }

        return datesets.get(sourceType);
    }

    private static List<Document> getDocument(String user, String source,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic stat, int samples)
            throws InstantiationException, IllegalAccessException {
        if (documents.get(sourceType) == null) {
            randomDoubleValue(user, source, sourceType, sensorType, stat, samples);
        }

        return documents.get(sourceType);
    }

}
