package org.radarcns.monitor;

import java.net.ConnectException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import org.radarcns.avro.restapi.device.Device;
import org.radarcns.avro.restapi.device.SensorStatus;
import org.radarcns.avro.restapi.device.Sensors;
import org.radarcns.avro.restapi.device.Status;
import org.radarcns.dao.mongo.AccelerationDAO;
import org.radarcns.dao.mongo.BatteryDAO;
import org.radarcns.dao.mongo.BloodVolumePulseDAO;
import org.radarcns.dao.mongo.ElectrodermalActivityDAO;
import org.radarcns.dao.mongo.HeartRateDAO;
import org.radarcns.dao.mongo.InterBeatIntervalDAO;
import org.radarcns.dao.mongo.TemperatureDAO;
import org.radarcns.util.RadarConverter;

/**
 * Empatica status monitor.
 */
public class Empatica {

    //private static final Logger logger = LoggerFactory.getLogger(Empatica.class);

    /**
     * Checks the status for the given Empatica device counting the number of received messages and
     *      checking whether it respects the sensor frequencies. There is a check for each sensor.
     *
     * @param user identifier
     * @param source identifier
     * @param context useful to retrieve the MongoDb connection
     * @return {@code Device} representing an Empatica device
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see {@link org.radarcns.avro.restapi.device.Device}
     */
    public static Device monitor(String user, String source, ServletContext context)
            throws ConnectException {
        long end = (System.currentTimeMillis() / 10000) * 10000;
        long start = end - 60000;

        List<SensorStatus> sensors = new LinkedList<>();

        double accCount = AccelerationDAO.getInstance().countSamplesByUserSourceWindow(user, source,
                start, end, context);
        double accPerc = getPercentage(accCount, 32.0);
        sensors.add(new SensorStatus(Sensors.ACC, getStatus(accPerc), (int)accCount,
                RadarConverter.roundDouble(1.0 - accPerc, 2)));

        double batCount = BatteryDAO.getInstance().countSamplesByUserSourceWindow(user, source,
                start, end, context);
        double batPerc = getPercentage(batCount, 1.0);
        sensors.add(new SensorStatus(Sensors.BAT, getStatus(batPerc), (int)batCount,
                RadarConverter.roundDouble(1.0 - batPerc, 2)));

        double bvpCount = BloodVolumePulseDAO.getInstance().countSamplesByUserSourceWindow(user,
                source, start, end, context);
        double bvpPerc = getPercentage(bvpCount, 64.0);
        sensors.add(new SensorStatus(Sensors.BVP, getStatus(bvpPerc), (int)bvpCount,
                RadarConverter.roundDouble(1.0 - bvpPerc, 2)));

        double edaCount = ElectrodermalActivityDAO.getInstance().countSamplesByUserSourceWindow(
                user, source, start, end, context);
        double edaPerc = getPercentage(edaCount, 4.0);
        sensors.add(new SensorStatus(Sensors.EDA, getStatus(edaPerc), (int)edaCount,
                RadarConverter.roundDouble(1.0 - edaPerc, 2)));

        double ibiCount = InterBeatIntervalDAO.getInstance().countSamplesByUserSourceWindow(user,
                source, start, end, context);
        double ibiPerc = getPercentage(ibiCount, 1.0);
        sensors.add(new SensorStatus(Sensors.IBI, getStatus(ibiPerc), (int)ibiCount,
                RadarConverter.roundDouble(1.0 - ibiPerc, 2)));

        double hrCount = HeartRateDAO.getInstance().countSamplesByUserSourceWindow(user, source,
                start, end, context);
        double hrPerc = getPercentage(hrCount, 1.0);
        sensors.add(new SensorStatus(Sensors.HR, getStatus(hrPerc), (int)hrCount,
                RadarConverter.roundDouble(1.0 - hrPerc, 2)));

        double tempCount = TemperatureDAO.getInstance().countSamplesByUserSourceWindow(user, source,
                start, end, context);
        double tempPerc = getPercentage(tempCount, 4.0);
        sensors.add(new SensorStatus(Sensors.TEMP, getStatus(tempPerc), (int)tempCount,
                RadarConverter.roundDouble(1.0 - tempPerc, 2)));

        double countMex = accCount + batCount + bvpCount + edaCount + hrCount + ibiCount
                + tempCount;
        double avgPerc = (accPerc + batPerc + bvpPerc + edaPerc + hrPerc + ibiPerc + tempPerc)
                / 7.0;

        Device device = new Device("Empatica", getStatus(avgPerc), (int)countMex,
                RadarConverter.roundDouble(1.0 - avgPerc, 2), sensors);

        return device;
    }

    /**
     * Returns the percentage of received message with respect to the expected value.
     *
     * @param count received messages
     * @param expected expected messages
     * @return the ratio of count over expected
     */
    private static double getPercentage(double count, double expected) {
        return count / expected;
    }

    /**
     * Convert numerical percentage to device status.
     *
     * @param percentage numerical value that has to be converted int Status
     * @return the current {@code Status}
     *
     * @see {@link org.radarcns.avro.restapi.device.Status}
     */
    private static Status getStatus(double percentage) {
        if (percentage > 0.95) {
            return Status.FINE;
        } else if ((percentage > 0.80) && (percentage <= 0.95)) {
            return Status.OK;
        } else if ((percentage > 0.50) && (percentage <= 0.80)) {
            return Status.WARNING;
        } else if ((percentage > 0.0) && (percentage <= 0.50)) {
            return Status.BAD;
        } else {
            return Status.DISCONNECTED;
        }
    }
}
