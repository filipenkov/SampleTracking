package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Query that returns an evaluation of a script in the current test context. 
 *
 * @since v4.3
 */
public class ScriptExecutionQuery extends AbstractSeleniumTimedQuery<String>
{
    public static class Builder extends AbstractSeleniumTimedQueryBuilder<Builder,ScriptExecutionQuery,String>
    {
        private String script;

        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
            defaultTimeout(Timeouts.UI_ACTION);
            expirationHandler(ExpirationHandler.RETURN_NULL);
        }

        public Builder script(String script)
        {
            this.script = notNull("script", script);
            return this;
        }

        @Override
        public ScriptExecutionQuery build()
        {
            return new ScriptExecutionQuery(this);
        }
    }

    private final String script;

    public ScriptExecutionQuery(Builder builder)
    {
        super(builder.context(), builder.expirationHandler(), builder.defaultTimeout());
        this.script = notNull("script", builder.script);
    }

    @Override
    protected boolean shouldReturn(String currentEval)
    {
        return true;
    }

    @Override
    protected String currentValue()
    {
        return client.getEval(script);
    }
}
