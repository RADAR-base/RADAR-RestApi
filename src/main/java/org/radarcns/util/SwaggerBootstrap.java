package org.radarcns.util;

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

import io.swagger.jaxrs.config.BeanConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 14/11/2016.
 */
public class SwaggerBootstrap extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerBootstrap.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setTitle("RADAR-CNS Downstream REST APIs");
        beanConfig.setSchemes(new String[]{"http"});

        beanConfig.setHost("localhost:8080");

        beanConfig.setBasePath("/api");
        beanConfig.setResourcePackage("org.radarcns.webapp");

        /*TODO
        beanConfig.setFilterClass();*/

        beanConfig.setScan(true);
        beanConfig.setPrettyPrint(true);

        logger.info("Swagger initialised");
    }
}