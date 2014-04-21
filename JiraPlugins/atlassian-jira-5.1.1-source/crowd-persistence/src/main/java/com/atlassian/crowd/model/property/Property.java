package com.atlassian.crowd.model.property;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Captures various server properties of the crowd server.
 */
public class Property implements Serializable
{
    /**
     * Parent Property Key for all Crowd Server properties.
     */
    public static final String CROWD_PROPERTY_KEY = "crowd";

    /**
     * Server Property: Cache time.
     */
    public static final String CACHE_TIME = "cache.time";

    /**
     * Server Property: Custom token seed.
     */
    public static final String TOKEN_SEED = "token.seed";

    /**
     * Server Property: Deployment title.
     */
    public static final String DEPLOYMENT_TITLE = "deployment.title";

    /**
     * Server Property: Deployment domain.
     */
    public static final String DOMAIN = "domain";

    /**
     * Server Property: Cache enabled.
     */
    public static final String CACHE_ENABLED = "cache.enabled";

    /**
     * Server Property: Session time;
     */
    public static final String SESSION_TIME = "session.time";

    /**
     * Server Property: Mail server host.
     */
    public static final String MAILSERVER_HOST = "mailserver.host";

    /**
     * Server Property: Mail server prefix.
     */
    public static final String MAILSERVER_PREFIX = "mailserver.prefix";

    /**
     * Server Property: Mail server sender.
     */
    public static final String MAILSERVER_SENDER = "mailserver.sender";

    /**
     * Server Property: Mail server username.
     */
    public static final String MAILSERVER_USERNAME = "mailserver.username";

    /**
     * Server Property: Mail server password.
     */
    public static final String MAILSERVER_PASSWORD = "mailserver.password";

    /**
     * Server Property: Forgotten password email template
     */
    public static final String FORGOTTEN_PASSWORD_EMAIL_TEMPLATE = "mailserver.message.template";

    /**
     * Server Property: Forgotten usernames email template
     */
    public static final String FORGOTTEN_USERNAME_EMAIL_TEMPLATE = "email.template.forgotten.username";

    /**
     * Server Property: Server encryption key.
     */
    public static final String DES_ENCRYPTION_KEY = "des.encryption.key";

    /**
     * Server Property: Total number of 'current' resources towards the license resource limit, ie number of users.
     */
    public static final String CURRENT_LICENSE_RESOURCE_TOTAL = "current.license.resource.total";

    /**
     * Server Property: Email address for the administrator when server notifications occur.
     */
    public static final String NOTIFICATION_EMAIL = "notification.email";

    /**
     * Server Property: The current build number for Crowd.
     */
    public static final String BUILD_NUMBER = "build.number";

    /**
     * Server Property: True if GZip compression is enabled on the response.
     */
    public static final String GZIP_ENABLED = "gzip.enabled";

    /**
 	* Server Property: A comma-delimited list of trusted proxy servers.
    */
    public static final String TRUSTED_PROXY_SERVERS = "trusted.proxy.servers";

    /**
     * Server Property: Whether or not Crowd is using database token storage
     */
    public static final String DATABASE_TOKEN_STORAGE_ENABLED = "database.token.storage.enabled";

    /**
     * Server Property: Mail Server JNDI Location
     */
    public static final String MAILSERVER_JNDI_LOCATION = "mailserver.jndi";

    /**
     * Server Property: Mail Server SMTP port
     */
    public static final String MAILSERVER_PORT = "mailserver.port";

    /**
     * Server Property: Mail Server SMTP Use SSL
     */
    public static final String MAILSERVER_USE_SSL = "mailserver.usessl";


    /**
     * Server Property: SSO Cookie set to "Secure"
     */
    public static final String SECURE_COOKIE = "secure.cookie";
    private PropertyId propertyId;
    private String value;

    public Property(final String key, final String name, final String value)
    {
        Validate.notNull(key, "key cannot be null");
        Validate.notNull(key, "name cannot be null");
        Validate.notNull(key, "value cannot be null");
        this.propertyId = new PropertyId();
        this.propertyId.setKey(key);
        this.propertyId.setName(name);
        this.value = value;
    }

    protected Property()
    {
    }

    private PropertyId getPropertyId()
    {
        return propertyId;
    }

    private void setPropertyId(final PropertyId propertyId)
    {
        this.propertyId = propertyId;
    }

    public String getKey()
    {
        return propertyId.getKey();
    }

    public String getName()
    {
        return propertyId.getName();
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(final String value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;

        Property property = (Property) o;

        if (propertyId != null ? !propertyId.equals(property.propertyId) : property.propertyId != null) return false;
        if (value != null ? !value.equals(property.value) : property.value != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = propertyId != null ? propertyId.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("key", getKey()).
                append("name", getName()).
                append("value", getValue()).
                toString();
    }
}
