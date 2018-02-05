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

package org.radarcns.webapp.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.radarcns.auth.authorization.Permission.Operation.READ;
import static org.radarcns.webapp.util.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SOURCES;
import static org.radarcns.webapp.util.BasePath.SOURCE;
import static org.radarcns.webapp.util.BasePath.SPECIFICATION;
import static org.radarcns.webapp.util.BasePath.STATE;
import static org.radarcns.webapp.util.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.util.Parameter.SOURCE_ID;
import static org.radarcns.webapp.util.Parameter.SOURCE_TYPE;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import com.mongodb.MongoClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.radarcns.auth.NeedsPermission;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.auth.authorization.Permission.Entity;
import org.radarcns.dao.SourceDataAccessObject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.monitor.Monitors;
import org.radarcns.restapi.header.EffectiveTimeFrame;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.spec.SourceSpecification;
import org.radarcns.restapi.subject.Subject;
import org.radarcns.webapp.validation.Alphanumeric;

/**
 * SourceDefinition web-app. Function set to access source information.
 */
@Provider
@Path("/" + SOURCE)
public class SourceEndPoint {
    @Inject
    private MongoClient mongoClient;

    @Inject
    private ManagementPortalClient mpClient;

    //--------------------------------------------------------------------------------------------//
    //                                       STATE FUNCTIONS                                      //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the status of the given source.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/" + STATE + "/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
    @Operation(summary = "Return a SourceDefinition values",
            description = "Using the source sensors values arrived within last 60sec, it computes "
                    + "the"
                    + "sender status for the given subjectID and sourceID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a source.avsc object containing last"
            + "computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
    public Source getLastComputedSourceStatusJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
        org.radarcns.managementportal.Subject sub = mpClient.getSubject(subjectId);

        String sourceType = SourceDataAccessObject.getSourceType(sourceId, mongoClient);

        if (sourceType != null) {
            return Monitors.getInstance().getState(mongoClient, subjectId, sourceId, sourceType);
        } else {
            Optional<org.radarcns.managementportal.Source> source = sub.getSources().stream()
                    .filter(s -> s.getSourceId().equals(sourceId))
                    .findAny();

            return new Source(sourceId,
                    source.map(s -> (s.getSourceTypeProducer() + "_" + s.getSourceTypeModel())
                            .toUpperCase()).orElse("UNKNOWN"),
                    null);
        }
    }

    //--------------------------------------------------------------------------------------------//
    //                               SOURCE SPECIFICATION FUNCTIONS                               //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns the specification of the given source.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/" + SPECIFICATION + "/{" + SOURCE_TYPE + "}")
    @Operation(summary = "Return a SourceDefinition specification",
            description = "Return the data specification of all on-board sensors for the given"
                    + "source type")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a source_specification.avsc object"
            + "containing last computed status")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Source type not found")
    @NeedsPermission(entity = Entity.SOURCE, operation = READ)
    public SourceSpecification getSourceSpecificationJson(
            @Alphanumeric @PathParam(SOURCE_TYPE) String sourceType) {
        SourceSpecification sourceSpec = Monitors.getInstance().getSpecification(sourceType);
        if (sourceSpec == null) {
            throw new NotFoundException("Source type " + sourceType + " not found");
        }
        return sourceSpec;
    }

    //--------------------------------------------------------------------------------------------//
    //                                         ALL SOURCES                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all known sources for the given subject.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/" + GET_ALL_SOURCES + "/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return a User value",
            description = "Return all known sources associated with the give subjectID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the "
            + "body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a subject.avsc object")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
    public Subject getAllSourcesJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId) throws IOException {
        // TODO: get sources data from MP
        mpClient.getSubject(subjectId);
        if (SubjectDataAccessObject.exist(subjectId, mongoClient)) {
            return SourceDataAccessObject.findAllSourcesByUser(subjectId, mongoClient);
        } else {
            String now = Instant.now().toString();
            return new Subject(subjectId, false, new EffectiveTimeFrame(now, now),
                    Collections.emptyList());
        }
    }
}
