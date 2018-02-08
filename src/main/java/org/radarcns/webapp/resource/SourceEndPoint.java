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
import static org.radarcns.webapp.resource.BasePath.AVRO_BINARY;
import static org.radarcns.webapp.resource.BasePath.PROJECTS;
import static org.radarcns.webapp.resource.BasePath.SOURCES;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;
import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.auth.authorization.Permission.Entity;
import org.radarcns.domain.restapi.Source;
import org.radarcns.service.SourceService;
import org.radarcns.webapp.filter.Authenticated;
import org.radarcns.webapp.validation.Alphanumeric;

/**
 * SourceDefinition web-app. Function set to access source information.
 */
@Authenticated
@Path("/" + PROJECTS)
public class SourceEndPoint {

    @Inject
    private SourceService sourceService;

    //--------------------------------------------------------------------------------------------//
    //                                         ALL SOURCES                                        //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all known sources for the given subject.
     */
    @GET
    @Produces({APPLICATION_JSON, AVRO_BINARY})
    @Path("/{" + PROJECT_NAME + "}" + "/" + SUBJECTS + "/{" + SUBJECT_ID + "}" + "/" + SOURCES)
    @Operation(summary = "Return all the sources used by a subject",
            description = "Return all known sources associated with the give subjectID")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a subject.avsc object")
    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
    public List<Source> getAllSourcesJson(
            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId) throws IOException {

        return sourceService.getAllSourcesOfSubject(projectName, subjectId);
    }

//    //------------------------------------------------------------------------------------------//
//    //                                       STATE FUNCTIONS                                    //
//    //------------------------------------------------------------------------------------------//
//
//    /**
//     * JSON function that returns the status of the given source.
//     */
//    @GET
//    @Produces({APPLICATION_JSON, AVRO_BINARY})
//    @Path("/" + STATE + "/{" + PROJECT_NAME + "}/{" + SUBJECT_ID + "}/{" + SOURCE_ID + "}")
//    @Operation(summary = "Return a SourceDefinition values",
//            description = "Using the source sensors values arrived within last 60sec, it computes "
//                    + "the"
//                    + "sender status for the given subjectID and sourceID")
//    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
//            + "there is a message.avsc object with more details")
//    @ApiResponse(responseCode = "200", description = "Return a source.avsc object containing last"
//            + "computed status")
//    @ApiResponse(responseCode = "401", description = "Access denied error occurred")
//    @ApiResponse(responseCode = "403", description = "Not Authorised error occurred")
//    @ApiResponse(responseCode = "404", description = "Subject cannot be found")
//    @NeedsPermissionOnSubject(entity = Entity.SOURCE, operation = READ)
//    public Source getLastComputedSourceStatusJson(
//            @Alphanumeric @PathParam(PROJECT_NAME) String projectName,
//            @Alphanumeric @PathParam(SUBJECT_ID) String subjectId,
//            @Alphanumeric @PathParam(SOURCE_ID) String sourceId) throws IOException {
//        org.radarcns.domain.managementportal.Subject sub = mpClient.getSubject(subjectId);
//
//        String sourceType = sourceDataAccessObject.getSourceType(sourceId, mongoClient);
//
//        if (sourceType != null) {
//            return Monitors.getInstance().getState(mongoClient, subjectId, sourceId, sourceType);
//        } else {
//            Optional<org.radarcns.domain.managementportal.Source> source = sub.getSources().stream()
//                    .filter(s -> s.getSourceId().equals(sourceId))
//                    .findAny();
//
//            return new Source(sourceId,
//                    source.map(s -> (s.getSourceTypeProducer() + "_" + s.getSourceTypeModel())
//                            .toUpperCase()).orElse("UNKNOWN"));
//        }
//    }

}
