package org.radarcns.integrationtest.util;

import static org.radarcns.integrationtest.util.Parser.Variable.TIMESTAMP;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecord;
import org.radarcns.integrationtest.config.MockDataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francesco on 14/02/2017.
 */
public class Parser {

    /** Information returned by the Parser. */
    public enum Variable {
        USER("userId"),
        SOURCE("sourceId"),
        TIME_WINDOW("timeWindow"),
        TIMESTAMP("timeReceived"),
        EXPECTED_TYPE("expectedType"),
        VALUE("value");

        private String value;

        Variable(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }

        public static Variable getEnum(String value) {
            for(Variable v : values())
                if(v.getValue().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }

        public static List<Variable> toList(){
            return new ArrayList<Variable>() {
                {
                    add(USER);
                    add(SOURCE);
                    add(TIME_WINDOW);
                    add(TIMESTAMP);
                    add(EXPECTED_TYPE);
                    add(VALUE);
                }
            };
        }
    }

    /** The type of a Expected Values. */
    public enum ExpectedType {
        ARRAY("org.radarcns.integrationtest.collector.ExpectedArrayValue"),
        DOUBLE("org.radarcns.integrationtest.collector.ExpectedDoubleValue");

        private String value;

        ExpectedType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.getValue();
        }

        public static ExpectedType getEnum(String value) {
            for(ExpectedType v : values())
                if(v.getValue().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }
    }

    private final Schema keySchema;
    private final Class<?> keyClass;

    private final Schema valueSchema;
    private final Class<?> valueClass;

    private final CSVReader csvReader;
    private final Map<String, Integer> headerMap;

    private final ExpectedType expecedType;

    private final MockDataConfig config;

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    public Parser(MockDataConfig config)
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
        IllegalAccessException, IOException {

        this.config = config;

        keyClass = Class.forName(config.getKeySchema());
        keySchema = (Schema) keyClass.getMethod("getClassSchema").invoke(null);
        SpecificData.newInstance(keyClass, keySchema);

        valueClass = Class.forName(config.getValueSchema());
        valueSchema = (Schema) valueClass.getMethod("getClassSchema").invoke(null);
        SpecificData.newInstance(valueClass, valueSchema);

        csvReader = new CSVReader(new FileReader(config.getCVSFile()));

        headerMap = new HashMap<>();
        getHeader();

        expecedType = getExpectedType(config);
    }

    public HashMap<Variable, Object> next() throws IOException {
        HashMap<Variable, Object> map = null;

        String[] rawValues = csvReader.readNext();
        if (rawValues != null) {
            map = new HashMap<>();

            for (Variable var : Variable.toList()){
                map.put(var, computeValue(rawValues, var, config));
            }
        }

        return map;
    }

    public void close() throws IOException {
        csvReader.close();
    }

    public ExpectedType getExpecedType() {
        return expecedType;
    }

    public static ExpectedType getExpectedType(MockDataConfig config){
        if ( config.getValuesToTest().contains(",") ) {
            return ExpectedType.ARRAY;
        }

        return ExpectedType.DOUBLE;
    }

    private Object computeValue(String[] rawValues, Variable var, MockDataConfig config) {
        switch (var) {
            case USER:
                existOrThrow(var.getValue());
                return rawValues[headerMap.get(var.getValue())];
            case SOURCE:
                existOrThrow(var.getValue());
                return rawValues[headerMap.get(var.getValue())];
            case TIMESTAMP:
                existOrThrow(var.getValue());
                return getTimestamp(rawValues[headerMap.get(var.getValue())]);
            case VALUE:
                return extractValue(rawValues, config);
            case TIME_WINDOW:
                return getStartTimeWindow(rawValues[headerMap.get(TIMESTAMP.getValue())]);
            case EXPECTED_TYPE:
                return expecedType;
            default:
                throw new IllegalArgumentException("Cannot handle variable of type "
                    + var.getValue());
        }
    }

    private Object extractValue(String[] rawValues, MockDataConfig config){
        if( expecedType.equals(ExpectedType.DOUBLE) ) {

            existOrThrow(config.getValuesToTest());

            return Double.parseDouble(rawValues[headerMap.get(config.getValuesToTest())]);
        }
        else if ( expecedType.equals(ExpectedType.ARRAY) ) {

            String[] testCase = config.getValuesToTest().split(", ");
            Double[] value = new Double[testCase.length];

            for(int i=0; i<testCase.length; i++){
                existOrThrow(testCase[i]);
                value[i] = Double.parseDouble(rawValues[headerMap.get(testCase[i])]);
            }

            return value;
        }

        throw new IllegalArgumentException("Illegale expected Type " + expecedType.getValue());
    }

    public void getHeader() throws IOException{
        String[] header = csvReader.readNext();

        for (int i = 0; i < header.length; i++) {
            headerMap.put(header[i], i);
        }
    }

    private SpecificRecord parseRecord(String[] rawValues, Class<?> recordClass, Schema schema) {
        SpecificRecord record = (SpecificRecord) SpecificData.newInstance(recordClass, schema);

        for (Field field : schema.getFields()) {
            String fieldString = rawValues[headerMap.get(field.name())];
            Object fieldValue = parseValue(field.schema(), fieldString);
            record.put(field.pos(), fieldValue);
        }

        return record;
    }

    private Object parseValue(Schema schema, String fieldString) {
        switch (schema.getType()) {
            case INT:
                return Integer.parseInt(fieldString);
            case LONG:
                return Long.parseLong(fieldString);
            case FLOAT:
                return Float.parseFloat(fieldString);
            case DOUBLE:
                return Double.parseDouble(fieldString);
            case BOOLEAN:
                return Boolean.parseBoolean(fieldString);
            case STRING:
                return fieldString;
            case ARRAY:
                return parseArray(schema, fieldString);
            default:
                throw new IllegalArgumentException("Cannot handle schemas of type "
                    + schema.getType());
        }
    }

    private List<Object> parseArray(Schema schema, String fieldString) {
        if (fieldString.charAt(0) != '['
            || fieldString.charAt(fieldString.length() - 1) != ']') {
            throw new IllegalArgumentException("Array must be enclosed by brackets.");
        }

        List<String> subStrings = new ArrayList<>();
        StringBuilder buffer = new StringBuilder(fieldString.length());
        int depth = 0;
        for (char c : fieldString.substring(1, fieldString.length() - 1).toCharArray()) {
            if (c == ';' && depth == 0) {
                subStrings.add(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append(c);
                if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                }
            }
        }
        if (buffer.length() > 0) {
            subStrings.add(buffer.toString());
        }

        List ret = new ArrayList(subStrings.size());
        for (String substring : subStrings) {
            ret.add(parseValue(schema.getElementType(), substring));
        }
        return ret;
    }

    private Long getStartTimeWindow(String value){
        Double timeDouble = Double.parseDouble(value) / 10d;
        return timeDouble.longValue() * 10000;
    }

    private Long getTimestamp(String value){
        Double timeDouble  = Double.valueOf(value) * 1000d;
        return timeDouble.longValue();
    }

    private void existOrThrow(String key){
        if ( !headerMap.containsKey(key) ) {
            throw new IllegalArgumentException("Headers does not contain " + key);
        }
    }
}