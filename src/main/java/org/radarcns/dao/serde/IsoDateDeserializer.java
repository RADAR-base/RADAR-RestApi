package org.radarcns.dao.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Francesco Nobilia on 28/10/2016.
 */
public class IsoDateDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jsonparser,
        DeserializationContext deserializationcontext) throws IOException {

        ObjectCodec oc = jsonparser.getCodec();
        JsonNode node = oc.readTree(jsonparser);
        String dateValue = node.get("$date").asText();

        Date date = new Date(Long.valueOf(dateValue));

        return date;
    }

}
