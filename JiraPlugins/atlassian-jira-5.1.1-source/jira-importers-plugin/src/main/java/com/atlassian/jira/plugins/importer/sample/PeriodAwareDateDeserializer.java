package com.atlassian.jira.plugins.importer.sample;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ext.JodaDeserializers;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;

public class PeriodAwareDateDeserializer extends JodaDeserializers.DateTimeDeserializer<DateTime> {

    public PeriodAwareDateDeserializer() {
        super(DateTime.class);
    }

    @Override
    public DateTime deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            String str = jp.getText().trim();
            if (str.length() == 0) { // [JACKSON-360]
                return null;
            }
            try {
                Period p = Period.parse(str);
                if (p != null) {
                    return new DateTime().minus(p);
                }
            } catch(IllegalArgumentException e) {
                // just ignore
            }
        }
        return super.deserialize(jp, deserializationContext);
    }

}
