package org.radarcns.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import org.radarcns.domain.managementportal.Source;
import org.radarcns.domain.managementportal.Subject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.webapp.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private ManagementPortalClient managementPortalClient;

    private SourceService sourceService;

    public SubjectService(ManagementPortalClient managementPortalClient, SourceService
            sourceService) {
        this.managementPortalClient = managementPortalClient;
        this.sourceService = sourceService;
    }

    public static boolean checkSourceAssignedToSubject(Subject subject, String sourceId) {
        if (subject.getSources().stream().filter(p -> p.getSourceId().equals(sourceId))
                .collect(Collectors.toList()).isEmpty()) {
            LOGGER.error("Cannot find source-id " + sourceId + "for subject" + subject.getId());
            throw new BadRequestException(
                    "Source-id " + sourceId + " is not available for subject " +
                            subject.getId());
        }
        return true;
    }

    public static Source getSourceFromSubject(Subject subject, String sourceId) {
        // there should be one source that would match the given sourceId
        return subject.getSources().stream().filter(p -> p.getSourceId().equals(sourceId))
                .collect(Collectors.toList()).get(0);
    }

    private org.radarcns.domain.restapi.Subject buildSubject(
            org.radarcns.domain.managementportal.Subject subject) {
        return new org.radarcns.domain.restapi.Subject()
                .subjectId(subject.getId())
                .projectName(subject.getProject().getProjectName())
                .status(subject.getStatus())
                .humanReadableId(subject.getHumanReadableIdentifier())
                .sources(this.sourceService.buildSources(subject.getId(), subject.getSources()));
    }

    public List<org.radarcns.domain.restapi.Subject> getAllSubjectsFromProject(String projectName)
            throws IOException, NotFoundException {
        return this.managementPortalClient.getAllSubjectsFromProject(projectName).stream()
                .map(this::buildSubject).collect(Collectors.toList());
    }

}
