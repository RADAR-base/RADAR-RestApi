package org.radarcns.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.BeanConfig;

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

        /** Docker **/
        beanConfig.setHost("localhost:8080");
        /** Amazon ECS **/
//        beanConfig.setHost("radar-restapi.eu-west-1.elasticbeanstalk.com");

        beanConfig.setBasePath("/api");
        beanConfig.setResourcePackage("org.radarcns.webapp");

        /*TODO
        beanConfig.setFilterClass();*/

        beanConfig.setScan(true);
        beanConfig.setPrettyPrint(true);

        logger.info("Swagger initialised");
    }
}