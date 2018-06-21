package org.radarcns.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.MinimalSourceDetailsDTO;
import org.radarcns.domain.managementportal.SourceDTO;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.restapi.Source;
import org.radarcns.domain.restapi.SourceStatus;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private final SourceMonitorService sourceMonitorService;

    private final SourceCatalog sourceCatalog;

    private final ManagementPortalClient managementPortalClient;

    private static final Duration DEFAULT_SOURCE_CONNECTION_TIMEOUT = Duration.ofHours(6);

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
    private List<Source> buildSourcesFromMinimal(String projectId,
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
     * @return computed Source.
     */
    private Source buildSource(String projectId, String subjectId,
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

        TimeFrame effectiveTimeFrame = this.sourceMonitorService
                .getEffectiveTimeFrame(projectId, subjectId,
                        source.getSourceId().toString(), sourceType);
        return new Source()
                .sourceId(source.getSourceId().toString())
                .assigned(source.isAssigned())
                .sourceName(source.getSourceName())
                .sourceTypeId(source.getSourceTypeId())
                .sourceTypeCatalogVersion(source.getSourceTypeCatalogVersion())
                .sourceTypeProducer(source.getSourceTypeProducer())
                .sourceTypeModel(source.getSourceTypeModel())
                .effectiveTimeFrame(effectiveTimeFrame)
                .status(getSourceStatus(source.getSourceTypeProducer(), source
                        .getSourceTypeModel(), effectiveTimeFrame));
    }

    /**
     * Build a {@link org.radarcns.domain.restapi.Source} using provided parameters and by
     * calculating TimeFrame of the source.
     *
     * @param projectId of subject
     * @param subjectId of subject
     * @param source instance from MP
     * @return computed Source.
     */
    private Source buildSource(String projectId, String subjectId,
            SourceDTO source) {
        SourceTypeDTO sourceType;
        // a source fetched from MP should ideally have a source-type
        try {
            sourceType = this.sourceCatalog.getSourceType(source
                    .getSourceType().getProducer(), source.getSourceType().getModel(), source
                    .getSourceType().getCatalogVersion());
        } catch (NotFoundException | IOException e) {
            LOGGER.error(
                    "Cannot retrieve sourceType for given sourceType " + source.getSourceId());
            throw new IllegalStateException(
                    "Cannot retrieve sourceType for given sourceType " + source.getSourceId());
        }

        TimeFrame effectiveTimeFrame = this.sourceMonitorService
                .getEffectiveTimeFrame(projectId, subjectId,
                        source.getSourceId(), sourceType);
        return new Source()
                .sourceId(source.getSourceId())
                .assigned(source.getAssigned())
                .sourceName(source.getSourceName())
                .sourceTypeId(sourceType.getId())
                .sourceTypeCatalogVersion(sourceType.getCatalogVersion())
                .sourceTypeProducer(sourceType.getProducer())
                .sourceTypeModel(sourceType.getModel())
                .effectiveTimeFrame(effectiveTimeFrame)
                .status(getSourceStatus(sourceType.getProducer(), sourceType.getModel(),
                        effectiveTimeFrame));
    }

    /**
     * Computes the status of a source based on the last seen of the source and
     * connection-timeout specified for source-type or using default value. Returns
     * {@link SourceStatus#CONNECTED} is the effectiveTimeFrame.endTime is not earlier than
     * timeout duration from now.
     * @param producer of source-type
     * @param model of source-type
     * @param effectiveTimeFrame of the source
     * @return computed source status
     */
    private SourceStatus getSourceStatus(String producer, String model,
            TimeFrame effectiveTimeFrame) {
        if (effectiveTimeFrame != null && effectiveTimeFrame.getEndDateTime() != null) {
            // convert to lower case to produce key
            String sourceTypeKey = (producer + "_" + model).toLowerCase();
            Duration timeout = DEFAULT_SOURCE_CONNECTION_TIMEOUT;
            if (org.radarcns.config.Properties.getApiConfig().getSourceTypeConnectionTimeout()
                    .containsKey(sourceTypeKey)) {
                // use configured timeout if specified
                timeout = Duration.parse(org.radarcns.config.Properties.getApiConfig()
                        .getSourceTypeConnectionTimeout().get(sourceTypeKey));
            }
            return (Instant.now().minus(timeout).isBefore(effectiveTimeFrame.getEndDateTime()))
                    ? SourceStatus.CONNECTED : SourceStatus.DISCONNECTED;
        }
        // status UNKNOWN if the effective-time is not available
        return SourceStatus.UNKNOWN;
    }

    /**
     * Returns all the sources recorded for a subject under given project including history.
     *
     * @param projectName of subject
     * @param subjectId of subject
     * @return list of {@link org.radarcns.domain.restapi.Source} of subject
     */
    public List<Source> getAllSourcesOfSubject(String projectName,
            String subjectId) throws IOException {
        //TODO implement fetching all recorded sources for subject
        return buildSourcesFromMinimal(projectName, subjectId,
                this.managementPortalClient.getSubject(subjectId).getSources());
    }

    public Source getSourceBySourceId(String projectName, String subjectId, String sourceId)
            throws IOException {
        return buildSource(projectName, subjectId, this.managementPortalClient.getSource(sourceId));
    }
}
