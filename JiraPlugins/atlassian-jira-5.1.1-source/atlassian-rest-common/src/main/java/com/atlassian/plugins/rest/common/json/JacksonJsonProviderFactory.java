package com.atlassian.plugins.rest.common.json;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class JacksonJsonProviderFactory
{
    public JacksonJsonProvider create()
    {
        ObjectMapper mapper = new ObjectMapper();

        /* This is what MapperConfigurator would do to a default ObjectMapper */
        AnnotationIntrospector intr = AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(),  new JaxbAnnotationIntrospector());
        mapper.setDeserializationConfig(mapper.getDeserializationConfig().withAnnotationIntrospector(intr));
        mapper.setSerializationConfig(mapper.getSerializationConfig().withAnnotationIntrospector(intr));

        /* In the absence of a specific annotation for @JsonSerialize(include), ignore null fields when serializing */
        mapper.setSerializationInclusion(Inclusion.NON_NULL);

        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider(mapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);

        // Make sure we only rely on annotations for de-/serialization
        provider.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
        provider.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, false);
        provider.configure(DeserializationConfig.Feature.AUTO_DETECT_SETTERS, false);
        provider.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, false);
        return provider;
    }
}
