package org.radarcns.pipeline.testcase;

/*
 * Copyright 2017 Kings College London and The Hyve
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

import static org.junit.Assert.assertEquals;
import static org.radarcns.webapp.util.BasePath.ANDROID;
import static org.radarcns.webapp.util.BasePath.AVRO;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import java.io.IOException;
import okhttp3.Response;
import org.radarcns.application.ApplicationRecordCounts;
import org.radarcns.application.ApplicationServerStatus;
import org.radarcns.application.ApplicationUptime;
import org.radarcns.application.ServerStatus;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.key.MeasurementKey;
import org.radarcns.pipeline.util.PipelineConfig;
import org.radarcns.pipeline.util.Sender;
import org.radarcns.pipeline.util.SenderRuntimeException;
import org.radarcns.pipeline.util.TestCase;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApplicationEndToEndTest extends TestCase{

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationEndToEndTest.class);

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final ServerStatus STATUS = ServerStatus.CONNECTED;
    private static final Integer MOCK_INTEGER = 10;
    private static final Double MOCK_DOUBLE = 10d;
    private static final double TIME = System.currentTimeMillis() / 1000d;

    private final PipelineConfig config;

    private final Sender<MeasurementKey,ApplicationRecordCounts> recordCountSender;
    private final Sender<MeasurementKey,ApplicationServerStatus> serverStatusSender;
    private final Sender<MeasurementKey,ApplicationUptime> uptimeSender;

    public ApplicationEndToEndTest(PipelineConfig config) {
        this.config = config;

        this.recordCountSender = new Sender<>(config, "application_record_counts",
                true, LOGGER);
        this.serverStatusSender = new Sender<>(config, "application_server_status",
            true, LOGGER);
        this.uptimeSender = new Sender<>(config, "application_uptime",
            true, LOGGER);
    }

    public void send() {
        try {
            recordCountSender.send(getKey(), getRecordCount());
            serverStatusSender.send(getKey(), getServerStatus());
            uptimeSender.send(getKey(), getUptime());
        } catch (IOException exe) {
            LOGGER.error("Cannot send message", exe);
            throw new SenderRuntimeException("Cannot send message", exe);
        } finally {
            close();
        }
    }

    private void close() {
        try {
            recordCountSender.close();
        } catch (IOException exe) {
            LOGGER.error("Cannot close recordCountSender", exe);
            throw new SenderRuntimeException("Cannot close recordCountSender", exe);
        } finally {
            try {
                serverStatusSender.close();
            } catch (IOException exe) {
                LOGGER.error("Cannot close serverStatusSender", exe);
                throw new SenderRuntimeException("Cannot close serverStatusSender", exe);
            } finally {
                try {
                    uptimeSender.close();
                } catch (IOException exe) {
                    LOGGER.error("Cannot close uptimeSender", exe);
                    throw new SenderRuntimeException("Cannot close uptimeSender", exe);
                }
            }
        }
    }

    private MeasurementKey getKey() {
        return new MeasurementKey(USER_ID_MOCK, SOURCE_ID_MOCK);
    }

    private ApplicationRecordCounts getRecordCount() {
        return new ApplicationRecordCounts(TIME, TIME, MOCK_INTEGER, MOCK_INTEGER, MOCK_INTEGER);
    }

    private ApplicationServerStatus getServerStatus() {
        return new ApplicationServerStatus(TIME, TIME, STATUS, IP_ADDRESS);
    }

    private ApplicationUptime getUptime() {
        return new ApplicationUptime(TIME, TIME, MOCK_DOUBLE);
    }

    @Override
    public void checkEndPoint() {
        Application expected = new Application(IP_ADDRESS, MOCK_DOUBLE, STATUS, MOCK_INTEGER,
                MOCK_INTEGER, MOCK_INTEGER);

        Application actual = null;

        String path = ANDROID + "/" + AVRO + "/" + STATUS + "/{" + SUBJECT_ID
            + "}/{" + SOURCE_ID + "}";
        path = path.replace("{" + SUBJECT_ID + "}", USER_ID_MOCK);
        path = path.replace("{" + SOURCE_ID + "}", SOURCE_ID_MOCK);

        try (RestClient client = new RestClient(config.getRestApi())) {
            LOGGER.info("Requesting {}", client.getRelativeUrl(path));

            try (Response response = client.request(path)) {
                assertEquals(200, response.code());

                LOGGER.info("[{}] {}", response.code(), path);

                if (response.code() == 200) {
                    actual = AvroConverter.avroByteToAvro(response.body().bytes(),
                        Application.getClassSchema());
                }
            }
        } catch (IOException exe) {
            LOGGER.error("Cannot make request to {}", path, exe);
            throw new IllegalArgumentException("Cannot make request to " + path, exe);
        }

        assertEquals(expected, actual);
    }
}
