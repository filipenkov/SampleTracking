package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

/*
* @since 4.3
*/
public interface WebSudoBanner extends Localizable
{
    WebSudoBanner dropWebSudo();
    TimedCondition protectedLinkIsPresent();
    TimedCondition normalLinkIsPresent();
    TimedCondition isPresent();
    TimedCondition isVisible();
}
