package org.radarcns.domain.restapi;

public enum TimeWindow {
  TEN_SECOND, ONE_MIN, TEN_MIN, ONE_HOUR, ONE_DAY, ONE_WEEK, UNKNOWN  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"TimeWindow\",\"namespace\":\"org.radarcns.catalogue\",\"doc\":\"Time window for aggregated measurements.\",\"symbols\":[\"TEN_SECOND\",\"ONE_MIN\",\"TEN_MIN\",\"ONE_HOUR\",\"ONE_DAY\",\"ONE_WEEK\",\"UNKNOWN\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
}