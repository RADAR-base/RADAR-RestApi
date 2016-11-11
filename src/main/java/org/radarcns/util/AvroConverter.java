package org.radarcns.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Francesco Nobilia on 11/11/2016.
 */
public class AvroConverter {

    public static Logger logger = LoggerFactory.getLogger(AvroConverter.class);

    /**
     * Returns an encoded JSON string for the given Avro object.
     *
     * @param record is the record to encode
     * @return the JSON string representing this Avro object.
     *
     * @throws IOException if there is an error.
     */
    public static String getJsonString(SpecificRecord record){
        try {

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), os);
            DatumWriter<SpecificRecord> writer = new GenericDatumWriter<>();
            if (record instanceof SpecificRecord) {
                writer = new SpecificDatumWriter<>();
            }

            writer.setSchema(record.getSchema());

                writer.write(record, encoder);

            encoder.flush();
            String jsonString = new String(os.toByteArray(), Charset.forName("UTF-8"));
            os.close();

            return jsonString;

        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    /**
     * Returns an encoded JSON object for the given Avro object.
     *
     * @param record is the record to encode
     * @return the JSON object representing this Avro object.
     *
     * @throws IOException if there is an error.
     */
    public static JsonNode getJsonNode(SpecificRecord record){
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(getJsonString(record));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

/*


    */
/**
     * Convert JSON to avro binary array.
     *
     * @param json
     * @param schema
     * @return
     * @throws IOException
    public static byte[] jsonToAvro(String json, Schema schema) throws IOException {
        DatumReader<Object> reader = new GenericDatumReader<>(schema);
        GenericDatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Decoder decoder = DecoderFactory.get().jsonDecoder(schema, json);
        Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);
        Object datum = reader.read(null, decoder);
        writer.write(datum, encoder);
        encoder.flush();
        return output.toByteArray();
    }

    *//*
*/
/**
     * Convert Avro binary byte array back to JSON String.
     *
     * @param avro
     * @param schema
     * @return
     * @throws IOException
     *//*

    public static String avroToJson(byte[] avro, Schema schema) throws IOException {
        boolean pretty = false;
        GenericDatumReader<Object> reader = new GenericDatumReader<>(schema);
        DatumWriter<Object> writer = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, output, pretty);
        Decoder decoder = DecoderFactory.get().binaryDecoder(avro, null);
        Object datum = reader.read(null, decoder);
        writer.write(datum, encoder);
        encoder.flush();
        output.flush();
        return new String(output.toByteArray(), "UTF-8");
    }

*/
}
