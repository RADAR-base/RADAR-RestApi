/*
 * Copyright 2016 King's College London and The Hyve
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

package org.radarcns.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to convert AVRO object to JSON and Bytes array.
 */
public class AvroConverter {
    private static Logger logger = LoggerFactory.getLogger(AvroConverter.class);

    /**
     * Returns an encoded JSON object for the given Avro object.
     *
     * @param record is the record to encode
     * @return the JSON object representing this Avro object.
     */
    public static JsonNode avroToJsonNode(SpecificRecord record) {
        try {
            return RadarConverter.GENERIC_JSON_READER.readTree(
                    RadarConverter.AVRO_JSON_WRITER.writeValueAsString(record));
        } catch (IOException exec) {
            logger.error("Impossible to generate error message", exec);
        }

        return null;
    }

    /**
     * Returns a byte array version of the given record.
     *
     * @param record is the record to encode
     * @return the byte array representing this Avro object.
     *
     * @throws IOException due to {@code DatumWriter}
     *
     * @see DatumWriter
     */
    public static <K extends SpecificRecord> byte[] avroToAvroByte(K record) throws IOException {
        DatumWriter<K> writer = new SpecificDatumWriter<>(record.getSchema());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);

        writer.write(record, encoder);

        encoder.flush();
        return output.toByteArray();
    }

    /**
     * Returns a byte array version of the given record.
     *
     * @param input is the byte array that has to be deserialised
     * @param schema to serialise
     * @return the deserialised record
     *
     * @throws IOException due to {@code DatumReader}
     *
     * @see DatumReader
     */
    public static <K extends SpecificRecord> K avroByteToAvro(byte[] input, Schema schema)
            throws IOException {
        DatumReader<K> reader = new SpecificDatumReader<>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(input, null);

        return reader.read(null, decoder);
    }

}
