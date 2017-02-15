package org.radarcns.util;


import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.avro.restapi.avro.Message;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 14/11/2016.
 */
public class ResponseHandler {

    private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    public static Response getJsonResponse(HttpServletRequest request, Dataset dataset,
        String sensor) throws IOException {
        int code = 200;
        int size = 0;
        SpecificRecord obj = dataset;

        if(dataset.getDataset().isEmpty()){
            code = 204;
            obj = new Message("No data for this input");
        }
        else{
            size = dataset.getDataset().size();
        }

        JsonNode json = AvroConverter.avroToJsonNode(obj,sensor);

        logger.debug("{}",json.toString());
        logger.debug("[{}] {} records",code,size);

        logger.info("[{}] {}", code, request.getRequestURI());

        return Response.status(code).entity(json).build();
    }

    public static Response getJsonResponse(HttpServletRequest request,SpecificRecord obj)
        throws IOException {
        JsonNode json = AvroConverter.avroToJsonNode(obj);

        logger.debug("{}",json.toString());
        logger.debug("[{}] {}",200,obj);

        logger.info("[{}] {}", 200, request.getRequestURI());

        return Response.status(200).entity(json).build();
    }

    public static Response getJsonErrorResponse(HttpServletRequest request, String message){
        SpecificRecord obj = new Message(message);

        JsonNode json = AvroConverter.avroToJsonNode(obj);

        if(json == null){
            logger.debug("[{}] {}",500,json);
            logger.info("[{}] {}", 500, request.getRequestURI());
            return Response.status(500).entity("Internal error!").build();
        }
        else{
            logger.debug("[{}] {}",500,json);
            logger.info("[{}] {}", 500, request.getRequestURI());
            return Response.status(500).entity(json).build();
        }
    }

    public static Response getAvroResponse(HttpServletRequest request, Dataset dataset) throws IOException {
        int code = 200;
        int size = 0;
        SpecificRecord obj = dataset;

        if(dataset.getDataset().isEmpty()){
            code = 204;
            obj = new Message("No data for this input");
        }
        else{
            size = dataset.getDataset().size();
        }

        byte[] array = AvroConverter.avroToAvroByte(obj);

        logger.debug("Array of size {}", array.length);
        logger.debug("[{}] {} records", code, size);

        logger.info("[{}] {}", code, request.getRequestURI());

        return Response.status(code).entity(array).build();
    }

    public static Response getAvroResponse(HttpServletRequest request, SpecificRecord obj)
        throws IOException {
        byte[] array = AvroConverter.avroToAvroByte(obj);

        logger.debug("Array of size {}", array.length);
        logger.debug("[{}] {}",200,obj);

        logger.info("[{}] {}", 200, request.getRequestURI());

        return Response.status(200).entity(array).build();
    }

    public static Response getAvroErrorResponse(HttpServletRequest request, String message){
        SpecificRecord obj = new Message(message);

        try {
            byte[] array = AvroConverter.avroToAvroByte(obj);

            logger.debug("[{}] Array of size {}", 500, array.length);
            logger.info("[{}] {}", 500, request.getRequestURI());
            return Response.status(500).entity(array).build();
        } catch (IOException e) {
            logger.debug("[{}] Error generating Avro message", 500);
            logger.info("[{}] {}", 500, request.getRequestURI());
            return Response.status(500).entity("Internal error!").build();
        }
    }
}
