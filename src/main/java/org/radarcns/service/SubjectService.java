package org.radarcns.service;

import static org.radarcns.domain.managementportal.Subject.HUMAN_READABLE_IDENTIFIER_KEY;

import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import org.radarcns.domain.managementportal.Source;
import org.radarcns.domain.managementportal.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private SourceService sourceService;

    public SubjectService(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    public static boolean checkSourceAssignedToSubject(Subject subject, String sourceId) {
        if(subject.getSources().stream().filter(p -> p.getSourceId().equals(sourceId))
                .collect(Collectors.toList()).isEmpty())
        {
            throw new BadRequestException("Source-id "+sourceId+" is not available for subject " +
                     subject.getId());
        }
        return true;
    }

    public static Source getSourceFromSubject(Subject subject , String sourceId) {
        return subject.getSources().stream().filter(p -> p.getSourceId().equals(sourceId))
                .collect(Collectors.toList()).get(0);
    }

    public org.radarcns.domain.restapi.Subject buildSubject(
            org.radarcns.domain.managementportal.Subject subject) {
        return new org.radarcns.domain.restapi.Subject()
                .subjectId(subject.getId())
                .project(subject.getProject())
                .status("ACTIVATED")
                .humanReadableId(subject.getAttribute(HUMAN_READABLE_IDENTIFIER_KEY))
                .sources(this.sourceService.buildSources(subject.getId(),subject.getSources()));
    }


}
