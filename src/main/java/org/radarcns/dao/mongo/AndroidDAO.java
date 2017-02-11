package org.radarcns.dao.mongo;

import java.net.ConnectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.dao.mongo.util.MongoAppDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class AndroidDAO {

    //private final Logger logger = LoggerFactory.getLogger(AndroidDAO.class);

    private final static AndroidDAO instance = new AndroidDAO();

    public static AndroidDAO getInstance(){
        return instance;
    }

    private MongoAppDAO server = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc) {
            return new Application(doc.getString("clientIP"),
                new Double(-1.0), doc.getString("serverStatus"));
            // TODO: 09/02/2017 define enumerator for server status. Ask Android team
        }

        @Override
        protected String getCollectionName() {
            return "application_status_server";
        }
    };

    private MongoAppDAO uptime = new MongoAppDAO() {
        @Override
        protected Application getApplication(Document doc) {
            return new Application("",doc.getDouble("applicationUptime"),"");
        }

        @Override
        protected String getCollectionName() {
            return "application_status_uptime";
        }
    };

    public Application getStatus(String user, String source, ServletContext context)
        throws ConnectException {
        Application app = server.valueByUserSource(user, source, context);
        app.setUptime(uptime.valueByUserSource(user, source, context).getUptime());

        return app;
    }

    public Collection<String> findAllUser(ServletContext context) throws ConnectException {
        Set<String> users = new HashSet<>();

        users.addAll(server.findAllUser(context));
        users.addAll(uptime.findAllUser(context));

        return users;
    }

    public Collection<String> findAllSoucesByUser(String user, ServletContext context)
        throws ConnectException {
        Set<String> users = new HashSet<>();

        users.addAll(server.findAllSoucesByUser(user, context));
        users.addAll(uptime.findAllSoucesByUser(user, context));

        return users;
    }
}