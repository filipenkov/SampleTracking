package com.atlassian.jira.webtest.selenium.gadgets;

/**
 * Base class for constants used when testing the standard statistics type selector
 */
public class StatsTestBase extends BaseChartGadgetTest
{
    protected static final String RESOLUTION_CANNOT_REPRODUCE = "Cannot Reproduce";
    protected static final String RESOLUTION_INCOMPLETE = "Incomplete";
    protected static final String RESOLUTION_DUPLICATE = "Duplicate";
    protected static final String RESOLUTION_WON_T_FIX = "Won't Fix";
    protected static final String RESOLUTION_FIXED = "Fixed";
    protected static final String STATUS_COLUMN = "status";
    protected static final String RESOLUTION_COLUMN = "resolution";
    protected static final String REPORTER_COLUMN = "reporter";
    protected static final String VERSIONS_COLUMN = "versions";
    protected static final String MONKEY = "monkey";
    protected static final String PRIORITY_COLUMN = "priority";
    protected static final String PRIORITY_TRIVIAL = "Trivial";
    protected static final String PRIORITY_MINOR = "Minor";
    protected static final String PRIORITY_MAJOR = "Major";
    protected static final String PRIORITY_CRITICAL = "Critical";
    protected static final String PRIORITY_BLOCKER = "Blocker";
    protected static final String NEW_VERSION_1 = "New Version 1";
    protected static final String NEW_VERSION_5 = "New Version 5";
    protected static final String NEW_VERSION_4 = "New Version 4";
    protected static final String FIX_VERSIONS_COLUMN = "fixVersions";
    protected static final String TYPE_IMPROVEMENT = "Improvement";
    protected static final String TYPE_TASK = "Task";
    protected static final String TYPE_NEW_FEATURE = "New Feature";
    protected static final String TYPE_BUG = "Bug";
    protected static final String ISSUETYPE_COLUMN = "issuetype";
    protected static final String COMPONENTS_COLUMN = "components";
    protected static final String NEW_COMPONENT_3 = "New Component 3";
    protected static final String NEW_COMPONENT_2 = "New Component 2";
    protected static final String NEW_COMPONENT_1 = "New Component 1";
    protected static final String BLARGH = "blargh";
    protected static final String HOMOSAPIEN = "homosapien";
    protected static final String MKY = "MKY";
    protected static final String HSP = "HSP";
    protected static final String ISSUE_TYPE_BUG = TYPE_BUG;
    protected static final String ISSUE_TYPE_FEATURE = TYPE_NEW_FEATURE;
    protected static final String ISSUE_TYPE_TASK = TYPE_TASK;
    protected static final String ISSUE_TYPE_IMPROVEMENT = TYPE_IMPROVEMENT;
    protected static final String FRED_USERNAME = "fred";
    protected static final String FRED_NAME = "Fred Normal";
    protected static final String ADMIN_NAME = "Administrator";
    protected static final String JIRA_DEVELOPERS = "jira-developers";

    protected void configGadget(String hint, String id, String statType, int refresh)
    {
        if (hint != null)
        {
            //selectProjectOrFilterFromAutoComplete("quickfind", hint, id);
            final String clickTarget = id + "_" + "quickfind" + "_listitem";
            // NOTE: must use type because selenium will not send !
            client.type("quickfind", hint);
            // Send a right arrow with an event to kick the js in
            client.keyPress("quickfind", "\\16");
            if (clickTarget != null)
            {
                assertThat.visibleByTimeout(clickTarget, TIMEOUT);
            }
            client.click(clickTarget);
        }
        if (statType != null)
        {
            client.select("statType", statType);
        }
        if (refresh > 0)
        {
            client.select("refresh", "value=" + refresh);
        }
        client.click("css=input.button.save");
    }
}
