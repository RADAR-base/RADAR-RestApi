package org.radarcns.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.radarcns.avro.DescriptiveStatistic;
import org.radarcns.avro.EffectiveTimeFrame;
import org.radarcns.avro.HeartRate;
import org.radarcns.avro.HeartRateDataSet;
import org.radarcns.avro.HeartRateItem;
import org.radarcns.avro.HeartRateValue;
import org.radarcns.avro.Unit;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;

import static org.radarcns.dao.MongoDAO.findDocumentByUser;
import static org.radarcns.dao.MongoDAO.findDocumentByUserAndWindow;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class MongoHeartRateDAO {

    public static Logger logger = LoggerFactory.getLogger(MongoHeartRateDAO.class);

    /*public static String findDocumentByUserTest(String user, MongoCollection<Document> collection) {

        FindIterable<Document> result = collection.find(eq("user",user));

        List<Statistic> list = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        String encodeJSON = "";

        MongoCursor<Document> cursor = result.iterator();
        try {
            while (cursor.hasNext()) {

                String temp = cursor.next().toJson();
                System.out.println(temp);

                list.add(mapper.readValue(temp,Statistic.class));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mapper.writeValue(out, list);

            byte[] data = out.toByteArray();
            encodeJSON = new String(data);
        }
        catch (IOException e){
            logger.error(e.getMessage());
        }
        finally {
            cursor.close();
        }

        return encodeJSON;
    }*/

    public static HeartRateDataSet avgByUser(String user, MongoCollection<Document> collection) {

        MongoCursor<Document> cursor = findDocumentByUser(user,"start",1,collection);

        Date start = null;
        Date end = null;

        LinkedList<HeartRateItem> list = new LinkedList<>();

        while (cursor.hasNext()) {

            Document doc = cursor.next();
            logger.debug(doc.toJson().toString());

            if(start == null){
                start = doc.getDate("start");
            }
            end = doc.getDate("end");

            EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(doc.getDate("start")),RadarConverter.getISO8601(doc.getDate("end")));

            list.addLast(new HeartRateItem(doc.getDouble("avg"),etf));
        }

        EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(start),RadarConverter.getISO8601(end));

        HeartRateDataSet hrd = new HeartRateDataSet(list,etf,DescriptiveStatistic.average,Unit.beats_per_min);

        return hrd;
    }

    public static HeartRate avgByUserWindow(String user, Long start, Long end, MongoCollection<Document> collection) {

        MongoCursor<Document> cursor = findDocumentByUserAndWindow(user,start,end,collection);

        while (cursor.hasNext()) {

            Document doc = cursor.next();

            logger.debug(doc.toJson());

            HeartRateValue hrv = new HeartRateValue(doc.getDouble("avg"), Unit.beats_per_min);

            EffectiveTimeFrame etf = new EffectiveTimeFrame(RadarConverter.getISO8601(doc.getDate("start")),RadarConverter.getISO8601(doc.getDate("end")));

            HeartRate hr = new HeartRate(hrv, etf, DescriptiveStatistic.average);

            return hr;
        }

        return null;
    }

}