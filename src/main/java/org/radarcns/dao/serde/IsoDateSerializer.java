package org.radarcns.dao.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * Created by Francesco Nobilia on 28/10/2016.
 */
public class IsoDateSerializer extends JsonSerializer<DateTime> {
    @Override
    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException{
        String isoDate = ISODateTimeFormat.dateTime().print(value);
        jgen.writeRaw("ISODATE(\"" + isoDate + "\")");
    }
}