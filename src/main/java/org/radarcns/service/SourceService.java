package org.radarcns.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.MinimalSourceDetailsDTO;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private final SourceMonitorService sourceMonitorService;

    private final SourceCatalog sourceCatalog;

    private final ManagementPortalClient managementPortalClient;

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
     * using provided source. It calculates the {@link TimeFrame} of each sources as well.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param sources from MP
     * @return list of sources assigned to subject under given project.
     */
    private List<org.radarcns.domain.restapi.Source> buildSourcesFromMinimal(String projectId,
            String subjectId, Collection<MinimalSourceDetailsDTO> sources) {
        return sources.stream().map(p -> buildSource(projectId, subjectId, p)).collect(Collectors
                .toList());
    }

    /**
     * Build a {@link org.radarcns.domain.restapi.Source} using provided parameters and by
     * calculating TimeFrame of the source.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param source instance from MP
     * @return computed SourceDTO.
     */
    private org.radarcns.domain.restapi.Source buildSource(String projectId, String subjectId,
            MinimalSourceDetailsDTO source) {
        SourceTypeDTO sourceType;
        // a source fetched from MP should ideally have a source-type
        try {
            sourceType = this.sourceCatalog.getSourceType(source
                    .getSourceTypeProducer(), source.getSourceTypeModel(), source
                    .getSourceTypeCatalogVersion());
        } catch (NotFoundException | IOException e) {
            LOGGER.error(
                    "Cannot retrieve sourceType for given sourceType " + source.getSourceId());
            throw new IllegalStateException(
                    "Cannot retrieve sourceType for given sourceType " + source.getSourceId());
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
                                source.getSourceId().toString(), sourceType));
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
        //TODO implement fetching all recorded sources for subject
        return buildSourcesFromMinimal(projectName, subjectId,
                this.managementPortalClient.getSubject(subjectId).getSources());
    }

}
