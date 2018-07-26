/*
 * Copyright 2016 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.service;

import static org.radarcns.domain.restapi.header.MonitorHeader.MonitorCategory.PASSIVE;
import static org.radarcns.domain.restapi.header.MonitorHeader.MonitorCategory.QUESTIONNAIRE;

import com.mongodb.MongoClient;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import org.radarcns.domain.managementportal.SourceDTO;
import org.radarcns.domain.restapi.header.MonitorHeader;
import org.radarcns.domain.restapi.monitor.ApplicationStatus;
import org.radarcns.domain.restapi.monitor.MonitorData;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.mongo.data.monitor.application.ApplicationStatusRecordCounter;
import org.radarcns.mongo.data.monitor.application.ApplicationStatusServerStatus;
import org.radarcns.mongo.data.monitor.application.ApplicationStatusUpTime;
import org.radarcns.mongo.data.monitor.application.MongoApplicationStatusWrapper;
import org.radarcns.mongo.data.monitor.questionnaire.QuestionnaireCompletionLogWrapper;

/**
 * Data Access Object for Source Status values.
 */
public class SourceStatusMonitorService {


    private final List<MongoApplicationStatusWrapper> dataAccessObjects;

    private QuestionnaireCompletionLogWrapper questionnaireCompletionLogWrapper;

    private final ManagementPortalClient managementPortalClient;

    /**
     * Default constructor. Initiates all the delegate classes to compute Source Status.
     */
    @Inject
    public SourceStatusMonitorService(ManagementPortalClient managementPortalClient) {
        this.managementPortalClient = managementPortalClient;
        dataAccessObjects = new LinkedList<>();
        dataAccessObjects.add(new ApplicationStatusUpTime());
        dataAccessObjects.add(new ApplicationStatusRecordCounter());
        dataAccessObjects.add(new ApplicationStatusServerStatus());

        this.questionnaireCompletionLogWrapper = new QuestionnaireCompletionLogWrapper();
    }

    /**
     * Computes the Source Status realign on different collection.
     *
     * @param projectName of the subject
     * @param subjectId   identifier
     * @param sourceId    identifier
     * @param client      is the MongoDb client
     * @return {@code MonitorData} representing the status of the related source
     */
    public MonitorData getStatus(String projectName, String subjectId, String sourceId,
            MongoClient client) throws IOException {

        MonitorHeader header = (MonitorHeader) new MonitorHeader()
                .projectId(projectName)
                .subjectId(subjectId)
                .sourceId(sourceId);
        SourceDTO source = managementPortalClient.getSource(sourceId);
        if (source.getSourceType() != null) {
            header.sourceType(source.getSourceType().getSourceTypeIdentifier().toString());
            if (source.getSourceType().getModel().contains("pRMT") || source.getSourceType()
                    .getSourceTypeScope().equals("MONITOR")) {
                ApplicationStatus app = new ApplicationStatus();
                for (MongoApplicationStatusWrapper dataAccessObject : dataAccessObjects) {
                    app = dataAccessObject
                            .valueByProjectSubjectSource(projectName, subjectId, sourceId, app,
                                    client);
                }
                header.monitorCategory(PASSIVE);
                return new MonitorData().header(header).data(app);
            }

            if (source.getSourceType().getModel().contains("aRMT-App")) {
                header.monitorCategory(QUESTIONNAIRE);
                return new MonitorData().header(header).data(questionnaireCompletionLogWrapper
                        .valueByProjectSubjectSource(projectName, subjectId, sourceId, client));
            }

        }
        return new MonitorData().header(header);
    }
}