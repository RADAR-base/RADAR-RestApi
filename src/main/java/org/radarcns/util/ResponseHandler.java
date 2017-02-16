package org.radarcns.util;


import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

    public static Response getAvroResponse(HttpServletRequest request, SpecificRecord obj) throws IOException {
        Status status = Status.OK;
        byte[] array = new byte[1];

        logger.info(obj.getSchema().getName());

        if(obj.getSchema().getName().equals("Dataset") && ((Dataset) obj).getDataset().isEmpty()){
            status = Status.NO_CONTENT;
        }
        else{
            int size = ((Dataset) obj).getDataset().size();
            logger.debug("[{}] {} records", status.getStatusCode(), size);

            array = AvroConverter.avroToAvroByte(obj);
            logger.info("Array of size {}", array.length);
        }

        logger.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        switch (status){
            case OK: return Response.ok(array, MediaType.APPLICATION_OCTET_STREAM_TYPE).build();
            case NO_CONTENT: return Response.noContent().build();
        }

        return Response.serverError().build();
    }

    public static Response getAvroErrorResponse(HttpServletRequest request){
        logger.info("[{}] {}", 500, request.getRequestURI());
        return Response.serverError().build();
    }
}
