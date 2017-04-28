package org.radarcns.listener;

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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.radarcns.source.SourceCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upon the web application initialisation, this Context Listener checks if the Source Catalog can
 *      be correctly loaded. In case of error, the application will be stopped since nothing will
 *      properly work without the Source Catalog.
 *
 * @see SourceCatalog
 */
@WebListener
public class SourceCatalogContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            SourceCatalogContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //Nothing to do
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            SourceCatalog.getInstance();
        } catch (Exception exec) {
            LOGGER.error("Source Catalog cannot be load. Check the log for more information.",
                    exec);
            throw new ExceptionInInitializerError(exec);
        }
    }

}
