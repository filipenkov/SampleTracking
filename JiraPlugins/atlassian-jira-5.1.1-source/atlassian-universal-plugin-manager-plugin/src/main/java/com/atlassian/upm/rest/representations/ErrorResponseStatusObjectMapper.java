package com.atlassian.upm.rest.representations;

import java.io.IOException;

import com.atlassian.plugins.rest.common.Status;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * A custom extension of the Jackson {@code ObjectMapper} that adds a custom object mapping from a {@code Status}
 * object to a custom {@code JsonSerializer}. The JSON Serializer will serialize an {@code ErrorRepresentation} object
 * from the mapped {@code Status} object.
 */
public class ErrorResponseStatusObjectMapper extends ObjectMapper
{
    private final RepresentationFactory representationFactory;

    public ErrorResponseStatusObjectMapper(RepresentationFactory representationFactory)
    {
        this.representationFactory = representationFactory;

        // Create a custom serializer factory to handle com.atlassian.plugins.rest.common.Status response entities
        CustomSerializerFactory sf = new CustomSerializerFactory();
        sf.addSpecificMapping(Status.class, new StatusToErrorRepresentationSerializer());
        setSerializerFactory(sf);
    }

    /**
     * Implementation of a JSON serializer that generates an {@code ErrorRepresentation} object from a {@code Status}
     * object. This class is needed for Jackson to properly serialize {@code Status} objects returned from the
     * response entity. The {@code Status} object must be of type {@code CLIENT_ERROR} or {@code SERVER_ERROR}
     */
    private final class StatusToErrorRepresentationSerializer extends JsonSerializer<Status>
    {
        /*
        * (non-Javadoc)
        *
        * @see org.codehaus.jackson.map.JsonSerializer#serialize(java.lang.Object,
        * org.codehaus.jackson.JsonGenerator,
        * org.codehaus.jackson.map.SerializerProvider)
        */
        @Override
        public void serialize(Status value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            if (isEmpty(value.getMessage()))
            {
                jgen.writeObject(representationFactory.createI18nErrorRepresentation("upm.plugin.error.unexpected.error"));
            }
            else
            {
                jgen.writeObject(representationFactory.createErrorRepresentation(value.getMessage(), "upm.plugin.error.unexpected.error"));
            }
        }
    }
}
