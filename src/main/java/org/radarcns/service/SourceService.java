package org.radarcns.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.Source;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.domain.managementportal.SourceTypeIdentifier;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private SourceMonitorService sourceMonitorService;

    private SourceCatalog sourceCatalog;

    private ManagementPortalClient managementPortalClient;

    @Inject
    public SourceService(SourceMonitorService sourceMonitorService, SourceCatalog sourceCatalog,
            ManagementPortalClient managementPortalClient) {
        this.sourceMonitorService = sourceMonitorService;
        this.sourceCatalog = sourceCatalog;
        this.managementPortalClient = managementPortalClient;
    }


    public List<org.radarcns.domain.restapi.Source> buildSources(String projectId, String
            subjectId,
            List<Source> sources) {
        return sources.stream().map(p -> buildSource(projectId, subjectId, p)).collect(Collectors
                .toList());
    }

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
     * Returns all the sources recorded for a subject under given project
     *
     * @param projectName of subject
     * @param subjectId of subject
     * @return list of {@link org.radarcns.domain.restapi.Source} of subject
     */
    public List<org.radarcns.domain.restapi.Source> getAllSourcesOfSubject(String projectName,
            String subjectId) throws IOException {
        this.managementPortalClient.getSubject(subjectId);
        //fetch all sourceIds of subject available in from mongoDB
        // can be replaced if the history tracking feature is available in MP
        List<String> allSources = this.sourceCatalog.getSourceTypes().stream().map(sourceType ->
                sourceMonitorService.getAllSourcesOfSubjectInProject(projectName, subjectId,
                        sourceType)).flatMap(List::stream).collect(Collectors.toList());
        // fetch source data from MP
        List<Source> sources = allSources.stream().map(s -> {
                    try {
                        return managementPortalClient.getSource(s);
                    } catch (IOException exe) {
                        return null;
                    }
                }
        ).collect(Collectors.toList());
        // convert source to rest-api response
        return buildSources(projectName, subjectId, sources);
    }


}
