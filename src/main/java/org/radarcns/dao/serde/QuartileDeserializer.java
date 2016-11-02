package org.radarcns.dao.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Francesco Nobilia on 28/10/2016.
 */
public class QuartileDeserializer extends JsonDeserializer<List<Double>> {
    @Override
    public List<Double> deserialize(JsonParser jsonparser,
        DeserializationContext deserializationcontext) throws IOException {

        ObjectCodec oc = jsonparser.getCodec();
        JsonNode node = oc.readTree(jsonparser);

        List<Double> list = new LinkedList<>();
        list.add(node.get(0).get("25").asDouble());
        list.add(node.get(1).get("50").asDouble());
        list.add(node.get(2).get("75").asDouble());

        return list;
    }

}
