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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Swagger initializer. It sets all the required variable to create a valid swagger documentation.
 */
public class SwaggerBootstrap extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerBootstrap.class);

    public static final String TITLE = "RADAR-CNS Downstream REST APIs";
    public static final String RESOURCE_PACKAGE = "org.radarcns.webapp";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
//
//        OpenAPI oas = new OpenAPI();
//        Info info = new Info()
//                .title("Swagger Sample App")
//                .description("This is a sample server Petstore server.  You can find out more about Swagger " +
//                        "at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, " +
//                        "you can use the api key `special-key` to test the authorization filters.")
//                .termsOfService("http://swagger.io/terms/")
//                .contact(new Contact()
//                        .email("apiteam@swagger.io"))
//                .license(new License()
//                        .name("Apache 2.0")
//                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
//
//        oas.info(info);
//        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
//                .openAPI(oas)
//                .resourcePackages(Stream.of("io.swagger.sample.resource").collect(Collectors.toSet()));
//
//        try {
//            new JaxrsOpenApiContextBuilder()
//                    .servletConfig(config)
//                    .openApiConfiguration(oasConfig)
//                    .buildContext(true);
//        } catch (OpenApiConfigurationException e) {
//            throw new ServletException(e.getMessage(), e);
//        }
//
//        BeanConfig beanConfig = new BeanConfig();
//        beanConfig.setVersion(Properties.getApiConfig().getSwaggerVersion());
//        beanConfig.setTitle(TITLE);
//        beanConfig.setSchemes(Properties.getApiConfig().getApplicationProtocols());
//
//        beanConfig.setHost(Properties.getApiConfig().getHost());
//
//        beanConfig.setBasePath(Properties.getApiConfig().getApiBasePath());
//        beanConfig.setResourcePackage(RESOURCE_PACKAGE);
//
//        /*TODO implement filter
//        beanConfig.setFilterClass();*/
//
//        beanConfig.setScan(true);
//        beanConfig.setPrettyPrint(true);

        LOGGER.info("Swagger initialised");
    }
}