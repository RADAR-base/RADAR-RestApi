package org.radarcns.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.SourceDTO;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.managementportal.SubjectDTO;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private SourceMonitorService sourceMonitorService;

    private SourceCatalog sourceCatalog;

    private ManagementPortalClient managementPortalClient;

    /**
     * Default constructor. Injects all dependencies for the class.
     *
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
     * using provided source. It calculates the {@link org.radarcns.domain.restapi.header.EffectiveTimeFrame}
     * of each sources as well.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param sources from MP
     * @return list of sources assigned to subject under given project.
     */
    public List<org.radarcns.domain.restapi.Source> buildSources(String projectId, String
            subjectId, List<SourceDTO> sources) {
        return sources.stream().map(p -> buildSource(projectId, subjectId, p)).collect(Collectors
                .toList());
    }

    /**
     * Build a {@link org.radarcns.domain.restapi.Source} using provided parameters and by
     * calculating EffectiveTimeFrame of the source.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param source instance from MP
     * @return computed Source.
     */
    public org.radarcns.domain.restapi.Source buildSource(String projectId, String subjectId,
            SourceDTO source) {
        SourceTypeDTO sourceType = null;
        // a source fetched from MP should ideally have a source-type
        try {
            sourceType = this.sourceCatalog.getSourceType(source
                    .getSourceType().getProducer(), source.getSourceType().getModel(), source
                    .getSourceType().getCatalogVersion());
        } catch (NotFoundException | IOException e) {
            LOGGER.error(
                    "Cannot retrieve sourceType-type for given sourceType " + source.getSourceId());
            throw new IllegalStateException(
                    "Cannot retrive sourceType-type for given sourceType " + source
                            .getSourceId());
        }

        return new org.radarcns.domain.restapi.Source()
                .sourceId(source.getSourceIdentifier())
                .assigned(source.getAssigned())
                .sourceName(source.getSourceName())
                .sourceTypeCatalogVersion(source.getSourceType().getCatalogVersion())
                .sourceTypeProducer(source.getSourceType().getProducer())
                .sourceTypeModel(source.getSourceType().getModel())
                .effectiveTimeFrame(this.sourceMonitorService
                        .getEffectiveTimeFrame(projectId, subjectId, source.getSourceIdentifier(),
                                sourceType));
    }

    /**
     * Builds list of {@link org.radarcns.domain.restapi.Source} of given subject under project
     * using provided source. It calculates the {@link org.radarcns.domain.restapi.header.EffectiveTimeFrame}
     * of each sources as well.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param sources from MP
     * @return list of sources assigned to subject under given project.
     */
    public List<org.radarcns.domain.restapi.Source> buildSourcesFromMinimal(String projectId, String
            subjectId, Collection<MinimalSourceDetailsDTO> sources) {
        return sources.stream().map(p -> buildSource(projectId, subjectId, p)).collect(Collectors
                .toList());
    }

    /**
     * Build a {@link org.radarcns.domain.restapi.Source} using provided parameters and by
     * calculating EffectiveTimeFrame of the source.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param source instance from MP
     * @return computed Source.
     */
    public org.radarcns.domain.restapi.Source buildSource(String projectId, String subjectId,
            MinimalSourceDetailsDTO source) {
        SourceTypeDTO sourceType = null;
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
                .sourceId(source.getSourceId().toString())
                .assigned(source.isAssigned())
                .sourceName(source.getSourceName())
                .sourceTypeCatalogVersion(source.getSourceTypeCatalogVersion())
                .sourceTypeProducer(source.getSourceTypeProducer())
                .sourceTypeModel(source.getSourceTypeModel())
                .effectiveTimeFrame(this.sourceMonitorService
                        .getEffectiveTimeFrame(projectId, subjectId,
                                source.getSourceId().toString(),
                                sourceType));
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
        SubjectDTO subject = this.managementPortalClient.getSubject(subjectId);

        Set<String> currentlyAvailableSourceIds = subject.getSources().stream().map
                (p -> p.getSourceId().toString()).collect(Collectors.toSet());

        //fetch all sourceIds of subject available in from mongoDB.
        List<String> recordedSourceIdsForSubject = fetchAllRecordedSourcesForSubject(projectName,
                subjectId);

        // set should avoid duplicates, thus if currently available sources are fetched they
        // won't be repeated.
        Set<String> allSourceIds = Stream.concat(currentlyAvailableSourceIds.stream(),
                recordedSourceIdsForSubject.stream()).collect(Collectors.toSet());

        // fetch source data from management-portal.
        //TODO optimize to reuse what is already gathered from subject and fetch only history
//        List<SourceDTO> sourceList = allSourceIds.stream()
//                .map(s -> {
//                            try {
//                                return managementPortalClient.getSource(s);
//                            } catch (IOException exe) {
//                                throw new BadGatewayException(exe);
//                            }
//                        }
//                ).collect(Collectors.toList());
        // convert source to rest-api response
        return buildSourcesFromMinimal(projectName, subjectId, subject.getSources());
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
