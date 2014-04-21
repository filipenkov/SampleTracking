package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.google.inject.Inject;

/**
 * Enables/disables web sudo in JIRA.
 *
 * @since v4.4
 */
public class WebSudoControl
{
    @Inject
    private Backdoor backdoor;

    public void toogle(boolean targetWebSudoState)
    {
        if (targetWebSudoState)
        {
            backdoor.websudo().enable();
        }
        else
        {
            backdoor.websudo().disable();
        }
    }
}
