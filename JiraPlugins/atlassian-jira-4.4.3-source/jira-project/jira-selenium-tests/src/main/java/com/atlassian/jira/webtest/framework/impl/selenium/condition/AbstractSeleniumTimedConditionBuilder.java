package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.util.AbstractSeleniumTimedObjectBuilder;

/**
 * abstract builder for {@link AbstractSeleniumTimedCondition}.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumTimedConditionBuilder<B extends AbstractSeleniumTimedConditionBuilder<B,T>, T extends AbstractSeleniumTimedCondition>
        extends AbstractSeleniumTimedObjectBuilder<B,T>
{


    protected AbstractSeleniumTimedConditionBuilder(SeleniumContext context, Class<B> target)
    {
        super(context, target);
    }
}
