package org.radarcns.util;


import com.fasterxml.jackson.databind.JsonNode;

import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.restapi.avro.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.ws.rs.core.Response;

/**
 * Created by Francesco Nobilia on 14/11/2016.
 */
public class ResponseHandler {

    private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    public static Response getJsonResponse(SpecificRecord obj, int size, String sensor) throws IOException {

        int code = 200;

        if(obj == null){
            code = 204;
            obj = new Message("No data for this input");
        }

        JsonNode json = AvroConverter.avroToJsonNode(obj,sensor);

        logger.info("[{}] {} records",code,size);

        return Response.status(code).entity(json).build();
    }

    public static Response getJsonErrorResponse(String message){
        SpecificRecord obj = new Message(message);

        JsonNode json = AvroConverter.avroToJsonNode(obj);

        if(json == null){
            logger.info("[{}] {}",500,json);
            return Response.status(500).entity("Internal error!").build();
        }
        else{
            logger.info("[{}] {}",500,json);
            return Response.status(500).entity(json).build();
        }
    }
}
