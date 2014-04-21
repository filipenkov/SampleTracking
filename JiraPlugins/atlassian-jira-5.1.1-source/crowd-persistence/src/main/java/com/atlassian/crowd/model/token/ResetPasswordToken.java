package com.atlassian.crowd.model.token;

import com.atlassian.util.concurrent.*;
import org.apache.commons.lang.*;

import java.io.*;

/**
 * Represents a reset password token.
 *
 * @since v2.1.0
 */
public class ResetPasswordToken implements Serializable
{
    private static final long serialVersionUID = -1897661365186057736L;
    
    private final long expiryDate;
    private final String token;
    private final String username;
    private final long directoryId;

    /**
     * Constructs a reset password token.
     *
     * @param expiryDate expiry date in milliseconds from epoch
     * @param token reset token
     * @param username username
     * @param directoryId directory ID
     */
    public ResetPasswordToken(final long expiryDate, final String token, final String username, final long directoryId)
    {
        Validate.notNull("token cannot be null", token);
        Validate.notNull("username cannot be null", username);
        this.expiryDate = expiryDate;
        this.token = token;
        this.username = username;
        this.directoryId = directoryId;
    }

    /**
     * Returns the expiry date of the reset token in milliseconds from epoch.
     *
     * @return expiry date in milliseconds from epoch
     */
    public long getExpiryDate()
    {
        return expiryDate;
    }

    /**
     * Returns the token to verify that the user wants to reset their password.
     *
     * @return cryptographically secure token
     */
    public String getToken()
    {
        return token;
    }

    public String getUsername()
    {
        return username;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof ResetPasswordToken))
            return false;

        ResetPasswordToken token = (ResetPasswordToken) o;

        if (getExpiryDate() != token.getExpiryDate()) return false;
        if (getToken() != null ? !getToken().equals(token.getToken()) : token.getToken() != null) return false;
        if (getUsername() != null ? !getUsername().equals(token.getUsername()) : token.getUsername() != null) return false;
        if (getDirectoryId() != token.getDirectoryId()) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31 * result + (int) (expiryDate ^ (expiryDate >>> 32));
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (int) (directoryId ^ (directoryId >>> 32));
        return result;
    }

    /**
     * Serialize this {@code ResetPasswordToken} instance.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    /**
     * De-serialize the {@code ResetPasswordToken} instance.
     */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
    }
}
