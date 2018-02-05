package org.radarcns.webapp.media;

import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.glassfish.jersey.internal.inject.PerThread;

@Provider
@Produces(AVRO_BINARY)
@PerThread
public class AvroBinaryWriter implements MessageBodyWriter<SpecificRecord> {
    private BinaryEncoder encoder;

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
        DatumWriter writer = new SpecificDatumWriter(record.getSchema());
        encoder = EncoderFactory.get().binaryEncoder(entityStream, encoder);

        //noinspection unchecked
        writer.write(record, encoder);

        encoder.flush();
    }
}
