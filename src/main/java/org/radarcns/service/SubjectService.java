package org.radarcns.service;

import static org.radarcns.util.ThrowingFunction.tryOrRethrow;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.radarcns.domain.managementportal.SubjectDTO;
import org.radarcns.domain.restapi.Source;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.webapp.exception.BadGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private final ManagementPortalClient managementPortalClient;

    private final SourceService sourceService;

    /**
     * Default constructor. Injects all dependencies.
     *
     * @param managementPortalClient instance
     * @param sourceService instance
     */
    @Inject
    public SubjectService(ManagementPortalClient managementPortalClient,
            SourceService sourceService) {
        this.managementPortalClient = managementPortalClient;
        this.sourceService = sourceService;
    }

    /**
     * Checks whether given source-id is available in the sources available for the subject.
     *
     * @param subjectId of subject
     * @param sourceId of source
     * @return {@code true} if available.
     * @throws IOException when unable to process the request.
     */
    public boolean checkSourceAssignedToSubject(String subjectId, String sourceId)
            throws IOException {
        SubjectDTO subject = managementPortalClient.getSubject(subjectId);
        if (subject.getSources().stream()
                .map(s -> s.getSourceId().toString())
                .noneMatch(sourceId::equals)) {
            LOGGER.error("Cannot find source-id " + sourceId + "for subject" + subject.getId());
            throw new BadRequestException(
                    "Source-id " + sourceId + " is not available for subject "
                            + subject.getId());
        }
        return true;
    }

    private Subject buildSubject(SubjectDTO subject)
            throws IOException {
        List<Source> sources = this.sourceService.getAllSourcesOfSubject(subject.getProject()
                .getProjectName(), subject.getId());
        return new Subject()
                .subjectId(subject.getId())
                .projectName(subject.getProject().getProjectName())
                .status(subject.getStatus())
                .humanReadableId(subject.getHumanReadableIdentifier())
                .sources(sources)
                .lastSeen(getLastSeenForSubject(sources));
    }

    private Instant getLastSeenForSubject(List<Source> sources) {
        return sources.stream()
                .map(Source::getEffectiveTimeFrame)
                .filter(Objects::nonNull)
                .map(TimeFrame::getEndDateTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * Returns list of {@link Subject} available under given project.
     *
     * @param projectName of project
     * @return list of subjects.
     * @throws IOException when unable to process
     * @throws NotFoundException when given parameters are not available in the database.
     */
    public List<Subject> getAllSubjectsFromProject(String projectName)
            throws IOException, NotFoundException {
        // returns NotFound if a project is not available
        this.managementPortalClient.getProject(projectName);
        return this.managementPortalClient.getAllSubjectsFromProject(projectName).stream()
                .map(tryOrRethrow(this::buildSubject, BadGatewayException::new))
                .collect(Collectors.toList());
    }

    public Subject getSubjectBySubjectId(String projectName, String subjectId)
            throws IOException, NotFoundException {
        this.managementPortalClient.getProject(projectName);
        return this.buildSubject(this.managementPortalClient.getSubject(subjectId));
    }
}
