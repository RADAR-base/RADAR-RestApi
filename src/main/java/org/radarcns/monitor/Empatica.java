package org.radarcns.monitor;

import javax.servlet.ServletContext;
import org.radarcns.avro.restapi.device.Device;
import org.radarcns.avro.restapi.device.Status;
import org.radarcns.dao.mongo.AccelerationDAO;
import org.radarcns.dao.mongo.BatteryDAO;
import org.radarcns.dao.mongo.BloodVolumePulseDAO;
import org.radarcns.dao.mongo.ElectrodermalActivityDAO;
import org.radarcns.dao.mongo.HeartRateDAO;
import org.radarcns.dao.mongo.InterBeatIntervalDAO;
import org.radarcns.dao.mongo.TemperatureDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class Empatica {

    private final static Logger logger = LoggerFactory.getLogger(Empatica.class);

    private final static long WINDOW = 30000;

    public static Device monitor(String user, String source, ServletContext context){
        long end = System.currentTimeMillis();
        long start = end - 30000;

        double accCount = AccelerationDAO.getInstance().countSamplesByUserSourceWindow(user, source, start, end, context);
        double accPerc = getPercentage(accCount, 32.0 * (WINDOW / 1000));
        logger.info("[ACC] count:{} perc:{}", accCount, accPerc);

        double batCount = BatteryDAO.getInstance().countSamplesByUserSourceWindow(user, source, start, end, context);
        double batPerc = getPercentage(batCount, 1.0 * (WINDOW / 1000));
        logger.info("[BAT] count:{} perc:{}", batCount, batPerc);

        double bvpCount = BloodVolumePulseDAO.getInstance().countSamplesByUserSourceWindow(user, source, start, end, context);
        double bvpPerc = getPercentage(bvpCount, 64.0 * (WINDOW / 1000));
        logger.info("[BVP] count:{} perc:{}", bvpCount, bvpPerc);

        double edaCount = ElectrodermalActivityDAO.getInstance().countSamplesByUserSourceWindow(user, source, start, end, context);
        double edaPerc = getPercentage(edaCount, 4.0 * (WINDOW / 1000));
        logger.info("[EDA] count:{} perc:{}", edaCount, edaPerc);

        double ibiCount = InterBeatIntervalDAO.getInstance().countSamplesByUserSourceWindow(user, source, start, end, context);
        double ibiPerc = getPercentage(ibiCount, 1.0 * (WINDOW / 1000));
        logger.info("[IBI] count:{} perc:{}", ibiCount, ibiPerc);

        double hrCount = HeartRateDAO.getInstance().countSamplesByUserSourceWindow(user, source, start, end, context);
        double hrPerc = getPercentage(hrCount, 1.0 * (WINDOW / 1000));
        logger.info("[HR] count:{} perc:{}", hrCount, hrPerc);

        double tempCount = TemperatureDAO.getInstance().countSamplesByUserSourceWindow(user, source, start, end, context);
        double tempPerc = getPercentage(tempCount, 4.0 * (WINDOW / 1000));
        logger.info("[TEMP] count:{} perc:{}", tempCount, tempPerc);

        double countMex = accCount + batCount + bvpCount + edaCount + hrCount + ibiCount + tempCount;
        double avgPerc = (accPerc + batPerc + bvpPerc + edaPerc + hrPerc + ibiPerc + tempPerc) / 7.0;
        logger.info("[DEVICE] count:{} perc:{}", countMex, avgPerc);

        Device device = new Device("Empatica E4", getStatus(avgPerc), (int)countMex, 1.0 - avgPerc);

        return device;
    }

    public static double getPercentage(double count, double expected){
        return (1.0 * count) / expected;
    }

    public static Status getStatus(double percentage){
        if(percentage > 0.95){
            return Status.fine;
        }
        else if((percentage > 0.80) && (percentage <= 0.95)){
            return Status.ok;
        }
        else if((percentage > 0.50) && (percentage <= 0.80)){
            return Status.warning;
        }
        else if((percentage > 0.0) && (percentage <= 0.50)){
            return Status.bad;
        }
        else{
            return Status.disconnected;
        }
    }
}
