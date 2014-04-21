package com.atlassian.applinks.core.property;

import com.atlassian.applinks.api.PropertySet;
import com.atlassian.applinks.spi.application.TypeId;
import com.google.common.base.Preconditions;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A wrapper around a property set that stores properties for one application link.
 *
 * @since 3.0
 */
public class ApplicationLinkProperties
{
    private static final String AUTH_PROVIDER_PREFIX = "auth";
    private static final EnumSet<Property> STANDARD_PROPERTIES = EnumSet.allOf(Property.class);
    private final Lock customPropertyWriteLock = new ReentrantLock();
    private final Lock authenticationProviderWriteLock = new ReentrantLock();
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLinkProperties.class.getName());

    /*Public for testing*/
    public enum Property
    {
        TYPE("type"),
        NAME("name"),
        DISPLAY_URL("display.url"),
        RPC_URL("rpc.url"),
        PRIMARY("primary"),
        AUTH_PROVIDER_KEYS("providerKeys"),
        PROPERTY_KEYS("propertyKeys");

        private final String key;

        Property(String key){
            this.key = key;
        }
        public String key(){ return key; }
    }

    private final PropertySet applinksAdminPropertySet;
    private final PropertySet applinksPropertySet;

    public ApplicationLinkProperties(final PropertySet applinksAdminPropertySet, final PropertySet applinksPropertySet)
    {
        this.applinksAdminPropertySet = applinksAdminPropertySet;
        this.applinksPropertySet = applinksPropertySet;
    }

    /**
     * Copies all values from the supplied
     * {@link com.atlassian.applinks.core.property.ApplicationLinkProperties}
     * object to this instance.
     *
     * @param props must not be {@code null}.
     */
    public void setProperties(final ApplicationLinkProperties props)
    {
        Preconditions.checkNotNull(props, "props must not be null");
        for (final Property propertyKey : EnumSet.allOf(Property.class))
        {
            final Object value = props.applinksAdminPropertySet.getProperty(propertyKey.key());
            if (value != null)
            {
                applinksAdminPropertySet.putProperty(propertyKey.key(), value);
                if (propertyKey == Property.PROPERTY_KEYS)
                {
                    customPropertyWriteLock.lock();
                    try
                    {
                        // custom user properties that are stored in applinksPropertySet
                        for (final String customPropertyKey : (List<String>) value)
                        {
                            applinksPropertySet.putProperty(customPropertyKey,
                                    props.applinksPropertySet.getProperty(customPropertyKey));
                        }
                    }
                    finally
                    {
                        customPropertyWriteLock.unlock();
                    }
                }
                else if (propertyKey == Property.AUTH_PROVIDER_KEYS)
                {
                    authenticationProviderWriteLock.lock();
                    try
                    {
                        for (final String authProviderKey : (List<String>) value)
                        {
                            applinksAdminPropertySet.putProperty(hashedAuthProviderKey(authProviderKey),
                                    props.applinksAdminPropertySet.getProperty(hashedAuthProviderKey(authProviderKey)));
                        }
                    }
                    finally
                    {
                        authenticationProviderWriteLock.unlock();
                    }
                }
            }
        }
    }

    private String hashedAuthProviderKey(final String authProviderKey)
    {
        return AUTH_PROVIDER_PREFIX + "." + createHashedProviderKey(authProviderKey);
    }

    private String createHashedProviderKey(final String providerKey)
    {
        String hashedValue = DigestUtils.md5Hex(providerKey);
        LOG.debug("Created hash for authentication provider key: '" + providerKey + "' digest: '" + hashedValue + "'");
        return hashedValue;
    }

    public TypeId getType()
    {
        final String id = (String) applinksAdminPropertySet.getProperty(Property.TYPE.key());
        return id != null ? new TypeId(id) : null;
    }

    public void setType(final TypeId type)
    {
        applinksAdminPropertySet.putProperty(Property.TYPE.key(), type.get());
    }

    public String getName()
    {
        return (String) applinksAdminPropertySet.getProperty(Property.NAME.key());
    }

    public void setName(final String name)
    {
        applinksAdminPropertySet.putProperty(Property.NAME.key(), name.trim());
    }

    public URI getDisplayUrl()
    {
        return getUri(Property.DISPLAY_URL.key());
    }

    public void setDisplayUrl(final URI url)
    {
        setUri(Property.DISPLAY_URL.key(), url);
    }

    public URI getRpcUrl()
    {
        return getUri(Property.RPC_URL.key());
    }

    public void setRpcUrl(final URI url)
    {
        setUri(Property.RPC_URL.key(), url);
    }

    public boolean isPrimary()
    {
        return Boolean.valueOf((String) applinksAdminPropertySet.getProperty(Property.PRIMARY.key()));
    }

    public void setIsPrimary(final boolean isPrimary)
    {
        applinksAdminPropertySet.putProperty(Property.PRIMARY.key(), String.valueOf(isPrimary));
    }

    public void remove()
    {
        // TODO: maybe shell out to AuthenticationConfigurationManager.unregister()
        //Delete authentication provider configurations first
        try
        {
            authenticationProviderWriteLock.lock();
            final List<String> providerKeys = getProviderKeys();
            for (final String providerKey : providerKeys)
            {
                applinksAdminPropertySet.removeProperty(hashedAuthProviderKey(providerKey));
            }
            setProviderKeys(Collections.<String>emptyList());
        }
        finally
        {
            authenticationProviderWriteLock.unlock();
        }
        //Now we delete the properties of this application link.
        try
        {
            customPropertyWriteLock.lock();
            for (final String key : getCustomPropertyKeys())
            {
                applinksPropertySet.removeProperty(key);
            }
            setPropertyKeys(Collections.<String>emptyList());
        }
        finally
        {
            customPropertyWriteLock.unlock();
        }
        //Now we delete the properties of this application link itself.
        for (Property standardProperty : STANDARD_PROPERTIES)
        {
            applinksAdminPropertySet.removeProperty(standardProperty.key());
        }
    }

    public void setProviderConfig(final String providerKey, final Map<String, String> config)
    {
        applinksAdminPropertySet.putProperty(hashedAuthProviderKey(providerKey), toProperties(config));
        try
        {
            authenticationProviderWriteLock.lock();
            final List<String> providerKeys = getProviderKeys();
            if (!providerKeys.contains(providerKey))
            {
                providerKeys.add(providerKey);
                setProviderKeys(providerKeys);
            }
        }
        finally
        {
            authenticationProviderWriteLock.unlock();
        }
    }

    public void removeProviderConfig(final String providerKey)
    {
        applinksAdminPropertySet.removeProperty(hashedAuthProviderKey(providerKey));
        try
        {
            authenticationProviderWriteLock.lock();
            final List<String> providerKeys = getProviderKeys();
            providerKeys.remove(providerKey);
            setProviderKeys(providerKeys);
        }
        finally
        {
            authenticationProviderWriteLock.unlock();
        }
    }

    public Map<String, String> getProviderConfig(final String providerKey)
    {
        final Object obj = applinksAdminPropertySet.getProperty(hashedAuthProviderKey(providerKey));
        if (obj == null || !(obj instanceof Properties))
        {
            return null;
        }
        else
        {
            return toMap((Properties) obj);
        }
    }

    /**
     * public access to facilitate upgrade tasks
     *
     * @return a {@link List} of authentication provider keys registered to the application link
     */
    @SuppressWarnings("unchecked")
    public List<String> getProviderKeys()
    {
        List<String> list = (List<String>) applinksAdminPropertySet.getProperty(Property.AUTH_PROVIDER_KEYS.key());
        if (list == null)
        {
            list = new ArrayList<String>();
        }
        return list;
    }

    public Object getProperty(final String key)
    {
        return applinksPropertySet.getProperty(key);
    }

    public Object putProperty(final String key, final Object value)
    {
        final Object oldValue = applinksPropertySet.putProperty(key, value);
        try
        {
            customPropertyWriteLock.lock();
            final List<String> propertyKeys = getCustomPropertyKeys();
            if (!propertyKeys.contains(key))
            {
                propertyKeys.add(key);
                setPropertyKeys(propertyKeys);
            }
        }
        finally
        {
            customPropertyWriteLock.unlock();
        }
        return oldValue;
    }

    public Object removeProperty(final String key)
    {
        final Object removedValue = applinksPropertySet.removeProperty(key);
        if (removedValue != null)
        {
            try
            {
                customPropertyWriteLock.lock();
                final List<String> properties = getCustomPropertyKeys();
                properties.remove(key);
                setPropertyKeys(properties);
            }
            finally
            {
                customPropertyWriteLock.unlock();
            }
        }
        return removedValue;
     }

    private List<String> getCustomPropertyKeys()
    {
        List<String> customPropertyKeys = (List<String>) applinksAdminPropertySet.getProperty(Property.PROPERTY_KEYS.key());
        if (customPropertyKeys == null)
        {
            customPropertyKeys = new ArrayList<String>();
        }
        return customPropertyKeys;
    }

    private void setPropertyKeys(List<String> customPropertyKeys)
    {
        applinksAdminPropertySet.putProperty(Property.PROPERTY_KEYS.key(), customPropertyKeys);
    }

    private void setUri(final String key, final URI uri)
    {
        applinksAdminPropertySet.putProperty(key, uri != null ? uri.toString() : null);
    }

    private URI getUri(final String key)
    {
        final String uri = (String) applinksAdminPropertySet.getProperty(key);
        if (uri == null)
        {
            return null;
        }
        try
        {
            return new URI(uri);
        }
        catch (URISyntaxException e)
        {
            // this will only happen if someone has manually hacked up the property store, and set an invalid URI
            throw new RuntimeException(String.format("Failed to deserialise stored %s URI (%s) reason: %s",
                    key, uri, e.getReason()));
        }
    }

    private void setProviderKeys(final List<String> providerKeys)
    {
        applinksAdminPropertySet.putProperty(Property.AUTH_PROVIDER_KEYS.key(), providerKeys);
    }

    public boolean authProviderIsConfigured(final String providerKey)
    {
        return getProviderKeys().contains(providerKey);
    }

    private Properties toProperties(final Map<String, String> map)
    {
        final Properties props = new Properties();
        props.putAll(map);
        return props;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> toMap(final Properties props)
    {
        return Collections.unmodifiableMap(new HashMap<String, String>((Map) props));
    }

}
