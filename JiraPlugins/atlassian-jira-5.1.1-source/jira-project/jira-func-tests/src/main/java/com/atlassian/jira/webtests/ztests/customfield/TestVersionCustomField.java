package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the version custom field
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS, Category.CUSTOM_FIELDS, Category.FIELDS })
public class TestVersionCustomField extends FuncTestCase
{
    // JRA-15007 - ordering of version CF should be same as system fields
    public void testOrderingOfOptions()
    {
        // Data contains:
        // * Only one project (homosapien) with 4 versions - 2 released, 2 unreleased
        // * Single Version Picker CF
        // * Version Picker CF
        administration.restoreData("TestVersionCustomField.xml");

        // check ordering in Issue Navigator (searchers)
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        String[] fixForVersions = tester.getDialog().getOptionsFor("fixfor");
        String[] affectsVersions = tester.getDialog().getOptionsFor("version");
        String[] cfSingleVersions = tester.getDialog().getOptionsFor("customfield_10000");
        String[] cfVersions = tester.getDialog().getOptionsFor("customfield_10001");

        // check all options are equal to the expected: Released DESC, Unreleased ASC
        assertEquals(getUnifiedVersions(affectsVersions), EasyList.build("New Version 4", "New Version 1", "New Version 5", "New Version 6"));
        assertEquals(getUnifiedVersions(affectsVersions), getUnifiedVersions(fixForVersions));
        assertEquals(getUnifiedVersions(affectsVersions), getUnifiedVersions(cfSingleVersions));
        assertEquals(getUnifiedVersions(affectsVersions), getUnifiedVersions(cfVersions));

        // check ordering in Create New Issue (edit)
        String key = navigation.issue().createIssue("homosapien", "Bug", "Test issue 1");
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        fixForVersions = tester.getDialog().getOptionsFor("fixVersions");
        affectsVersions = tester.getDialog().getOptionsFor("versions");
        cfSingleVersions = tester.getDialog().getOptionsFor("customfield_10000");
        cfVersions = tester.getDialog().getOptionsFor("customfield_10001");

        // cheat here because on the edit screen, no options are properly grouped. so only assert that the affects list
        // is the same as the custom fields (ignore fix for, who's ordering of un/released is opposite)
        assertEquals(getUnifiedVersions(fixForVersions), EasyList.build("New Version 5", "New Version 6", "New Version 4", "New Version 1"));
        assertEquals(getUnifiedVersions(affectsVersions), EasyList.build("New Version 4", "New Version 1", "New Version 5", "New Version 6"));
        assertEquals(getUnifiedVersions(affectsVersions), getUnifiedVersions(cfSingleVersions));
        assertEquals(getUnifiedVersions(affectsVersions), getUnifiedVersions(cfVersions));
    }

    /**
     * Return all versions in the order of Released (DESC) then Unreleased (ASC) if the options contain markers.
     * Note this order cannot be guaranteed when options do not contain markers.
     *
     * @param options values from the form element
     * @return a list of versions ordered correctly.
     */
    private List/*<String>*/ getUnifiedVersions(String[] options)
    {
        List allVersions;
        // two expected formats: either list contains Unknown and does NOT contain marker options
        // or list doesn't contain Unknown and DOES contain marker options.
        // here we don't actually know which is listed first, sometimes its released, sometimes unreleased,
        // so be careful what assumptions you make. for now we assume that version CFs have same ordering as Affects system field
        if (Arrays.asList(options).contains("Unknown"))
        {
            allVersions = new ArrayList(Arrays.asList(options));
            allVersions.remove("Unknown");
        }
        else
        {
            allVersions = new ArrayList();
            allVersions.addAll(getReleasedVersions(options, 2));
            allVersions.addAll(getUnreleasedVersions(options, 2));
        }
        return allVersions;
    }

    private List/*<String>*/ getReleasedVersions(String[] options, int expected)
    {
        return getVersions(options, expected, "Released Versions");
    }

    private List/*<String>*/ getUnreleasedVersions(String[] options, int expected)
    {
        return getVersions(options, expected, "Unreleased Versions");
    }

    private List/*<String>*/ getVersions(final String[] options, final int expected, final String flag)
    {
        List versions = Arrays.asList(options);
        final int i = versions.indexOf(flag) + 1;
        return versions.subList(i, i + expected);
    }
}
