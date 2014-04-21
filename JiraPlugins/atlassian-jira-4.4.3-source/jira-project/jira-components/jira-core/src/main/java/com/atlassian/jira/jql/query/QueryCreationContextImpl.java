package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.util.concurrent.LazyReference;
import electric.xml.Comment;

/**
 * Default implementation of {@link QueryCreationContext}.
 *
 * @since v4.0
 */
public class QueryCreationContextImpl implements QueryCreationContext
{
    private final User user;
    private final boolean securityOverriden;

    private final LazyReference<com.opensymphony.user.User> ref = new LazyReference<com.opensymphony.user.User>()
    {

        @Override
        protected com.opensymphony.user.User create() throws Exception
        {
            return OSUserConverter.convertToOSUser(user);
        }
    };

    /**
     * Use this constructor unless you know you need to override security.
     *
     * @param user the user performing the search
     */
    public QueryCreationContextImpl(final User user)
    {
        this(user, false);
    }

    /**
     * Use this constructor if you need to override security.
     *
     * @param user the user performing the search
     * @param securityOverriden true if you want to override security; false otherwise
     */
    public QueryCreationContextImpl(final User user, final boolean securityOverriden)
    {
        this.user = user;
        this.securityOverriden = securityOverriden;
    }

    @Override
    public com.opensymphony.user.User getUser()
    {
        return ref.get();
    }

    public User getQueryUser()
    {
        return user;
    }

    public boolean isSecurityOverriden()
    {
        return securityOverriden;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final QueryCreationContextImpl that = (QueryCreationContextImpl) o;

        if (securityOverriden != that.securityOverriden)
        {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (securityOverriden ? 1 : 0);
        return result;
    }
}
