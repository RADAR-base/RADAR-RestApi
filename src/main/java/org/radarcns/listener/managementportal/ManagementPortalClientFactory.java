/*
 * Copyright 2017 King's College London
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

package org.radarcns.listener.managementportal;

import javax.ws.rs.core.Context;
import okhttp3.OkHttpClient;
import org.glassfish.hk2.api.Factory;
import org.radarcns.oauth.OAuth2AccessTokenDetails;

/**
 * Refreshes the OAuth2 token needed to authenticate against the Management Portal and adds it to
 * the {@link javax.servlet.ServletContext} in this way multiple function can make reuse of it.
 */
public class ManagementPortalClientFactory implements Factory<ManagementPortalClient> {

    @Context
    private OAuth2AccessTokenDetails token;

    @Context
    private OkHttpClient client;

    @Override
    public ManagementPortalClient provide() {
        ManagementPortalClient mpClient = new ManagementPortalClient(client);
        mpClient.updateToken(token);
        return mpClient;
    }

    @Override
    public void dispose(ManagementPortalClient instance) {
        // no disposal needed
    }
}
