package com.atlassian.jira.plugins.importer.sample;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.ScalarSerializerBase;
import org.joda.time.Period;

import java.io.IOException;

public class PeriodSerializer extends ScalarSerializerBase<Period> {

    protected PeriodSerializer() {
        super(Period.class);
    }

    @Override
    public void serialize(Period value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(value.toString());
    }

}
