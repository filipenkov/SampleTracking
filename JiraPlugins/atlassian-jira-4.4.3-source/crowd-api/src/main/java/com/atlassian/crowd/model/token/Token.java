package com.atlassian.crowd.model.token;

import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.Date;

/**
 * Holds the token information for an authenticated entity.
 */
public class Token implements Serializable
{
    /**
     * Tokens are used for application clients and principals. If the stored token does not have a valid directory ID,
     * the token is then for an application. The value for an invalid directory ID is the <code>APPLICATION_TOKEN_DIRECTORY_ID</code>
     * value.
     */
    public static final long APPLICATION_TOKEN_DIRECTORY_ID = -1;

    private Long id;
    private String identifierHash;
    private String randomHash;
    private long randomNumber;
    private Date createdDate;
    private Date lastAccessedDate;
    private String name;     // Username or Application name the token is for.
    private long directoryId = APPLICATION_TOKEN_DIRECTORY_ID;

    public Token(long directoryId, String name, String identifierHash, long randomNumber, String randomHash)
    {
        this(directoryId, name, identifierHash, randomNumber, randomHash, new Date(), new Date());
    }

    public Token(long directoryId, String name, String identifierHash, long randomNumber, String randomHash, Date createdDate, Date lastAccessedDate)
    {
        Validate.notNull(directoryId, "directoryId argument cannot be null");
        this.directoryId = directoryId;

        Validate.notNull(name, "name argument cannot be null");
        this.name = name;

        Validate.notNull(identifierHash, "identifierHash argument cannot be null");
        this.identifierHash = identifierHash;

        Validate.notNull(randomNumber, "randomNumber argument cannot be null");
        this.randomNumber = randomNumber;

        Validate.notNull(randomHash, "randomHash argument cannot be null");
        this.randomHash = randomHash;

        Validate.notNull(createdDate, "createdDate argument cannot be null");
        this.createdDate = createdDate;

        Validate.notNull(lastAccessedDate, "lastAccessedDate argument cannot be null");
        this.lastAccessedDate = lastAccessedDate;
    }

    private Token()
    {
        // Used for Hibernate only
    }

    public Long getId()
    {
        return id;
    }

    private void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Gets the token key.
     *
     * @return The key.
     */
    public String getRandomHash()
    {
        return randomHash;
    }

    /**
     * Sets the token key.
     *
     * @param randomHash The key.
     */
    private void setRandomHash(String randomHash)
    {
        this.randomHash = randomHash;
    }

    /**
     * Gets the name of the entity.
     *
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the entity.
     *
     * @param name The name.
     */
    private void setName(String name)
    {
        this.name = name;
    }

    /**
     * Directory the {@link com.atlassian.crowd.model.user.User user} originated, -1 if the token is for an
     * {@link com.atlassian.crowd.model.application.ApplicationImpl application}.
     *
     * @return The {@link com.atlassian.crowd.model.directory.DirectoryImpl directory} ID.
     */
    public long getDirectoryId()
    {
        return directoryId;
    }

    /**
     * Diretory the {@link com.atlassian.crowd.model.user.User user} originated, -1 if the token is for an
     * {@link com.atlassian.crowd.model.application.ApplicationImpl application}.
     *
     * @param directoryId The {@link com.atlassian.crowd.model.directory.DirectoryImpl directory} ID.
     */
    private void setDirectoryId(long directoryId)
    {
        this.directoryId = directoryId;
    }

    public long getRandomNumber()
    {
        return randomNumber;
    }

    private void setRandomNumber(final long randomNumber)
    {
        this.randomNumber = randomNumber;
    }

    public boolean isUserToken()
    {
        return !isApplicationToken();
    }

    public boolean isApplicationToken()
    {
        return getDirectoryId() == APPLICATION_TOKEN_DIRECTORY_ID;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    private void setCreatedDate(final Date createdDate)
    {
        this.createdDate = createdDate;
    }

    public Date getLastAccessedDate()
    {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(final Date lastAccessedDate)
    {
        this.lastAccessedDate = lastAccessedDate;
    }

    public String getIdentifierHash()
    {
        return identifierHash;
    }

    private void setIdentifierHash(final String identifierHash)
    {
        this.identifierHash = identifierHash;
    }        

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (getIdentifierHash() != null ? !getIdentifierHash().equals(token.getIdentifierHash()) : token.getIdentifierHash() != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return getIdentifierHash() != null ? getIdentifierHash().hashCode() : 0;
    }
}