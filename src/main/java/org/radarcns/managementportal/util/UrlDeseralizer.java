package org.radarcns.managementportal.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

/*
 * Copyright 2017 King's College London
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

/**
 * Helper Java class for deserializing URLs with no protocol in their path.
 */
public class UrlDeseralizer extends JsonDeserializer<URL> {

    private Pattern urlPrefix = Pattern.compile("^(https?://|ftp://).*");

    @Override
    public URL deserialize(JsonParser parser, DeserializationContext ctxt) throws
            IOException, JsonProcessingException {
        ObjectCodec objectCodec = parser.getCodec();
        JsonNode node = objectCodec.readTree(parser);
        String stringUrl = node.asText();
        if (!urlPrefix.matcher(stringUrl).matches()) {
            return new URL("http://" + stringUrl);
        } else {
            return new URL(stringUrl);
        }
    }
}


