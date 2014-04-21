package com.atlassian.crowd.model.application;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.PasswordCredential;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An Application in Crowd. This is the top level citizen in Crowd, where an application will have an ordered set
 * of associated {@link com.atlassian.crowd.embedded.api.Directory}'s which it can access.
 */
public interface Application extends Serializable, Attributes
{
    /**
     * Returns the application ID.
     *
     * @return application ID
     */
    Long getId();

    /**
     * Returns the name of the application.
     *
     * @return name of the application
     */
    String getName();

    /**
     * Returns the type of the application.
     *
     * @return application type
     */
    ApplicationType getType();

    /**
     * Returns the description of the application.
     *
     * @return description of the application
     */
    String getDescription();

    /**
     * Returns the application password.
     *
     * @return application password
     */
    PasswordCredential getCredential();

    /**
     * Returns whether the application is a permanent application and thus cannot be removed. For instance, the Crowd
     * application is a permanent application.
     *
     * @return <tt>true</tt> if the application is permanent.
     */
    boolean isPermanent();

    /**
     * Returns whether the application is active.
     *
     * @return <tt>true</tt> if the application is active.
     */
    boolean isActive();

    /**
     * Returns the attributes of the application.
     *
     * @return attributes of the application
     */
    Map<String, String> getAttributes();

    // TODO make this a sorted set
    /**
     * Returns the list of directory mappings ranked by directory priority as in perspective of the application.
     *
     * @return List of directory mappings (never null).
     */
    List<DirectoryMapping> getDirectoryMappings();

    /**
     * Returns a directory mapping of the directory specified by directory id.
     *
     * @param directoryId ID of the directory
     * @return directory mapping if found, null if the directory mapping could not be found
     */
    DirectoryMapping getDirectoryMapping(long directoryId);

    /**
     * Returns the whitelist of addresses allowed to connect to Crowd as the application. The remote addresses may
     * contain subnet masking information in CIDR format.
     *
     * @return whitelist of addresses allowed to connect to Crowd as the application.
     */
    Set<RemoteAddress> getRemoteAddresses();

    /**
     * Returns <tt>true</tt> if the remote address is already in the list of allowable remote addresses for the
     * application.
     *
     * @param remoteAddress RemoteAddress whose presence is to be tested
     * @return <tt>true</tt> if the remote address is already in the list of allowable remote addresses for the
     *          application
     */
    boolean hasRemoteAddress(String remoteAddress);

    /**
     * Returns <tt>true</tt> if the usernames and group names returned should be in lowercase.
     *
     * @return <tt>true</tt> if the usernames and group names returned 
     */
    boolean isLowerCaseOutput();

    /**
     * Returns <tt>true</tt> if aliasing is enabled for the application.
     *
     * @return <tt>true</tt> if aliasing is enabled for the application
     */
    boolean isAliasingEnabled();

    /**
     * Returns the date the application was created.
     *
     * @return date the application was created
     */
    Date getCreatedDate();

    /**
     * Returns the date the application was last updated. If the application has just been created, the updated date
     * will be the same as the creation date.
     *
     * @return date the application was last updated
     */
    Date getUpdatedDate();
}
