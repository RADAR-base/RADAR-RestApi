package org.radarcns.util;

/*
 *  Copyright 2016 King's College London and The Hyve
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

import io.swagger.jaxrs.config.BeanConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.radarcns.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 14/11/2016.
 */
public class SwaggerBootstrap extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerBootstrap.class);

    public static final String TITLE = "RADAR-CNS Downstream REST APIs";
    public static final String RESOURCE_PACKAGE = "org.radarcns.webapp";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(Properties.getApiConfig().getSwaggerVersion());
        beanConfig.setTitle(TITLE);
        beanConfig.setSchemes(Properties.getApiConfig().getApplicationProtocols());

        beanConfig.setHost(Properties.getApiConfig().getHost());

        beanConfig.setBasePath(Properties.getApiConfig().getApiBasePath());
        beanConfig.setResourcePackage(RESOURCE_PACKAGE);

        /*TODO implement filter
        beanConfig.setFilterClass();*/

        beanConfig.setScan(true);
        beanConfig.setPrettyPrint(true);

        LOGGER.info("Swagger initialised");
    }
}