package org.radarcns.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.Source;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.domain.managementportal.SourceTypeIdentifier;
import org.radarcns.domain.managementportal.Subject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.webapp.exception.BadGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private SourceMonitorService sourceMonitorService;

    private SourceCatalog sourceCatalog;

    private ManagementPortalClient managementPortalClient;

    /**
     * Default constructor.
     * Injects all dependencies for the class.
     * @param sourceMonitorService instance
     * @param sourceCatalog instance
     * @param managementPortalClient instance
     */
    @Inject
    public SourceService(SourceMonitorService sourceMonitorService, SourceCatalog sourceCatalog,
            ManagementPortalClient managementPortalClient) {
        this.sourceMonitorService = sourceMonitorService;
        this.sourceCatalog = sourceCatalog;
        this.managementPortalClient = managementPortalClient;
    }


    /**
     * Builds list of {@link org.radarcns.domain.restapi.Source} of given subject under project
     * using provided source. It calculates the
     * {@link org.radarcns.domain.restapi.header.EffectiveTimeFrame} of each sources as well.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param sources from MP
     * @return list of sources assigned to subject under given project.
     */
    public List<org.radarcns.domain.restapi.Source> buildSources(String projectId, String
            subjectId, List<Source> sources) {
        return sources.stream().map(p -> buildSource(projectId, subjectId, p)).collect(Collectors
                .toList());
    }

    /**
     * Build a {@link org.radarcns.domain.restapi.Source} using provided parameters and by
     * calculating EffectiveTimeFrame of the source.
     * @param projectId of subject
     * @param subjectId of subject
     * @param source instance from MP
     * @return computed Source.
     */
    public org.radarcns.domain.restapi.Source buildSource(String projectId, String subjectId,
            Source source) {
        SourceType sourceType = null;
        // a source fetched from MP should ideally have a source-type
        try {
            sourceType = this.sourceCatalog.getSourceType(source
                    .getSourceTypeProducer(), source.getSourceTypeModel(), source
                    .getSourceTypeCatalogVersion());
        } catch (NotFoundException | IOException e) {
            LOGGER.error(
                    "Cannot retrieve sourceType-type for given sourceType " + source.getSourceId());
            throw new IllegalStateException(
                    "Cannot retrive sourceType-type for given sourceType " + source
                            .getSourceId());
        }

        return new org.radarcns.domain.restapi.Source()
                .sourceId(source.getSourceId())
                .assigned(source.getAssigned())
                .sourceName(source.getSourceName())
                .sourceTypeCatalogVersion(source.getSourceTypeCatalogVersion())
                .sourceTypeProducer(source.getSourceTypeProducer())
                .sourceTypeModel(source.getSourceTypeModel())
                .effectiveTimeFrame(this.sourceMonitorService
                        .getEffectiveTimeFrame(projectId, subjectId, source.getSourceId(),
                                sourceType));
    }

    public static SourceTypeIdentifier getSourceTypeIdFromSource(Source source) {
        return new SourceTypeIdentifier(source.getSourceTypeProducer(), source.getSourceTypeModel(),
                source.getSourceTypeCatalogVersion());
    }


    /**
     * Returns all the sources recorded for a subject under given project including history.
     *
     * @param projectName of subject
     * @param subjectId of subject
     * @return list of {@link org.radarcns.domain.restapi.Source} of subject
     */
    public List<org.radarcns.domain.restapi.Source> getAllSourcesOfSubject(String projectName,
            String subjectId) throws IOException {
        Subject subject = this.managementPortalClient.getSubject(subjectId);

        Set<String> currentlyAvailableSourceIds = subject.getSources().stream().map
                (Source::getSourceId).collect(Collectors.toSet());

        //fetch all sourceIds of subject available in from mongoDB.
        List<String> recordedSourceIdsForSubject = fetchAllRecordedSourcesForSubject(projectName,
                subjectId);

        // set should avoid duplicates, thus if currently available sources are fetched they
        // won't be repeated.
        Set<String> allSourceIds= Stream.concat(currentlyAvailableSourceIds.stream(),
                recordedSourceIdsForSubject.stream()).collect(Collectors.toSet());

        // fetch source data from management-portal.
        List<Source> sourceList = allSourceIds.stream()
                .map(s -> {
                            try {
                                return managementPortalClient.getSource(s);
                            } catch (IOException exe) {
                                throw new BadGatewayException(exe);
                            }
                        }
                ).collect(Collectors.toList());
        // convert source to rest-api response
        return buildSources(projectName, subjectId, sourceList);
    }


    private List<String> fetchAllRecordedSourcesForSubject(String projectName, String subjectId)
            throws IOException {
        // fetches source-ids reported for all available source-types for provided subject and
        // project in source-monitor-statistics
        return this.sourceCatalog.getSourceTypes().stream().map(sourceType ->
                sourceMonitorService.getAllSourcesOfSubjectInProject(projectName, subjectId,
                        sourceType)).flatMap(List::stream).collect(Collectors.toList());
    }
}
