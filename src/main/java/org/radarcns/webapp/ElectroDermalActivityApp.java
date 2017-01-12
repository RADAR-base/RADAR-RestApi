package org.radarcns.webapp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.Unit;
import org.radarcns.dao.mongo.ElectrodermalActivityDAO;
import org.radarcns.dao.mongo.util.MongoDAO;
import org.radarcns.security.Param;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/EDA")
public class ElectroDermalActivityApp {

    private static Logger logger = LoggerFactory.getLogger(ElectroDermalActivityApp.class);

    private final String sensorName = "electrodermal_activity";

    @Context private ServletContext context;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return an ElectroDermal Activity values",
            notes = "Return the last seen ElectroDermal Activity value of type stat for the given userID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing last seen electrodermal_activity.avsc value for the required statistic function")})
    public Response getRealTimeUser(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoDAO.Stat stat) {
        try {
            Param.isValidInput(userID, sourceID);

            Dataset eda = ElectrodermalActivityDAO.getInstance().valueRTByUserSource(userID, sourceID, Unit.microsiemens, stat, context);

            if (eda.getDataset().isEmpty()) {
                logger.info("No data for the user {} with source {}", userID, sourceID);
            }

            return ResponseHandler.getJsonResponse(eda, sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse("Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a dataset of ElectroDermal Activity values",
            notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all available electrodermal_activity.avsc values for the required statistic function")})
    public Response getAllByUser(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoDAO.Stat stat) {
        try {
            Param.isValidInput(userID, sourceID);

            Dataset eda = ElectrodermalActivityDAO.getInstance().valueByUserSource(userID, sourceID, Unit.microsiemens, stat, context);

            if (eda.getDataset().isEmpty()) {
                logger.info("No data for the user {} with source {}", userID, sourceID);
            }

            return ResponseHandler.getJsonResponse(eda, sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse("Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Return a dataset of ElectroDermal Activity values",
            notes = "Return a dataset of type stat for the given userID and sourceID with data belonging to the time window [start - end]")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all electrodermal_activity.avsc values belonging to the time window [start - end] for the required statistic function")})
    public Response getByUserForWindow(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoDAO.Stat stat,
            @PathParam("start") long start,
            @PathParam("end") long end) {
        try {
            Param.isValidInput(userID, sourceID);

            Dataset eda = ElectrodermalActivityDAO.getInstance().valueByUserSourceWindow(userID, sourceID, Unit.microsiemens, stat, start, end, context);

            if (eda.getDataset().isEmpty()) {
                logger.info("No data for the user {} with source {}", userID, sourceID);
            }

            return ResponseHandler.getJsonResponse(eda, sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            return ResponseHandler.getJsonErrorResponse("Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }

    //TODO
    /*@GET
    @Produces("avro/binary")
    @Path("avro/checkPatientDevices/{userID}")
    public Response checkPatientDevicesAvro(@PathParam("userID") String userID) {

        final String mockValue = userID;

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                List<Double> list = new LinkedList<>();
                list.add(new Double(6d));

                Statistic stat = new Statistic(mockValue,mockValue,1d,2d,3d,4d,5d,list,7d);

                DatumWriter<Statistic> datumWriter = new SpecificDatumWriter<>(Statistic.class);
                Encoder encoder = EncoderFactory.get().binaryEncoder(os, null);
                datumWriter.write(stat, encoder);
                encoder.flush();
            }
        };

        return Response.status(200).entity(stream).build();

        *//*HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet("localhost:8080/api/avro/checkPatientDevices/user");
        HttpResponse response = client.execute(get);
        byte[] content = EntityUtils.toByteArray(response.getEntity());
        InputStream is = new ByteArrayInputStream(content);
        DatumReader<T> reader = new SpecificDatumReader<>(Statistic.class);
        Decoder decoder = DecoderFactory.get().binaryDecoder(is, null);
        Statistic fromServer = reader.read(null, decoder);
        *//*
    }*/

}
