/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtest.selenium.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;

import java.util.Arrays;
import java.util.List;

/**
 * Tests the javascript filter on the issue navigator where the list of issue types
 * shown is dependent on what projects are selected.
 */
@WebTest({Category.SELENIUM_TEST })
@Quarantine()
public class TestIssueNavigatorIssueTypeFilter extends JiraSeleniumTest
{
    private static final String BUG = "Bug";
    private static final String DOG = "Dog Specific";
    private static final String MAN = "Homosapien Specific";
    private static final String IMP = "Improvement";
    private static final String MONKEY = "Monkey Specific";
    private static final String FEET = "New Feature";
    private static final String TASK = "Task";

    private static final String PROJECT = "pid";

    private static final String ISSUE_TYPE = "id=searcher-type";
    private static final String PROJECT_REFRESH_PANEL = "id=projectRefreshPanel";

    private static final String LABEL_ALL = "label=All projects";
    private static final String LABEL_MAN = "label=homosapien";
    private static final String LABEL_MONKEY = "label=monkey";
    private static final String LABEL_DOG = "label=canine";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestIssueNavigatorIssueTypeFilter.xml");
    }

    public void test() throws Exception
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        getNavigator().gotoFindIssues();

        assertThat.elementNotVisible(PROJECT_REFRESH_PANEL);
        assertThat.textPresent(BUG);
        assertThat.textPresent(DOG);
        assertThat.textPresent(MAN);
        assertThat.textPresent(IMP);
        assertThat.textPresent(MONKEY);
        assertThat.textPresent(FEET);
        assertThat.textPresent(TASK);

        client.removeSelection(PROJECT, LABEL_ALL);
        client.addSelection(PROJECT, LABEL_MAN);
        contains(client.getSelectOptions(ISSUE_TYPE), new String[] {MAN});
        doesNotContain(client.getSelectOptions(ISSUE_TYPE), new String[] {MONKEY, DOG});
        assertThat.elementVisible(PROJECT_REFRESH_PANEL);

        client.addSelection(PROJECT, LABEL_MONKEY);
        contains(client.getSelectOptions(ISSUE_TYPE), new String[] {MONKEY, MAN});
        doesNotContain(client.getSelectOptions(ISSUE_TYPE), new String[] {DOG});
        assertThat.elementVisible(PROJECT_REFRESH_PANEL);

        client.removeSelection(PROJECT, LABEL_MAN);
        contains(client.getSelectOptions(ISSUE_TYPE), new String[] {MONKEY});
        doesNotContain(client.getSelectOptions(ISSUE_TYPE), new String[] {MAN, DOG});
        assertTrue(client.isVisible(PROJECT_REFRESH_PANEL));

        client.addSelection(PROJECT, LABEL_DOG);
        contains(client.getSelectOptions(ISSUE_TYPE), new String[] {MONKEY, DOG});
        doesNotContain(client.getSelectOptions(ISSUE_TYPE), new String[] {MAN});
        assertThat.elementVisible(PROJECT_REFRESH_PANEL);

        client.removeSelection(PROJECT, LABEL_MONKEY);
        contains(client.getSelectOptions(ISSUE_TYPE), new String[] {DOG});
        doesNotContain(client.getSelectOptions(ISSUE_TYPE), new String[] {MAN, MONKEY});
        assertThat.elementVisible(PROJECT_REFRESH_PANEL);

        // when none selected all should be present again
        client.removeSelection(PROJECT, LABEL_DOG);
        contains(client.getSelectOptions(ISSUE_TYPE), new String[] {MAN, MONKEY, DOG});
        assertThat.elementNotVisible(PROJECT_REFRESH_PANEL);
    }

    private void contains(String[] source, String[] requires)
    {
        assertTrue(Arrays.asList(source).containsAll(Arrays.asList(requires)));
    }

    private void doesNotContain(String[] source, String[] requires)
    {
        List sourceList = Arrays.asList(source);
        for (int i = 0; i < requires.length; i++)
        {
            assertFalse(requires[i], sourceList.contains(requires[i]));
        }
    }
}