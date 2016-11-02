package org.radarcns.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import org.bson.Document;
import org.radarcns.dao.mapper.Statistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Francesco Nobilia on 20/10/2016.
 */
public class MongodbDAO {

    public static Logger logger = LoggerFactory.getLogger(MongodbDAO.class);

    public static String findDocumentByUser(String user, MongoCollection<Document> collection) {

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
    }
}