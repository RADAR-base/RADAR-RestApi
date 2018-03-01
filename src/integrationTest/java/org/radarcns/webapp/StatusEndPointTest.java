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

package org.radarcns.webapp;

import java.io.IOException;
import javax.ws.rs.core.Response.Status;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.status.hdfs.HdfsBinsData;
import org.radarcns.status.hdfs.HdfsBinsDataTest;

public class StatusEndPointTest {

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString()
                    + "status/");

    @Test
    public void getStatusTest200() throws IOException {
        HdfsBinsData bins = apiClient.getJson("hdfs", HdfsBinsData.class, Status.OK);
        HdfsBinsDataTest.assertBinsMatchFile(bins);
    }
}
