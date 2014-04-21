package com.atlassian.crowd.manager.property;

import com.atlassian.crowd.dao.property.PropertyDAO;
import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.model.property.Property;
import static com.atlassian.crowd.model.property.Property.*;
import com.atlassian.crowd.password.encoder.DESPasswordEncoder;
import com.atlassian.crowd.util.mail.SMTPServer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class PropertyManagerGeneric implements PropertyManager
{
    private PropertyDAO propertyDAO;
    private static final Logger logger = LoggerFactory.getLogger(PropertyManagerGeneric.class);
    private static final int DEFAULT_SESSION_TIME_IN_MINUTES = 5;
    private static final long MILLIS_IN_MINUTE = 60000;

    public PropertyManagerGeneric(PropertyDAO propertyDAO)
    {
        this.propertyDAO = propertyDAO;
    }

    public long getCacheTime() throws PropertyManagerException
    {
        // method is deprecated
        throw new PropertyManagerException("Cache Time is no longer supported. Crowd 1.0.2.");
    }

    public void setCacheTime(long cacheTime)
    {
        //comes in as minutes, store in milliseconds
        setProperty(CACHE_TIME, Long.toString(cacheTime * MILLIS_IN_MINUTE));
    }

    public String getTokenSeed() throws PropertyManagerException
    {
        try
        {
            Property property = getPropertyObject(TOKEN_SEED);

            return property.getValue();

        }
        catch (Exception e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    public void setTokenSeed(String seed)
    {
        setProperty(TOKEN_SEED, seed);
    }

    public String getDeploymentTitle() throws PropertyManagerException
    {
        try
        {
            Property property = getPropertyObject(DEPLOYMENT_TITLE);

            return property.getValue();

        }
        catch (Exception e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    public void setDeploymentTitle(String title)
    {
        setProperty(DEPLOYMENT_TITLE, title);
    }

    public String getDomain() throws PropertyManagerException
    {
        String domain = null;
        try
        {
            Property property = getPropertyObject(DOMAIN);

            domain = property.getValue();

        }
        catch (ObjectNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Failed to find a domain property.");
            }
        }

        return domain;
    }

    public void setDomain(String domain)
    {
        setProperty(DOMAIN, domain);
    }

    public boolean isSecureCookie()
    {
        try
        {
            Property property = getPropertyObject(SECURE_COOKIE);

            return Boolean.parseBoolean(property.getValue());

        }
        catch (Exception e)
        {
            return false;
        }
    }

    public void setSecureCookie(boolean secure)
    {
        setProperty(SECURE_COOKIE, Boolean.toString(secure));
    }

    public void setCacheEnabled(boolean enabled)
    {
        setProperty(CACHE_ENABLED, Boolean.toString(enabled));
    }

    public boolean isCacheEnabled()
    {
        try
        {
            Property property = getPropertyObject(CACHE_ENABLED);

            return Boolean.parseBoolean(property.getValue());

        }
        catch (Exception e)
        {
            return false;
        }
    }

    public long getSessionTime()
    {
        try
        {
            Property property = getPropertyObject(SESSION_TIME);

            // stored in milliseconds, pass back as minutes
            long b = Long.parseLong(property.getValue());
            return (b / MILLIS_IN_MINUTE);

        }
        catch (Exception e)
        {
            // default session time to five minutes if not set yet
            return DEFAULT_SESSION_TIME_IN_MINUTES;
        }
    }

    public void setSessionTime(long time)
    {
        //comes in as minutes, store in milliseconds
        setProperty(SESSION_TIME, Long.toString(time * MILLIS_IN_MINUTE));
    }

    public SMTPServer getSMTPServer() throws PropertyManagerException
    {
        // Required Field
        InternetAddress fromAddress;
        try
        {
            fromAddress = new InternetAddress(getPropertyObject(MAILSERVER_SENDER).getValue());
        }
        catch (Exception e)
        {
            throw new PropertyManagerException(e);
        }

        String prefix = null;
        try
        {
            prefix = getPropertyObject(MAILSERVER_PREFIX).getValue();
        }
        catch (ObjectNotFoundException ignored)
        {
            // Optional field
        }

        SMTPServer server;
        try
        {
            server = buildJNDIServer(fromAddress, prefix);
        }
        catch (ObjectNotFoundException ignored)
        {
            server = buildSMTPServer(fromAddress, prefix);
        }

        return server;
    }

    private SMTPServer buildJNDIServer(InternetAddress fromAddress, String prefix)
            throws ObjectNotFoundException
    {
        String jndiLocation = getPropertyObject(MAILSERVER_JNDI_LOCATION).getValue();

        if (StringUtils.isNotBlank(jndiLocation))
        {
            return new SMTPServer(jndiLocation, fromAddress, prefix);
        }
        else
        {
            throw new ObjectNotFoundException(Property.class, "JNDI location");
        }

    }

    private SMTPServer buildSMTPServer(InternetAddress fromAddress, String prefix)
            throws PropertyManagerException
    {
        String host;

        // Required
        try
        {
            host = getPropertyObject(MAILSERVER_HOST).getValue();
        }
        catch (ObjectNotFoundException e)
        {
            throw new PropertyManagerException(e);
        }

        // Optional Attributes
        String password = null;
        try
        {
            password = getPropertyObject(MAILSERVER_PASSWORD).getValue();
        }
        catch (ObjectNotFoundException ignored)
        {
            // Ignore since these are optional
        }

        String username = null;
        try
        {
            username = getPropertyObject(MAILSERVER_USERNAME).getValue();
        }
        catch (ObjectNotFoundException ignored)
        {
            // Ignore since these are optional
        }

        int port = SMTPServer.DEFAULT_MAIL_PORT;
        try
        {
            String value = getPropertyObject(MAILSERVER_PORT).getValue();
            if (StringUtils.isNotBlank(value))
            {
                port = Integer.parseInt(value);
            }
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("SMTP port number was not a valid number", e);
        }
        catch (ObjectNotFoundException ignored)
        {
            // Ignore since these are optional
        }

        boolean useSSL = false;
        try
        {
            useSSL = Boolean.valueOf(getPropertyObject(MAILSERVER_USE_SSL).getValue());
        }
        catch (ObjectNotFoundException ignored)
        {
            // Ignore since these are optional
        }

        return new SMTPServer(port, prefix, fromAddress, password, username, host, useSSL);
    }

    public void setSMTPServer(SMTPServer server)
    {
        setProperty(MAILSERVER_PREFIX, server.getPrefix());
        setProperty(MAILSERVER_SENDER, server.getFrom().toString());

        if (StringUtils.isNotBlank(server.getJndiLocation()))
        {
            setProperty(MAILSERVER_JNDI_LOCATION, server.getJndiLocation());

            // Blank out the SMTP properties
            setProperty(MAILSERVER_HOST, "");
            setProperty(MAILSERVER_PASSWORD, "");
            setProperty(MAILSERVER_USERNAME, "");
            setProperty(MAILSERVER_PORT, "");
            setProperty(MAILSERVER_USE_SSL, "");
        }
        else
        {
            setProperty(MAILSERVER_HOST, server.getHost());
            setProperty(MAILSERVER_PASSWORD, server.getPassword());
            setProperty(MAILSERVER_USERNAME, server.getUsername());
            setProperty(MAILSERVER_PORT, String.valueOf(server.getPort()));
            setProperty(MAILSERVER_USE_SSL, String.valueOf(server.getUseSSL()));

            // Blank out the JNDI server location
            setProperty(MAILSERVER_JNDI_LOCATION, "");
        }
    }

    public Key getDesEncryptionKey() throws PropertyManagerException
    {
        try
        {
            // get the key string
            String keyStr = getPropertyObject(DES_ENCRYPTION_KEY).getValue();

            // create a DES key spec
            DESKeySpec ks = new DESKeySpec(new sun.misc.BASE64Decoder().decodeBuffer(keyStr));

            // generate the key from the DES key spec
            return SecretKeyFactory.getInstance(DESPasswordEncoder.PASSWORD_ENCRYPTION_ALGORITHM).generateSecret(ks);

        }
        catch (NoSuchAlgorithmException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);

        }
        catch (IOException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);

        }
        catch (ObjectNotFoundException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);

        }
        catch (InvalidKeyException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);

        }
        catch (InvalidKeySpecException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    public void generateDesEncryptionKey() throws PropertyManagerException
    {
        try
        {
            try
            {

                getPropertyObject(DES_ENCRYPTION_KEY);

                return;

            }
            catch (Exception e)
            {
                // only generate the key if it does not already exist
            }

            // create a new key
            Key key = KeyGenerator.getInstance(DESPasswordEncoder.PASSWORD_ENCRYPTION_ALGORITHM).generateKey();

            // store this key
            setProperty(DES_ENCRYPTION_KEY, new sun.misc.BASE64Encoder().encode(key.getEncoded()));

        }
        catch (NoSuchAlgorithmException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    @Deprecated
    public void setSMTPTemplate(String template)
    {
        setProperty(FORGOTTEN_PASSWORD_EMAIL_TEMPLATE, template);
    }

    @Deprecated
    public String getSMTPTemplate() throws PropertyManagerException
    {
        try
        {
            Property property = getPropertyObject(FORGOTTEN_PASSWORD_EMAIL_TEMPLATE);

            return property.getValue();

        }
        catch (Exception e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    public void setCurrentLicenseResourceTotal(int total)
    {
        setProperty(CURRENT_LICENSE_RESOURCE_TOTAL, Integer.toString(total));
    }

    public int getCurrentLicenseResourceTotal()
    {
        int total = 0;
        try
        {
            total = Integer.parseInt(getPropertyObject(CURRENT_LICENSE_RESOURCE_TOTAL).getValue());
        }
        catch (Exception e)
        {
            logger.debug("Failed to find current resource total.", e);
        }
        return total;
    }

    public void setNotificationEmail(String notificationEmail)
    {
        setProperty(NOTIFICATION_EMAIL, notificationEmail);
    }

    public String getNotificationEmail() throws PropertyManagerException
    {
        try
        {
            return getPropertyObject(NOTIFICATION_EMAIL).getValue();
        }
        catch (ObjectNotFoundException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }

    }

    public boolean isGzipEnabled() throws PropertyManagerException
    {
        try
        {
            Property property = getPropertyObject(GZIP_ENABLED);

            return Boolean.parseBoolean(property.getValue());

        }
        catch (ObjectNotFoundException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    public void setGzipEnabled(boolean gzip)
    {
        setProperty(GZIP_ENABLED, Boolean.toString(gzip));
    }

    public Integer getBuildNumber() throws PropertyManagerException
    {
        try
        {
            return Integer.valueOf(getPropertyObject(BUILD_NUMBER).getValue());
        }
        catch (ObjectNotFoundException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    public void setBuildNumber(Integer buildNumber)
    {
        setProperty(BUILD_NUMBER, buildNumber.toString());
    }

    /**
     * Retrieves a String that contains a list of proxy servers we trust to correctly set the X-Forwarded-For flag.
     * Internal format of this string is the responsibility of {@link com.atlassian.crowd.manager.proxy.TrustedProxyManagerImpl}.
     *
     * @return list of proxy servers we trust
     * @throws PropertyManagerException If the list of proxy servers could not be found.
     */
    public String getTrustedProxyServers() throws PropertyManagerException
    {
        try
        {
            return getPropertyObject(TRUSTED_PROXY_SERVERS).getValue();
        }
        catch (ObjectNotFoundException e)
        {
            throw new PropertyManagerException(e.getMessage(), e);
        }
    }

    /**
     * Persists a String containing a list of proxy servers we trust to correctly set the X-Forwarded-For flag.
     * Internal format of this string is the responsibility of {@link com.atlassian.crowd.manager.proxy.TrustedProxyManagerImpl}.
     *
     * @throws org.springframework.dao.DataAccessException
     *          If the list of proxy servers could not be saved.
     */
    public void setTrustedProxyServers(String proxyServers)
    {
        setProperty(TRUSTED_PROXY_SERVERS, proxyServers);
    }

    public boolean isUsingDatabaseTokenStorage() throws PropertyManagerException
    {
        boolean usingDatabaseStorage;
        try
        {
            usingDatabaseStorage = Boolean.parseBoolean(getPropertyObject(DATABASE_TOKEN_STORAGE_ENABLED).getValue());
        }
        catch (ObjectNotFoundException e)
        {
            // By default return true
            usingDatabaseStorage = true;
        }

        return usingDatabaseStorage;
    }

    public void setUsingDatabaseTokenStorage(boolean usingMemoryTokenStorage)
    {
        setProperty(DATABASE_TOKEN_STORAGE_ENABLED, Boolean.toString(usingMemoryTokenStorage));
    }

    public void removeProperty(String name)
    {
        propertyDAO.remove(CROWD_PROPERTY_KEY, name);
    }

    protected Property getPropertyObject(String name) throws ObjectNotFoundException
    {
        return propertyDAO.find(CROWD_PROPERTY_KEY, name);
    }

    public String getProperty(String name) throws ObjectNotFoundException
    {
        Property property = getPropertyObject(name);
        return property.getValue();
    }

    public void setProperty(String name, String value)
    {
        Property property = null;

        try
        {
            property = getPropertyObject(name);
        }
        catch (ObjectNotFoundException e)
        {
            // ignore, we just want to update if the property already exist
        }

        if (property == null)
        {
            property = new Property(CROWD_PROPERTY_KEY, name, value);
        }
        else
        {
            property.setValue(value);
        }

        // add the property to the database
        propertyDAO.update(property);
    }
}
