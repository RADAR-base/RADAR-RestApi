package org.radarcns.pipeline.config;

/*
 *  Copyright 2016 Kings College London and The Hyve
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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PipelineConfig {
    @JsonProperty("rest_proxy")
    private List<ServerConfig> restProxy;

    @JsonProperty("schema_registry")
    private List<ServerConfig> schemaRegistry;

    @JsonProperty("rest_api")
    private List<ServerConfig> restApi;

    @JsonProperty("api_web_root")
    private String apiWebRoot;

    private Long duration;

    public List<ServerConfig> getRestProxy() {
        return restProxy;
    }

    public void setRestProxy(List<ServerConfig> restProxy) {
        this.restProxy = restProxy;
    }

    public List<ServerConfig> getSchemaRegistry() {
        return schemaRegistry;
    }

    public void setSchemaRegistry(List<ServerConfig> schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    /**
     * Returns the URL to access Schema Registry.
     */
    public String getSchemaRegistryInstance() {
        ServerConfig server = schemaRegistry.get(0);
        return server.getProtocol() + "://" + server.getHost() + ":" + server.getPort();
    }

    /**
     * Returns the URL to access REST Proxy.
     */
    public String getRestProxyInstance() {
        ServerConfig server = restProxy.get(0);
        return server.getProtocol() + "://" + server.getHost() + ":" + server.getPort();
    }

    public String getApiWebRoot() {
        return apiWebRoot;
    }

    public void setApiWebRoot(String apiWebRoot) {
        this.apiWebRoot = apiWebRoot;
    }

    /**
     * Returns the URL to access RADAR-CNS REST API.
     */
    public String getRestApiInstance() {
        ServerConfig server = restApi.get(0);
        return server.getProtocol() + "://" + server.getHost() + ":" + server.getPort()
                + getApiWebRoot();
    }
}
