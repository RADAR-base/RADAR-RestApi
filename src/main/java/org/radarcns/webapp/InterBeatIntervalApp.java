package org.radarcns.webapp;

import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.dao.mongo.InterBeatIntervalDAO;
import org.radarcns.dao.mongo.util.MongoDAO;
import org.radarcns.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/IBI")
public class InterBeatIntervalApp {

    private static Logger logger = LoggerFactory.getLogger(InterBeatIntervalApp.class);

    private final String sensorName = "inter_beat_interval";

    @Context ServletContext context;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/RT/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return an Inter Beat Interval values",
            notes = "Return the last seen Inter Beat Interval value of type stat for the given userID and sourceID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing last seen inter_beat_interval.avsc value for the required statistic function")})
    public Response getRealTimeUser(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoDAO.Stat stat) {
        try {
            Dataset ibi = InterBeatIntervalDAO.getInstance().valueRTByUserSource(userID, sourceID, stat, context);

            if (ibi.getDataset().isEmpty()) {
                logger.info("No data for the user {}", userID);
            }

            return ResponseHandler.getJsonResponse(ibi,ibi.getDataset().size(),sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            return ResponseHandler.getJsonErrorResponse("Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}")
    @ApiOperation(
            value = "Return a dataset of Inter Beat Interval values",
            notes = "Return a dataset for the given userID and sourceID of type stat")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all available inter_beat_interval.avsc values for the required statistic function")})
    public Response getAllByUser(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("stat") MongoDAO.Stat stat) {
        try {
            Dataset ibi = InterBeatIntervalDAO.getInstance().valueByUserSource(userID, sourceID, stat, context);

            if (ibi.getDataset().isEmpty()) {
                logger.info("No data for the user {} with source {}", userID, sourceID);
            }

            return ResponseHandler.getJsonResponse(ibi,ibi.getDataset().size(),sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
            return ResponseHandler.getJsonErrorResponse("Your request cannot be completed. If this error persists, please contact the service administrator.");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{sourceID}/{start}/{end}")
    @ApiOperation(
            value = "Return a dataset of Inter Beat Interval values",
            notes = "Return a dataset of type stat for the given userID and sourceID with data belonging to the time window [start - end]")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a dataset.avsc object containing all inter_beat_interval.avsc values belonging to the time window [start - end] for the required statistic function")})
    public Response getByUserForWindow(
            @PathParam("userID") String userID,
            @PathParam("sourceID") String sourceID,
            @PathParam("start") long start,
            @PathParam("end") long end,
            @PathParam("stat") MongoDAO.Stat stat) {
        try {
            Dataset ibi = InterBeatIntervalDAO.getInstance().valueByUserSourceWindow(userID, sourceID, stat, start, end, context);

            if (ibi.getDataset().isEmpty()) {
                logger.info("No data for the user {} with source {}", userID, sourceID);
            }

            return ResponseHandler.getJsonResponse(ibi,ibi.getDataset().size(),sensorName);
        }
        catch (Exception e){
            logger.error(e.getMessage(),e);
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
