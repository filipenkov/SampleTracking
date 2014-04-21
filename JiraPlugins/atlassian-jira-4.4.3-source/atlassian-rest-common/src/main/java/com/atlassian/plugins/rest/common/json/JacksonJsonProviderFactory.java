package com.atlassian.plugins.rest.common.json;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.SerializationConfig;

public class JacksonJsonProviderFactory
{
    public JacksonJsonProvider create()
    {
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        // Make sure we only rely on annotations for de-/serialization
        provider.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
        provider.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, false);
        provider.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS, false);
        provider.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, false);
        return provider;
    }
}
