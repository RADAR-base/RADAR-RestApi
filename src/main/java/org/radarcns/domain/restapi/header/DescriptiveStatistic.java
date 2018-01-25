package org.radarcns.domain.restapi.header;

public enum DescriptiveStatistic {
  AVERAGE, COUNT, MAXIMUM, MEDIAN, MINIMUM, SUM, INTERQUARTILE_RANGE, LOWER_QUARTILE, UPPER_QUARTILE, QUARTILES, RECEIVED_MESSAGES  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"DescriptiveStatistic\",\"namespace\":\"org.radarcns.restapi.header\",\"doc\":\"Statical values.\",\"symbols\":[\"AVERAGE\",\"COUNT\",\"MAXIMUM\",\"MEDIAN\",\"MINIMUM\",\"SUM\",\"INTERQUARTILE_RANGE\",\"LOWER_QUARTILE\",\"UPPER_QUARTILE\",\"QUARTILES\",\"RECEIVED_MESSAGES\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
}