package com.atlassian.crowd.manager.property;

import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.util.mail.SMTPServer;

import java.security.Key;

/**
 * API for storing and retrieving Crowd server properties.
 */
public interface PropertyManager
{
    /**
     * Returns the cache time in minutes
     * @return caching time in minutes
     * @throws PropertyManagerException if the property cannot be found
     * @deprecated since 1.0.2 All caching configuration has moved to the crowd-ehcache.xml
     */
    long getCacheTime() throws PropertyManagerException;

    /**
     * Sets the cache time in minutes
     * @param cacheTime the caching time in minutes
     * @deprecated since 1.0.2 All caching configuration has moved to the crowd-ehcache.xml
     */
    void setCacheTime(long cacheTime);

    /**
     * @return token seed.
     * @throws PropertyManagerException property does not exist.
     */
    String getTokenSeed() throws PropertyManagerException;

    /**
     * @param seed token seed.
     */
    void setTokenSeed(String seed);

    /**
     * @return deployment title.
     * @throws PropertyManagerException property does not exist.
     */
    String getDeploymentTitle() throws PropertyManagerException;

    /**
     * @param title deployment title.
     */
    void setDeploymentTitle(String title);

    /**
     * Will return the Domain property from the datastore or null if the domain has
     * not been set.
     * @return domain or null
     * @throws PropertyManagerException property does not exist.
     */
    String getDomain() throws PropertyManagerException;

    /**
     * @param domain SSO cookie domain.
     */
    void setDomain(String domain);

    /**
     * @return {@code true} if the "secure" flag should be set on the SSO cookie.
     * @throws PropertyManagerException property does not exist.
     */
    boolean isSecureCookie() throws PropertyManagerException;

    /**
     * @param secure {@code true} if the "secure" flag should be set on the SSO cookie.
     */
    void setSecureCookie(boolean secure);

    /**
     * @param enabled {@code true} if application authorisation caching should be used on the server-side.
     */
    void setCacheEnabled(boolean enabled);

    /**
     * @return {@code true} if application authorisation caching is used on the server-side.
     */
    boolean isCacheEnabled();

    /**
     * @return number of minutes the session is valid.
     */
    long getSessionTime();

    /**
     * @param time number of minutes the session is valid.
     */
    void setSessionTime(long time);

    /**
     * @return SMTP server config.
     * @throws PropertyManagerException property does not exist.
     */
    SMTPServer getSMTPServer() throws PropertyManagerException;

    /**
     * @param server SMTP server config.
     */
    void setSMTPServer(SMTPServer server);

    /**
     * @return DES key for DES encoded passwords.
     * @throws PropertyManagerException property does not exist.
     */
    Key getDesEncryptionKey() throws PropertyManagerException;

    /**
     * Generates and stores a DES key for DES encoded passwords.
     *
     * @throws PropertyManagerException DES algorithm does not exist.
     */
    void generateDesEncryptionKey() throws PropertyManagerException;

    /**
     * @param template mail template.
     * @deprecated As of release 2.1, use {@link #setProperty(String, String)}
     */
    @Deprecated
    void setSMTPTemplate(String template);

    /**
     * @deprecated As of release 2.1, use {@link #getProperty(String)}
     * @return mail template.
     * @throws PropertyManagerException property does not exist.
     */
    @Deprecated
    String getSMTPTemplate() throws PropertyManagerException;

    /**
     * @param total license resource total.
     */
    void setCurrentLicenseResourceTotal(int total);

    /**
     * @return license resource total.
     */
    int getCurrentLicenseResourceTotal();

    /**
     * @param notificationEmail notification email.
     */
    void setNotificationEmail(String notificationEmail);

    /**
     * @return notification email.
     * @throws PropertyManagerException property does not exist.
     */
    String getNotificationEmail() throws PropertyManagerException;

    /**
     * @return {@code true} if GZip compression should be used.
     * @throws PropertyManagerException property does not exist.
     */
    boolean isGzipEnabled() throws PropertyManagerException;

    /**
     * @param gzip {@code true} if GZip compression should be used.
     */
    void setGzipEnabled(boolean gzip);

    /**
     * This method returns the current build number for Crowd from the datastore. This BuildNumber may not be the same
     * as the build number in {@link com.atlassian.crowd.util.build.BuildUtils#BUILD_NUMBER} since this number is for the
     * current release of Crowd, while the number in the database may still be set to a previous version if the UpgradeManager
     * has not been run.
     * @return an Integer representing the current build number in the database.
     * @throws PropertyManagerException if we fail to find the buildNumber
     */
    Integer getBuildNumber() throws PropertyManagerException;

    /**
     * Will set the buildNumber for the current release of Crowd.
     * @param buildNumber the buildNumber to set in the database
     */
    void setBuildNumber(Integer buildNumber);

    /**
    * Retrieves a String that contains a list of proxy servers we trust to correctly set the X-Forwarded-For flag.
    * Internal format of this string is the responsibility of TrustedProxyManagerImpl.
    * @return list of proxy servers as a string.
    * @throws PropertyManagerException If the list of proxy servers could not be found.
 	*/
 	String getTrustedProxyServers() throws PropertyManagerException;

 	/**
 	* Persists a String containing a list of proxy servers we trust to correctly set the X-Forwarded-For flag.
 	* Internal format of this string is the responsibility of TrustedProxyManagerImpl.
    * @param proxyServers proxy servers.
    */
    void setTrustedProxyServers(String proxyServers);

    /**
     * Will return true if the Crowd instance is using database token storage for authentication {@link com.atlassian.crowd.model.token.Token}'s
     * otherwise assume we are using in-memory
     * @return true if database token storage is being used.
     * @throws PropertyManagerException property does not exist.
     */
    boolean isUsingDatabaseTokenStorage() throws PropertyManagerException;

    /**
     * Will set a property to state that this crowd instance is using database token storage, otherwise assume we are using in-memory
     * @param usingDatabaseTokenStorage true if you are switching to in-memory token storage
     */
    void setUsingDatabaseTokenStorage(boolean usingDatabaseTokenStorage);

    /**
     * Will attempt to remove a property from the datastore
     * @param name the name of the property.
     */
    void removeProperty(String name);

    /**
     * Retrieves an arbitrary property by name.
     * @param name name of property.
     * @return value.
     * @throws ObjectNotFoundException property does not exist.
     */
    String getProperty(String name) throws ObjectNotFoundException;

    /**
     * Sets an arbitrary property.
     * @param name name of property.
     * @param value value.
     */
    void setProperty(String name, String value);
}
