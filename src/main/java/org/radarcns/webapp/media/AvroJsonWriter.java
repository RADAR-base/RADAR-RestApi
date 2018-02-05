package org.radarcns.webapp.media;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.util.RadarConverter;

/** Converts Avro SpecificRecord objects to plain JSON format. */
@Provider
@Produces(APPLICATION_JSON)
@Singleton
public class AvroJsonWriter implements MessageBodyWriter<SpecificRecord> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return SpecificRecord.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(SpecificRecord record, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        RadarConverter.AVRO_JSON_WRITER.writeValue(entityStream, record);
    }
}
