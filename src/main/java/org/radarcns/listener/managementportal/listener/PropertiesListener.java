package org.radarcns.listener.managementportal.listener;

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

import org.radarcns.config.managementportal.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Loads the service configuration.
 * @see Properties
 */
@WebListener
public class PropertiesListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            LOGGER.info(Properties.validate());

            //MpClient mp = new MpClient(sce.getServletContext());
            //LOGGER.info(mp.toString());
        } catch (Exception exc) {
            LOGGER.error("Properties cannot be load. Check the log for more information.", exc);
            throw new ExceptionInInitializerError(exc);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //Nothing to do
    }
}
