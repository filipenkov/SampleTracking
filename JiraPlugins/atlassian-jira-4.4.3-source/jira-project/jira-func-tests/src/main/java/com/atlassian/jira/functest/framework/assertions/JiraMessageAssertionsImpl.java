package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class JiraMessageAssertionsImpl extends AbstractFuncTestUtil implements JiraMessageAssertions
{


    private final LocatorFactory locator;
    private final TextAssertions textAssertions;

    public JiraMessageAssertionsImpl(WebTester tester, JIRAEnvironmentData environmentData,
            LocatorFactory locator, TextAssertions textAssertions)
    {
        super(tester, environmentData, 2);
        this.locator = locator;
        this.textAssertions = textAssertions;
    }

    @Override
    public void assertHasTitle(String expectedTitle)
    {
        textAssertions.assertTextPresent(locator.css("#jira-message-container h1"), expectedTitle);
    }

    @Override
    public void assertHasMessage(String expectedMsg)
    {
        textAssertions.assertTextPresent(locator.css("#jira-message-container .aui-message"), expectedMsg);
    }
}
