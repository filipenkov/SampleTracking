package com.atlassian.upm.rest.representations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.SerializationConfig;

import static com.atlassian.upm.rest.MediaTypes.AUDIT_LOG_ENTRIES_JSON;
import static com.atlassian.upm.rest.MediaTypes.AUDIT_LOG_MAX_ENTRIES_JSON;
import static com.atlassian.upm.rest.MediaTypes.AUDIT_LOG_PURGE_AFTER_JSON;
import static com.atlassian.upm.rest.MediaTypes.AVAILABLE_FEATURED_JSON;
import static com.atlassian.upm.rest.MediaTypes.AVAILABLE_PLUGINS_COLLECTION_JSON;
import static com.atlassian.upm.rest.MediaTypes.AVAILABLE_PLUGIN_JSON;
import static com.atlassian.upm.rest.MediaTypes.BUILD_NUMBER_JSON;
import static com.atlassian.upm.rest.MediaTypes.CANCELLABLE_TASK_JSON;
import static com.atlassian.upm.rest.MediaTypes.CHANGES_REQUIRING_RESTART_JSON;
import static com.atlassian.upm.rest.MediaTypes.COMPATIBILITY_JSON;
import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALLED_PLUGINS_COLLECTION_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALLED_PLUGIN_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_NEXT_TASK_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_COMPLETE_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_DOWNLOADING_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_ERR_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_INSTALLING_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_URI_JSON;
import static com.atlassian.upm.rest.MediaTypes.OSGI_BUNDLE_COLLECTION_JSON;
import static com.atlassian.upm.rest.MediaTypes.OSGI_BUNDLE_JSON;
import static com.atlassian.upm.rest.MediaTypes.OSGI_PACKAGE_COLLECTION_JSON;
import static com.atlassian.upm.rest.MediaTypes.OSGI_PACKAGE_JSON;
import static com.atlassian.upm.rest.MediaTypes.OSGI_SERVICE_COLLECTION_JSON;
import static com.atlassian.upm.rest.MediaTypes.OSGI_SERVICE_JSON;
import static com.atlassian.upm.rest.MediaTypes.PAC_STATUS_JSON;
import static com.atlassian.upm.rest.MediaTypes.PAC_MODE_JSON;
import static com.atlassian.upm.rest.MediaTypes.PAC_BASE_URL_JSON;
import static com.atlassian.upm.rest.MediaTypes.PENDING_TASKS_COLLECTION_JSON;
import static com.atlassian.upm.rest.MediaTypes.PENDING_TASK_JSON;
import static com.atlassian.upm.rest.MediaTypes.PLUGIN_MODULE_JSON;
import static com.atlassian.upm.rest.MediaTypes.POPULAR_PLUGINS_JSON;
import static com.atlassian.upm.rest.MediaTypes.PRODUCT_UPDATES_JSON;
import static com.atlassian.upm.rest.MediaTypes.SAFE_MODE_ERROR_REENABLING_PLUGIN_JSON;
import static com.atlassian.upm.rest.MediaTypes.SAFE_MODE_ERROR_REENABLING_PLUGIN_MODULE_JSON;
import static com.atlassian.upm.rest.MediaTypes.SAFE_MODE_FLAG_JSON;
import static com.atlassian.upm.rest.MediaTypes.SUPPORTED_PLUGINS_JSON;
import static com.atlassian.upm.rest.MediaTypes.TASK_ERROR_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATES_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_COMPLETE_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_DOWNLOADING_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_ERR_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_FINDING_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_UPDATING_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPM_JSON;
import static com.atlassian.upm.rest.MediaTypes.PAC_DETAILS;

/**
 * {@code JsonProvider} is an implementation of the {@code MessageBodyReader} and {@code MessageBodyWriter} interfaces.
 * It provides serialization and deserialization of objects annotated with Jackson annotations.  The implementation
 * simply wraps the {@link JacksonJsonProvider} which would not be loaded by the REST module otherwise.
 */
@Provider
@Produces({
    INSTALL_DOWNLOADING_JSON,
    INSTALL_NEXT_TASK_JSON,
    INSTALL_COMPLETE_JSON,
    INSTALL_INSTALLING_JSON,
    INSTALL_ERR_JSON,
    TASK_ERROR_JSON,
    ERROR_JSON,
    PENDING_TASK_JSON,
    PENDING_TASKS_COLLECTION_JSON,
    UPDATE_ALL_FINDING_JSON,
    UPDATE_ALL_DOWNLOADING_JSON,
    UPDATE_ALL_UPDATING_JSON,
    UPDATE_ALL_COMPLETE_JSON,
    UPDATE_ALL_ERR_JSON,
    INSTALLED_PLUGINS_COLLECTION_JSON,
    INSTALL_URI_JSON,
    COMPATIBILITY_JSON,
    INSTALLED_PLUGIN_JSON,
    PLUGIN_MODULE_JSON,
    AVAILABLE_FEATURED_JSON,
    AVAILABLE_PLUGIN_JSON,
    AVAILABLE_PLUGINS_COLLECTION_JSON,
    UPDATES_JSON,
    PRODUCT_UPDATES_JSON,
    POPULAR_PLUGINS_JSON,
    SUPPORTED_PLUGINS_JSON,
    SAFE_MODE_FLAG_JSON,
    SAFE_MODE_ERROR_REENABLING_PLUGIN_JSON,
    SAFE_MODE_ERROR_REENABLING_PLUGIN_MODULE_JSON,
    CHANGES_REQUIRING_RESTART_JSON,
    AUDIT_LOG_ENTRIES_JSON,
    AUDIT_LOG_MAX_ENTRIES_JSON,
    AUDIT_LOG_PURGE_AFTER_JSON,
    OSGI_BUNDLE_COLLECTION_JSON,
    OSGI_BUNDLE_JSON,
    OSGI_SERVICE_COLLECTION_JSON,
    OSGI_SERVICE_JSON,
    OSGI_PACKAGE_JSON,
    OSGI_PACKAGE_COLLECTION_JSON,
    BUILD_NUMBER_JSON,
    CANCELLABLE_TASK_JSON,
    PAC_BASE_URL_JSON,
    PAC_STATUS_JSON,
    PAC_MODE_JSON,
    PAC_DETAILS,
    UPM_JSON
})
public class JsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{
    private final JacksonJsonProvider provider = new JacksonJsonProvider();

    @SuppressWarnings("deprecation")
    public JsonProvider(RepresentationFactory representationFactory)
    {
        provider.setMapper(new ErrorResponseStatusObjectMapper(representationFactory));
        provider.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
        provider.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, false);
        // the WRITE_NULL_PROPERTIES feature is deprecated, but there is no way to get to the configuration without
        // extending the JacksonJsonProvider
        provider.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);

        provider.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ----------------- delegates for MessageBodyWriter

    public long getSize(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return provider.getSize(value, type, genericType, annotations, mediaType);
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return provider.isWriteable(type, genericType, annotations, mediaType);
    }

    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
    {
        provider.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    // ----------------- delegates for MessageBodyReader

    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return provider.isReadable(type, genericType, annotations, mediaType);
    }

    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException
    {
        return provider.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
}
