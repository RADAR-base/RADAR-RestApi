package org.radarcns.util;

/*
 * Copyright 2016 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.radarcns.status.CsvData;


/**
 * Provider class for producing csv media type in a http response.
 */
@Provider
@Produces("text/csv")
public class CsvMessageBodyWriter implements MessageBodyWriter {

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        boolean ret = List.class.isAssignableFrom(type);
        return ret;
    }

    @Override
    public long getSize(Object data, Class type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Object o, Class type, Type genericType, Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException,
            WebApplicationException {
        List<CsvData> data = (List<CsvData>) o;
        if (data != null && data.size() > 0) {
            CsvMapper mapper = new CsvMapper();
            Object ob = data.get(0);
            CsvSchema schema = mapper.schemaFor(ob.getClass()).withHeader();
            mapper.writer(schema).writeValue(entityStream, data);
        }
    }
}