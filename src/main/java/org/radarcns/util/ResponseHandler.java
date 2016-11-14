package org.radarcns.util;

import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

/**
 * Created by Francesco Nobilia on 14/11/2016.
 */
public class ResponseHandler {

    private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    public static Response getJsonResponse(SpecificRecord obj){

        int code = 200;

        if(obj == null){
            code = 204;
            obj = new Message("No data for this input");
        }

        String json = AvroConverter.avroObjToJsonString(obj);

        logger.info("[{}] {}",code,json);

        return Response.status(code).entity(json).build();
    }
}
