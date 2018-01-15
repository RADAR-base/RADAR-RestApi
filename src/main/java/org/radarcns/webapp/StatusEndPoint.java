package org.radarcns.webapp;

import static org.radarcns.auth.authorization.Permission.MEASUREMENT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.security.utils.SecurityUtils.getJWT;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.status.CsvData;
import org.radarcns.status.CsvDataController;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/status")
public class StatusEndPoint {

    private static final String CSV_FILE_PATH = "/usr/local/tomcat/bin/radar/bins.csv";

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusEndPoint.class);

    @Context
    private HttpServletRequest request;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + "hdfs")
    @Operation(
            summary = "Return a list of summary of records received by the server",
            description = "Reads and displays the bins.csv file generated by HDFS restructure"
                    + " script")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "An error occurs while executing, "
                    + "in the body there is a message.avsc object with more details"),
            @ApiResponse(responseCode = "204", description = "No value for the given parameters, "
                    + "in the body there is a message.avsc object with more details"),
            @ApiResponse(responseCode = "200", description = "Return a list of summary of records"),
            @ApiResponse(responseCode = "401", description = "Access denied error occured"),
            @ApiResponse(responseCode = "403", description = "Not Authorised error occured")})
    public Response getJsonData() {
        try {
            checkPermission(getJWT(request), MEASUREMENT_READ);
            List<CsvData> data = getListFromCsvFile(CSV_FILE_PATH);
            HashSet<String> topics = CsvDataController.getAllTopics(data);
            return Response.status(Response.Status.OK).entity(topics + "\n"
                    + CsvDataController.getDataOfTopics(data)).build();
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (FileNotFoundException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonErrorResponse(request, "The bins.csv file could "
                    + "not be found. Please make sure its in the right directory.");
        } catch (IOException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonErrorResponse(request, "The bins.csv file could "
                    + "not be read. Please make sure its in the right format.");
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact "
                    + "the service administrator.");
        }
    }


    @GET
    @Produces("text/csv")
    @Path("/" + "hdfs")
    @Operation(
            summary = "Return a list of summary of records received by the server",
            description = "Reads and displays the bins.csv file generated by HDFS restructure "
                    + "script")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "An error occurs while executing, "
                    + "in the body there is a message.avsc object with more details"),
            @ApiResponse(responseCode = "204", description = "No value for the given parameters, "
                    + "in the body there is a message.avsc object with more details"),
            @ApiResponse(responseCode = "200", description = "Return a list of summary of records"),
            @ApiResponse(responseCode = "401", description = "Access denied error occured"),
            @ApiResponse(responseCode = "403", description = "Not Authorised error occured")})
    public Response getData() {
        try {
            checkPermission(getJWT(request), MEASUREMENT_READ);
            List<CsvData> data = getListFromCsvFile(CSV_FILE_PATH);
            return Response.status(Response.Status.OK).entity(data).build();
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (FileNotFoundException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonErrorResponse(request, "The bins.csv file could "
                    + "not be found. Please make sure its in the right directory.");
        } catch (IOException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonErrorResponse(request, "The bins.csv file could "
                    + "not be read. Please make sure its in the right format.");
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact "
                    + "the service administrator.");
        }
    }

    private List<CsvData> getListFromCsvFile(String csvFileToRead) throws IOException {
        List<CsvData> dataList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(csvFileToRead));
        String line;

        br.readLine();
        while ((line = br.readLine()) != null) {

            String[] dataCsv = line.split(",");

            CsvData dataObj = new CsvData();

            dataObj.setTopic(dataCsv[0]);
            dataObj.setDevice(dataCsv[1]);
            dataObj.setTimestamp(dataCsv[2]);
            dataObj.setCount(dataCsv[3]);

            dataList.add(dataObj);
        }

        return dataList;
    }
}
