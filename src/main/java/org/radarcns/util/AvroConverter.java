package org.radarcns.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Francesco Nobilia on 11/11/2016.
 */
public class AvroConverter {

    private static Logger logger = LoggerFactory.getLogger(AvroConverter.class);

    public static JsonNode avroToJsonNode(SpecificRecord record){
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(record.toString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    /**
     * Returns an encoded JSON string for the given Avro object.
     *
     * @param record is the record to encode
     * @return the JSON string representing this Avro object.
     *
     * @throws IOException if there is an error.
     */
    public static String avroObjToJsonString(SpecificRecord record){
        try {

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), os);

            DatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>();

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
    public static JsonNode avroObjToJsonNode(SpecificRecord record){
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(avroObjToJsonString(record));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return null;
    }



    /**
     * Convert JSON to avro binary array.
     *
     * @param json
     * @param schema
     * @return
     * @throws IOException
     */
    public static byte[] jsonToAvroByte(String json, Schema schema) throws IOException {
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


    /**
     * Convert Avro binary byte array back to JSON String.
     *
     * @param avro
     * @param schema
     * @return
     * @throws IOException
     */
    public static String avroByteToJson(byte[] avro, Schema schema) throws IOException {
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

    public static byte[] avroObjToBytes(Object activity) throws IOException {
        Schema schema = ReflectData.get().getSchema(activity.getClass());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ReflectDatumWriter< Object > reflectDatumWriter = new ReflectDatumWriter< Object >(schema);
        DataFileWriter< Object > writer = new DataFileWriter< Object >(reflectDatumWriter).create(schema, outputStream);
        writer.append(activity);
        writer.close();
        return outputStream.toByteArray();
    }

    public static < T > T bytesToAvroObj(Class< T > returnType, byte[] bytes) throws IOException {
        Schema schema = ReflectData.get().getSchema(returnType);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ReflectDatumReader< T > reflectDatumReader = new ReflectDatumReader< T >(schema);
        DataFileStream< T > reader = new DataFileStream< T >(inputStream, reflectDatumReader);
        Object activity = reader.next();
        reader.close();
        inputStream.close();
        return ( T ) activity;
    }

}
