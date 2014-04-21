package com.atlassian.plugins.rest.common.json;

import org.codehaus.jackson.JsonEncoding;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DefaultJaxbJsonMarshaller implements JaxbJsonMarshaller
{
    public String marshal(Object jaxbBean)
    {
        try
        {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            // Don't use JsonGenerator directly as we want to make sure we use the same
            // configuration as for REST requests in OsgiResourceConfig
            new JacksonJsonProviderFactory().create().writeTo(jaxbBean, jaxbBean.getClass(), null, null, MediaType.APPLICATION_JSON_TYPE, null, os);
            // The encoding used inside JacksonJsonProvider is always UTF-8
            return new String(os.toByteArray(), JsonEncoding.UTF8.getJavaName());
        }
        catch (IOException e)
        {
            throw new JsonMarshallingException(e);
        }
    }

    @Deprecated
    public String marshal(final Object jaxbBean, final Class... jaxbClasses) throws JAXBException
    {
        return marshal(jaxbBean);
    }
}
