/*
 * Copyright 2016 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.webapp.util;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.restapi.app.Application;
import org.radarcns.restapi.avro.Message;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.restapi.subject.Cohort;
import org.radarcns.restapi.subject.Subject;
import org.radarcns.util.AvroConverter;
import org.radarcns.webapp.exception.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic response handler.
 */
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);

    /**
     * It serialises the {@link Dataset} in input in JSON and sets the suitable status code.
     * @param request HTTP request that has to be served
     * @param dataset request result
     * @return the response content formatted in JSON
     **/
    public static Response getJsonResponse(HttpServletRequest request, Dataset dataset)
            throws IOException {
        Status status = Status.OK;
        int size = 0;
        SpecificRecord obj = dataset;

        if (dataset.getDataset().isEmpty()) {
            status = NO_CONTENT;
            obj = new Message("No data for this input");
        } else {
            size = dataset.getDataset().size();
        }

        JsonNode json = AvroConverter.avroToJsonNode(obj);

        LOGGER.debug("{}", json);
        LOGGER.debug("[{}] {} records", status.getStatusCode(), size);

        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        return Response.status(status.getStatusCode()).entity(json).build();
    }

    /**
     * It serialises the {@code SpecificRecord} in input in JSON and sets the suitable status code.
     * @param request HTTP request that has to be served
     * @param obj request result
     * @return the response content formatted in JSON
     **/
    public static Response getJsonResponse(HttpServletRequest request, SpecificRecord obj)
            throws IOException {
        Status status = getStatus(obj);
        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        if (status == NO_CONTENT) {
            return Response.noContent().build();
        }

        JsonNode json = AvroConverter.avroToJsonNode(obj);
        return Response.status(status.getStatusCode())
                .entity(json)
                .build();
    }

    //TODO return Status.BAD_REQUEST in case of parameter that does not respect regex.
    /**
     * It sets the suitable status code and return a JSON message containing the input String.
     * @param request HTTP request that has to be served
     * @param message to provide more information about the error
     * @return the response content formatted in JSON
     **/
    public static Response getJsonErrorResponse(HttpServletRequest request, String message) {
        Status status = Status.INTERNAL_SERVER_ERROR;
        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        LOGGER.debug("[{}] {}", status.getStatusCode(), message);
        return Response.status(status.getStatusCode())
                .entity(new StatusMessage("server_error", message, null))
                .build();
    }

    /**
     * It sets the suitable status code and return a JSON message containing the input String.
     * @param request HTTP request that has to be served
     * @param message to provide more information about the error
     * @return the response content formatted in JSON
     **/
    public static Response getJsonAccessDeniedResponse(HttpServletRequest request,
                                                       String message) {
        Status status = Status.UNAUTHORIZED;
        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        LOGGER.debug("[{}] {}", status.getStatusCode(), message);
        return Response.status(status.getStatusCode())
                .entity(new StatusMessage("access_denied", message))
                .build();
    }

    /**
     * It sets the suitable status code and return a JSON message containing the input String.
     * @param request HTTP request that has to be served
     * @param message to provide more information about the error
     * @return the response content formatted in JSON
     **/
    public static Response getJsonNotAuthorizedResponse(HttpServletRequest request,
                                                        String message) {
        Status status = Status.FORBIDDEN;
        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        SpecificRecord obj = new Message(message);

        JsonNode json = AvroConverter.avroToJsonNode(obj);

        LOGGER.debug("[{}] {}", status.getStatusCode(), json);

        return Response.status(status.getStatusCode())
                .entity(new StatusMessage("forbidden", "Forbidden!", json))
                .build();
    }


    /**
     * It sets the status code and serialises the given {@code SpecificRecord} in bytes array.
     * @param request HTTP request that has to be served
     * @param obj request result
     * @return the response content formatted in AVRO
     **/
    public static Response getAvroResponse(HttpServletRequest request, SpecificRecord obj)
            throws IOException {
        Status status = getStatus(obj);
        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        switch (status) {
            case OK:
                byte[] array = AvroConverter.avroToAvroByte(obj);
                return Response.ok(array, MediaType.APPLICATION_OCTET_STREAM_TYPE).build();
            case NO_CONTENT:
                return Response.noContent().build();
            default:
                return Response.serverError().build();
        }
    }


    //TODO return 400 in case of parameter does not respect regex
    /**
     * It sets the status code.
     * @param request HTTP request that has to be served
     * @return the response error code
     **/
    public static Response getAvroErrorResponse(HttpServletRequest request) {
        LOGGER.info("[{}] {}", Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                request.getRequestURI());
        return Response.serverError().build();
    }

    private static Status getStatus(SpecificRecord obj) throws UnsupportedEncodingException {
        if (obj == null) {
            return NO_CONTENT;
        }

        switch (obj.getSchema().getName()) {
            case "Cohort" :
                if (((Cohort) obj).getSubjects().isEmpty()) {
                    return NO_CONTENT;
                }
                break;
            case "Dataset" :
                if (((Dataset) obj).getDataset().isEmpty()) {
                    return NO_CONTENT;
                }
                break;
            case "Application" :
                if (((Application) obj).getServerStatus() == null) {
                    return NO_CONTENT;
                }
                break;
            case "Subject" :
                if (((Subject) obj).getSubjectId() == null) {
                    return NO_CONTENT;
                }
                break;
            case "Source" : break;
            case "SourceSpecification" : break;
            default: throw new UnsupportedEncodingException("SpecificRecord "
                + obj.getSchema().getName() + " is not supported yet");
        }
        return Status.OK;
    }

    /**
     * It sets the suitable status code and return a JSON message containing the input String.
     * @param request HTTP request that has to be served
     * @param message to provide more information about the error
     * @return the response content formatted in JSON
     **/
    public static Response getJsonNotFoundResponse(HttpServletRequest request,
            String message) {
        Status status = Status.NO_CONTENT;
        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        SpecificRecord obj = new Message(message);

        JsonNode json = AvroConverter.avroToJsonNode(obj);

        LOGGER.debug("[{}] {}", status.getStatusCode(), json);

        return Response.status(status.getStatusCode())
                .entity(new StatusMessage("notfound", "Not Found!", json))
                .build();
    }
}
