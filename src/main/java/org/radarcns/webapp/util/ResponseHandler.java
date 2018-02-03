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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.util.AvroConverter;
import org.radarcns.webapp.exception.StatusMessage;

/**
 * Generic response handler.
 */
public class ResponseHandler {
    public static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";

    /**
     * It serialises the {@link Dataset} in input in JSON and sets the suitable status code.
     * @param dataset request result
     * @return the response content formatted in JSON
     **/
    public static Response getJsonResponse(Dataset dataset) {
        JsonNode json = AvroConverter.avroToJsonNode(dataset);
        return Response.ok()
                .entity(json)
                .build();
    }

    /**
     * It serialises the {@code SpecificRecord} in input in JSON and sets the suitable status code.
     * @param obj request result
     * @return the response content formatted in JSON
     **/
    public static Response getJsonResponse(SpecificRecord obj) {
        if (obj == null) {
            return Response.noContent().build();
        } else {
            JsonNode json = AvroConverter.avroToJsonNode(obj);
            return Response.ok()
                    .entity(json)
                    .build();
        }
    }

    /**
     * It sets the status code and serialises the given {@code SpecificRecord} in bytes array.
     * @param obj request result
     * @return the response content formatted in AVRO
     **/
    public static Response getAvroResponse(SpecificRecord obj)
            throws IOException {
        if (obj == null) {
            return Response.noContent().build();
        } else {
            byte[] array = AvroConverter.avroToAvroByte(obj);
            return Response.ok(array, "avro/binary").build();
        }
    }

    public static ResponseBuilder jsonStatus(
            MediaType type, Status status, String error, String message) {
        ResponseBuilder builder = Response.status(status);
        if (type == null || type.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            builder.header("Content-Type", APPLICATION_JSON_UTF8)
                    .entity(new StatusMessage(error, message));
        }
        return builder;
    }
}
