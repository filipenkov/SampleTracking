package com.atlassian.jira.webtest.framework.core.query;

import com.atlassian.jira.webtest.framework.util.AbstractTimedObjectBuilder;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An abstract builder for {@link com.atlassian.jira.webtest.framework.core.query.AbstractTimedQuery} implementations.
 *
 * @since v4.3
 */
public abstract class AbstractTimedQueryBuilder<B extends AbstractTimedQueryBuilder<B,Q,V>, Q extends AbstractTimedQuery<V>,V>
        extends AbstractTimedObjectBuilder<B,Q>
{
    public static ExpirationHandler DEFAULT_HANDLER = ExpirationHandler.RETURN_NULL;

    private ExpirationHandler expirationHandler = DEFAULT_HANDLER;

    protected AbstractTimedQueryBuilder(Class<B> target)
    {
        super(target);
    }

    public final B expirationHandler(ExpirationHandler handler)
    {
        this.expirationHandler = notNull("expirationHandler", handler);
        return asTargetType();
    }

    public final ExpirationHandler expirationHandler()
    {
        return expirationHandler;
    }


}
