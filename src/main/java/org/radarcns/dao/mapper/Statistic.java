package org.radarcns.dao.mapper;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.radarcns.dao.serde.IsoDateDeserializer;
import org.radarcns.dao.serde.QuartileDeserializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Francesco Nobilia on 27/10/2016.
 */
public class Statistic {
    
    private transient String _id;

    @JsonProperty("user")
    private String user;

    @JsonProperty("source")
    private String source;

    @JsonProperty("min")
    private double min;

    @JsonProperty("max")
    private double max;

    @JsonProperty("sum")
    private double sum;

    @JsonProperty("count")
    private double count;

    @JsonProperty("avg")
    private double avg;

    @JsonProperty("quartile")
    @JsonDeserialize(using = QuartileDeserializer.class)
    private List<Double> quartile = new ArrayList<>();

    @JsonProperty("iqr")
    private double iqr;

    @JsonProperty("start")
    @JsonDeserialize(using = IsoDateDeserializer.class)
    private Date start;

    @JsonProperty("end")
    @JsonDeserialize(using = IsoDateDeserializer.class)
    private Date end;

    @JsonIgnore
    private Map<String, Object> otherProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> any() {
        return otherProperties;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        otherProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "_id='" + _id + '\'' +
                ", user='" + user + '\'' +
                ", source='" + source + '\'' +
                ", min=" + min +
                ", max=" + max +
                ", sum=" + sum +
                ", count=" + count +
                ", avg=" + avg +
                ", quartile=" + quartile +
                ", iqr=" + iqr +
                ", start=" + start +
                ", end=" + end +
                ", otherProperties=" + otherProperties +
                '}';
    }
}