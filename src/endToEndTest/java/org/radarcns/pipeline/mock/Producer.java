package org.radarcns.pipeline.mock;

/*
 *  Copyright 2016 Kings College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.data.SpecificRecordEncoder;
import org.radarcns.key.MeasurementKey;
import org.radarcns.mock.MockFile;
import org.radarcns.pipeline.config.Config;
import org.radarcns.pipeline.mock.config.MockConfig;
import org.radarcns.producer.KafkaSender;
import org.radarcns.producer.rest.BatchedKafkaSender;
import org.radarcns.producer.rest.RestSender;
import org.radarcns.producer.rest.SchemaRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock Producer that sends data stored in cvs file linked by mock-file.yml on the resource folder.
 */
public class Producer {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    private final Map<String, MockFile> files;
    private final List<KafkaSender<MeasurementKey, SpecificRecord>> senders;

    /**
     * Constructor.
     */
    public Producer() throws IOException {
        MockConfig mockConfig = Config.getMockConfig();

        int numDevices = mockConfig.getData().size();

        logger.info("Simulating the load of " + numDevices);

        senders = new ArrayList<>(numDevices);
        files = new HashMap<>(numDevices);

        SchemaRetriever retriever = new SchemaRetriever(
                Config.getPipelineConfig().getSchemaRegistryInstance());

        RestSender<MeasurementKey, SpecificRecord> firstSender = new RestSender<>(
                Config.getPipelineConfig().getRestProxyInstance(), retriever,
                new SpecificRecordEncoder(false), new SpecificRecordEncoder(false),
                10_000);
        for (int i = 0; i < numDevices; i++) {
            senders.add(new BatchedKafkaSender<>(firstSender, 10_000, 1000));
        }

        try {
            for (int i = 0; i < numDevices; i++) {
                files.put(mockConfig.getData().get(i).getTopic(), new MockFile(senders.get(i),
                            Config.getBaseFile(), mockConfig.getData().get(i)));
            }
        } catch (NoSuchMethodException | IllegalAccessException
            | InvocationTargetException | ClassNotFoundException ex) {
            throw new IOException("Cannot instantiate mock file", ex);
        }
    }

    /**
     * Starts all available mock devices.
     *
     * @throws IOException from more details, please check {@link MockFile#send()}
     * @throws InterruptedException from more details, please check {@link MockFile#send()}
     */
    public void start() throws IOException, InterruptedException {
        for (String topic : files.keySet()) {
            logger.info("Sending messages into {} ...", topic.toUpperCase());
            files.get(topic).send();
            logger.info("Sent all messages for {}", topic.toUpperCase());
        }
    }

    /**
     * Stops all available mock devices.
     *
     * @throws IOException from more details, please check {@link KafkaSender#close()}
     * @throws InterruptedException from more details, please check {@link KafkaSender#close()}
     */
    public void shutdown() throws IOException, InterruptedException {
        logger.info("Closing channels");
        for (KafkaSender<MeasurementKey, SpecificRecord> sender : senders) {
            sender.close();
        }
        logger.info("CLOSED ALL CHANNELS");
    }
}
