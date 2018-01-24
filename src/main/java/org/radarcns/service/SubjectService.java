package org.radarcns.service;

import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import org.radarcns.managementportal.Source;
import org.radarcns.managementportal.Subject;

public class SubjectService {

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

}
