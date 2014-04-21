package com.atlassian.jira.plugins.importer.sample;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdScalarDeserializer;
import org.joda.time.Period;

import java.io.IOException;

public class PeriodDeserializer extends StdScalarDeserializer<Period> {

    protected PeriodDeserializer() {
        super(Period.class);
    }

    @Override
    public Period deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            String str = jp.getText().trim();
            if (str.length() == 0) { // [JACKSON-360]
                return null;
            }
            return Period.parse(str);
        }
        throw ctxt.mappingException(_valueClass);
    }

}
