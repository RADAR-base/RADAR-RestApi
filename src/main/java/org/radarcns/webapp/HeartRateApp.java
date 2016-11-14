package org.radarcns.webapp;

import org.radarcns.avro.HeartRate;
import org.radarcns.avro.HeartRateDataSet;
import org.radarcns.dao.MongoDAO;
import org.radarcns.dao.MongoHeartRateDAO;
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
@Path("/HeartRate")
public class HeartRateApp {

    private static Logger logger = LoggerFactory.getLogger(HeartRateApp.class);

    @Context ServletContext context;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/RT/{stat}/{userID}")
    @ApiOperation(
            value = "Return a Heart Rate values",
            notes = "Return the last seen Heart rate value of type stat for the given userID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a heart_rate.avsc object")})
    public Response getRealTimeUser(@PathParam("userID") String userID,@PathParam("stat") MongoDAO.Stat stat) {

        HeartRate hr = MongoHeartRateDAO.valueRTByUser(userID, stat, context);

        if(hr == null){
            logger.info("No data for the user {}",userID);
        }

        return ResponseHandler.getJsonResponse(hr);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}")
    @ApiOperation(
            value = "Return a dataset of Heart Rate values",
            notes = "Return a dataset for the given userID of type stat")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a heart_rate_dataset.avsc object")})
    public Response getAllByUser(@PathParam("userID") String userID, @PathParam("stat") MongoDAO.Stat stat) {

        HeartRateDataSet hrd = MongoHeartRateDAO.valueByUser(userID, stat, context);

        if(hrd == null){
            logger.info("No data for the user {}",userID);
        }

        return ResponseHandler.getJsonResponse(hrd);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{stat}/{userID}/{start}/{end}")
    @ApiOperation(
            value = "Return a dataset of Heart Rate values",
            notes = "Return a dataset of type stat for the given userID with data belonging to the time window [start - end]")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body" +
                    "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a heart_rate_dataset.avsc object")})
    public Response getByUserForWindow(
            @PathParam("userID") String userID,
            @PathParam("start") long start,
            @PathParam("end") long end,
            @PathParam("stat") MongoDAO.Stat stat) {

        HeartRateDataSet hrd = MongoHeartRateDAO.valueByUserWindow(userID, stat, start, end, context);

        if(hrd == null){
            logger.info("No data for the user {}",userID);
        }

        return ResponseHandler.getJsonResponse(hrd);
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
