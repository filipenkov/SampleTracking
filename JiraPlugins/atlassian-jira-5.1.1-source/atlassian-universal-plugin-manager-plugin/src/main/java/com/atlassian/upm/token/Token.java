package com.atlassian.upm.token;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

final class Token
{
    private final String token;
    private final Date creationDate;

    Token(String token, Date creationDate)
    {
        this.token = checkNotNull(token, "token");
        this.creationDate = checkNotNull(creationDate, "creationDate");
    }

    public String getValue()
    {
        return token;
    }

    public boolean isExpired()
    {
        // check if token is more than 5 min old
        Date tokenExpiryDate = new Date(creationDate.getTime() + 5 * 60 * 1000);
        Date currentDate = new Date();
        return currentDate.after(tokenExpiryDate);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Token otherToken = (Token) o;

        if (!token.equals(otherToken.token) || !creationDate.equals(otherToken.creationDate))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return token.hashCode();
    }

    public String toString()
    {
        return token;
    }
}
