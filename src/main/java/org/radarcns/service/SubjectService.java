package org.radarcns.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.radarcns.domain.managementportal.Subject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.webapp.exception.BadGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private ManagementPortalClient managementPortalClient;

    private SourceService sourceService;

    /**
     * Default constructor.
     * Injects all dependencies.
     * @param managementPortalClient instance
     * @param sourceService instance
     */
    @Inject
    public SubjectService(ManagementPortalClient managementPortalClient, SourceService
            sourceService) {
        this.managementPortalClient = managementPortalClient;
        this.sourceService = sourceService;
    }

    /**
     * Checks whether given source-id is available in the sources available for the subject.
     * @param subjectId of subject
     * @param sourceId of source
     * @return {@code true} if available.
     * @throws IOException when unable to process the request.
     */
    public boolean checkSourceAssignedToSubject(String subjectId, String sourceId) throws
            IOException {
        Subject subject = managementPortalClient.getSubject(subjectId);
        if (subject.getSources().stream().filter(p -> p.getSourceId().equals(sourceId))
                .collect(Collectors.toList()).isEmpty()) {
            LOGGER.error("Cannot find source-id " + sourceId + "for subject" + subject.getId());
            throw new BadRequestException("Source-id " + sourceId + " is not available for subject "
                    + subject.getId());
        }
        return true;
    }

    private org.radarcns.domain.restapi.Subject buildSubject(Subject subject) throws IOException {
        return new org.radarcns.domain.restapi.Subject()
                .subjectId(subject.getId())
                .projectName(subject.getProject().getProjectName())
                .status(subject.getStatus())
                .humanReadableId(subject.getHumanReadableIdentifier())
                .sources(this.sourceService.getAllSourcesOfSubject(subject.getProject()
                        .getProjectName(), subject.getId()));
    }

    /**
     * Returns list of {@link org.radarcns.domain.restapi.Subject} available under given project.
     * @param projectName of project
     * @return list of subjects.
     * @throws IOException when unable to process
     * @throws NotFoundException when given parameters are not available in the database.
     */
    public List<org.radarcns.domain.restapi.Subject> getAllSubjectsFromProject(String projectName)
            throws IOException, NotFoundException {
        // returns NotFound if a project is not available
        this.managementPortalClient.getProject(projectName);
        return this.managementPortalClient.getAllSubjectsFromProject(projectName).stream()
                .map(s -> {
                    try {
                        return buildSubject(s);
                    } catch (IOException exe) {
                        throw new BadGatewayException(exe);
                    }
                }).collect(Collectors.toList());
    }

    public org.radarcns.domain.restapi.Subject getSubjectBySubjectId(String projectName,
            String subjectId) throws IOException, NotFoundException {
        this.managementPortalClient.getProject(projectName);
        return this.buildSubject(this.managementPortalClient.getSubject(subjectId));
    }
}
