package org.radarcns.dao.mongo;

import java.net.ConnectException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;

/**
 * Created by francesco on 09/02/2017.
 */
public class UserDAO {

    public static Cohort findAllUser(ServletContext context) throws ConnectException {

        List<Patient> patients = new LinkedList<>();

        Set<String> users = new HashSet<>();
        users.addAll(AccelerationDAO.getInstance().findAllUser(context));
        users.addAll(BatteryDAO.getInstance().findAllUser(context));
        users.addAll(BloodVolumePulseDAO.getInstance().findAllUser(context));
        users.addAll(ElectrodermalActivityDAO.getInstance().findAllUser(context));
        users.addAll(HeartRateDAO.getInstance().findAllUser(context));
        users.addAll(InterBeatIntervalDAO.getInstance().findAllUser(context));
        users.addAll(TemperatureDAO.getInstance().findAllUser(context));
        users.addAll(AndroidDAO.getInstance().findAllUser(context));

        for (String user : users) {
            patients.add(findAllSoucesByUser(user, context));
        }

        return new Cohort(0, patients);
    }

    public static Patient findAllSoucesByUser(String userID, ServletContext context) throws ConnectException {
        Set<String> sources = new HashSet<>();
        sources.addAll(AccelerationDAO.getInstance().findAllSoucesByUser(userID, context));
        sources.addAll(BatteryDAO.getInstance().findAllSoucesByUser(userID, context));
        sources.addAll(BloodVolumePulseDAO.getInstance().findAllSoucesByUser(userID, context));
        sources.addAll(ElectrodermalActivityDAO.getInstance().findAllSoucesByUser(userID, context));
        sources.addAll(HeartRateDAO.getInstance().findAllSoucesByUser(userID, context));
        sources.addAll(InterBeatIntervalDAO.getInstance().findAllSoucesByUser(userID, context));
        sources.addAll(TemperatureDAO.getInstance().findAllSoucesByUser(userID, context));
        sources.addAll(AndroidDAO.getInstance().findAllSoucesByUser(userID, context));

        return new Patient(userID, new LinkedList<>(sources));
    }

}
