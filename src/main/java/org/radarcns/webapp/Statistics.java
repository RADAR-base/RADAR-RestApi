package org.radarcns.webapp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.radarcns.dao.MongodbDAO;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by Francesco Nobilia on 18/10/2016.
 */
@Api
@Path("/")
public class Statistics {
    @Context ServletContext context;

    @GET
    @Path("/checkPatientDevices/{userID}")
    public Response checkPatientDevices(@PathParam("userID") String userID) {
        MongoClient mongoClient = (MongoClient) context.getAttribute("MONGO_CLIENT");
        MongoDatabase database = mongoClient.getDatabase(context.getInitParameter("MONGODB_DB"));
        MongoCollection<Document> collection = database.getCollection("heartrate");

        String json = MongodbDAO.findDocumentByUser(userID,collection);

        return Response.status(200).entity(json).build();
    }

}
