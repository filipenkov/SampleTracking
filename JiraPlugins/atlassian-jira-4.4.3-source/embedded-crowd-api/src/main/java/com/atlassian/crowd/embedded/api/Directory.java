package com.atlassian.crowd.embedded.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Directory configuration in Crowd. It is used to create a <tt>RemoteDirectory</tt> for want of a better
 * name, you could really call this a Directory Configuration class.
 */
public interface Directory extends Serializable, Attributes
{
    /**
     * Returns the directory ID.
     *
     * @return directory ID
     */
    Long getId();

    /**
     * Returns the name of the directory.
     *
     * @return name of the directory
     */
    String getName();

    /**
     * Returns <tt>true</tt> if the directory is currently active, otherwise <tt>false</tt>.
     *
     * @return <tt>true</tt> if the directory is currently active, otherwise <tt>false</tt>.
     */
    boolean isActive();

    /**
     * Returns the encryption algorithm used by the directory.
     *
     * @return encryption algorithm used by the directory
     */
    String getEncryptionType();

    /**
     * Returns the attributes of the directory.
     *
     * @return attributes of the directory
     */
    Map<String, String> getAttributes();

    /**
     * Returns the operations allowed to be performed on this directory.
     *
     * @return the operations allowed to be performed on this directory
     */
    Set<OperationType> getAllowedOperations();

    /**
     * Returns a description of the directory.
     *
     * @return description of the directory
     */
    String getDescription();

    /**
     * Returns the type of the directory.
     *
     * @return type of the directory
     * @see DirectoryType
     */
    DirectoryType getType();

    /**
     * Returns the fully qualified name of the class that implements the directory.
     *
     * @return the fully qualified name of the class that implements the directory
     */
    String getImplementationClass();

    /**
     * Returns the date the directory was created.
     *
     * @return date the directory was created
     */
    Date getCreatedDate();

    /**
     * Returns the date the directory was last modified.
     * 
     * @return date the directory was last modified
     */
    Date getUpdatedDate();
}
