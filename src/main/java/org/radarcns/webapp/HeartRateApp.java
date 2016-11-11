package org.radarcns.webapp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.bson.Document;
import org.radarcns.avro.HeartRate;
import org.radarcns.avro.HeartRateDataSet;
import org.radarcns.avro.Statistic;
import org.radarcns.dao.MongoHeartRateDAO;
import org.radarcns.util.AvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import io.swagger.annotations.Api;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/HeartRate")
public class HeartRateApp {

    public static Logger logger = LoggerFactory.getLogger(HeartRateApp.class);

    @Context ServletContext context;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/avg/{userID}")
    public Response avgUser(@PathParam("userID") String userID) {

        HeartRateDataSet hrd = MongoHeartRateDAO.avgByUser(userID,getCollection());

        String json = AvroConverter.getJsonString(hrd);

        logger.trace(json);

        return Response.status(200).entity(json).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/avg/{userID}/{start}/{end}")
    public Response avgUserWindow(
            @PathParam("userID") String userID,
            @PathParam("start") long start,
            @PathParam("end") long end) {

        HeartRate hr = MongoHeartRateDAO.avgByUserWindow(userID,start,end,getCollection());

        String json = AvroConverter.getJsonString(hr);

        logger.trace(json);

        return Response.status(200).entity(json).build();
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

    private MongoCollection<Document> getCollection(){
        MongoClient mongoClient = (MongoClient) context.getAttribute("MONGO_CLIENT");
        MongoDatabase database = mongoClient.getDatabase("hotstorage");

        return database.getCollection("heartrate");
    }

}
