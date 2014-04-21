package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Condition that a script result must evaluate to a predefined expected value.
 *
 * @since v4.3
 *
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 */
public class ExecuteScriptCondition extends AbstractSeleniumTimedCondition implements TimedCondition
{
    public static final class Builder extends AbstractSeleniumTimedConditionBuilder<Builder, ExecuteScriptCondition>
    {
        private String script;
        private String expectedResult;

        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        public Builder script(String script)
        {
            this.script = script;
            return this;
        }

        public Builder expectedResult(String expResult)
        {
            this.expectedResult = expResult;
            return this;
        }

        public Builder expectedResult(boolean expResult)
        {
            this.expectedResult = Boolean.toString(expResult);
            return this;
        }

        @Override
        public ExecuteScriptCondition build()
        {
            return new ExecuteScriptCondition(this);
        }
    }

    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    private final String script;
    private final String expectedResult;

    private ExecuteScriptCondition(Builder builder)
    {
        super(builder);
        this.script = notNull("script", builder.script);
        this.expectedResult = notNull("expectedResult", builder.expectedResult);
    }


    public boolean now()
    {
        return client.getEval(script).equals(expectedResult);
    }
}
