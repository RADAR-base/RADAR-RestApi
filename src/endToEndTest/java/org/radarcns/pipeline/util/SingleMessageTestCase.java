package org.radarcns.pipeline.util;

import java.io.IOException;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;

/*
 * Copyright 2017 King's College London and The Hyve
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
public abstract class SingleMessageTestCase<K extends SpecificRecord, V extends SpecificRecord>
        extends TestCase{

    protected final String userId;
    protected final String sourceId;

    private final Sender<K,V> sender;

    private final Logger logger;

    public SingleMessageTestCase(String userId, String sourceId, PipelineConfig config,
            Logger logger) {
        this.userId = userId;
        this.sourceId = sourceId;

        this.logger = logger;

        this.sender = new Sender<>(config, getTopicName(), isSensor(), logger);
    }

    public void send() {
        try {
            this.sender.send(getKey(), getValue());
        } catch (IOException exe) {
            logger.error("Cannot send message", exe);
            throw new SenderRuntimeException("Cannot send message", exe);
        } finally {
            close();
        }
    }

    private void close() {
        try {
            sender.close();
        } catch (IOException exe) {
            logger.error("Cannot close the sender", exe);
            throw new SenderRuntimeException("Cannot close the sender", exe);
        }
    }

    public abstract String getTopicName();

    public abstract boolean isSensor();

    protected abstract K getKey();

    protected abstract V getValue();
}
