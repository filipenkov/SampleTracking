package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test that the JQL simple context (i.e. the project and issuetype clauses) is calculated correctly and displays the
 * correct fields.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestContextFieldValues extends FuncTestCase
{
    private static final String FIELD_FIXFOR = "fixfor";
    private static final String FIELD_AFFECTS = "version";
    private static final String FIELD_COMPONENT = "component";
    private static final String FIELD_STATUS = "status";

    private static final String CF_CASCADINGSELECT = "customfield_10022";
    private static final String CF_SINGLEVERSION = "customfield_10039";
    private static final String CF_MULTIPLEVERSION = "customfield_10021";
    private static final String CF_DATE = "customfield_10032:after";
    private static final String CF_DATE_TIME = "customfield_10023:after";
    private static final String CF_FREE_TEXT = "customfield_10033";
    private static final String CF_GROUP_PICKER = "customfield_10024";
    private static final String CF_IMPORT_ID = "customfield_10034";
    private static final String CF_MULTI_GROUP = "customfield_10035";
    private static final String CF_MULTI_USER = "customfield_10036";
    private static final String CF_NUMBER = "customfield_10027";
    private static final String CF_READ_TEXT = "customfield_10038";
    private static final String CF_TEXT = "customfield_10030";
    private static final String CF_URL = "customfield_10040";
    private static final String CF_USER_SELECT = "customfield_10031";
    private static final String CF_MULTI_CHECK = "customfield_10025";
    private static final String CF_MULTI_SELECT = "customfield_10026";
    private static final String CF_PROJECT_PICKER = "customfield_10037";
    private static final String CF_SELECT_LIST = "customfield_10029";
    private static final String CF_RADIO = "customfield_10028";


    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestJqlContextValues.xml");
    }

    public void testFieldValues() throws Exception
    {
        AssertionOptions options = new AssertionOptions();
        options.addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "OneStatus", "TwoStatus");
        options.addDefaultProjectPickerOptions();
        options.addOptions("Global");
        options.addMultiCheckOptions("10014");
        options.addRadioOptions("10035");
        options.setVisibility(true);
        assertNavigatorState("", options);

        //
        // Check for single project context with project one.
        //
        AssertionOptions template = new AssertionOptions();
        template.addUnreleasedVersions("Version11");
        template.addComponents("Component11", "Component12", "Component13");
        
        options = new AssertionOptions(template);
        options.addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "OneStatus");
        assertNavigatorState("project = one", options);
        assertNavigatorState("project = one and type in (task, onetype)", options);

        options = new AssertionOptions(template).addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed");
        assertNavigatorState("project = one and issuetype = task", options);

        options = new AssertionOptions(template).addStatuses("Open", "OneStatus");
        options.setVisibility(true).addOptions("One");
        options.addMultiCheckOptions("10015");
        options.addRadioOptions("10034");
        options.addDefaultProjectPickerOptions();
        assertNavigatorState("project = one and issuetype = oneTYPE", options);

        //
        // Check for single project context with project two.
        //
        options = new AssertionOptions();
        options.addComponents("Component21");
        options.addReleasedVersions("Version21").addUnreleasedVersions("Version23", "Version22");
        options.addStatuses("Open", "TwoStatus");
        options.addDefaultProjectPickerOptions();
        options.addMultiCheckOptions("10016");
        options.addRadioOptions("10033");
        options.addOptions("Two");
        options.setVisibility(true);

        assertNavigatorState("project = two", options);
        assertNavigatorState("project = two and issuetype = twotype", options);
        assertNavigatorState("project = two and issuetype = task", options);
        assertNavigatorState("project = two and issuetype in (twotype, task)", options);


        //
        // Check for single project context with project two.
        //        
        options = new AssertionOptions();
        options.addUnreleasedVersions("Version31");
        options.addComponents("Component31");
        options.addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed");
        options.addDefaultProjectPickerOptions();
        options.addOptions("Global");
        options.addMultiCheckOptions("10014");
        options.addRadioOptions("10035");
        options.setVisibility(true);

        assertNavigatorState("project = three", options);
        assertNavigatorState("project = three and type = bug", options);
        assertNavigatorState("project = three and type in (bug, task, improvement)", options);


        //
        // Check for multiple project context with project one and two.
        //
        options = new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "OneStatus", "TwoStatus");
        assertNavigatorState("project in (one, two)", options);
        assertNavigatorState("issuetype in (onetype, twotype) and project in (one, two)", options);
        assertNavigatorState("issuetype in (onetype, twotype, task) and project in (one, two)", options);
        
        assertNavigatorState("project in (one, two) and issuetype = onetype", new AssertionOptions().addStatuses("Open", "OneStatus", "TwoStatus"));

        options = new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "TwoStatus");
        assertNavigatorState("project in (one, two) and issuetype = twotype", options);
        assertNavigatorState("project in (one, two) and issuetype = task", options);

        //
        //Check for multiple project context with project three and one.
        //
        options = new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "OneStatus");
        assertNavigatorState("project in (one, three)", options);
        assertNavigatorState("issuetype in (onetype, twotype) and project in (one, three)", options);
        assertNavigatorState("issuetype in (onetype, twotype, task) and project in (one, three)", options);
        assertNavigatorState("project in (three, one) and issuetype in (onetype, twotype, task)", options);
        assertNavigatorState("project in (one, three) and issuetype = task", new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed"));

        //
        //Check for multiple project context with project two and three.
        //
        options = new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "TwoStatus");
        assertNavigatorState("project in (two, three)", options);
        assertNavigatorState("issuetype in (onetype, twotype) and project in (two, three)", options);
        assertNavigatorState("issuetype in (onetype, twotype, task) and project in (two, three)", options);
        assertNavigatorState("project in (three, two) and issuetype in (onetype, twotype, task)", options);
        assertNavigatorState("project in (two, three) and issuetype = task", options);

        //
        //Check for multiple project context with projects one, two, three.
        //
        options = new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "OneStatus", "TwoStatus");
        assertNavigatorState("project in (one, two, three)", options);
        assertNavigatorState("project in (one, two, three) and issuetype = onetype", options);
        assertNavigatorState("project in (one, two, three) and issuetype in (onetype, twotype)", options);

        options = new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "TwoStatus");
        assertNavigatorState("project in (one, two, three) and issuetype = twotype", options);
        assertNavigatorState("project in (one, two, three) and issuetype in (improvement, 'new feature', twotype)", options);

        options = new AssertionOptions().addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "TwoStatus");
        assertNavigatorState("project in (one, two, three) and issuetype = twotype", options);
        assertNavigatorState("project in (one, two, three) and issuetype in (improvement, 'new feature', twotype)", options);

        //
        //Check for some issue type
        //
        options = new AssertionOptions();
        options.addDefaultProjectPickerOptions();
        options.addOptions("Global");
        options.addMultiCheckOptions("10014");
        options.addRadioOptions("10035");
        options.setVisibility(true);
        options.addStatuses("Open", "In Progress", "Reopened", "Resolved", "Closed", "OneStatus", "TwoStatus");

        assertNavigatorState("issuetype = onetype", options);
        assertNavigatorState("issuetype = twotype", options);
        assertNavigatorState("issuetype in (twotype, onetype)", options);
        assertNavigatorState("issuetype in (twotype, onetype, task)", options);
    }

    private void assertNavigatorState(String jqlQuery, AssertionOptions options)
    {
        log("Asserting naviagator state for: '" + jqlQuery + "'.");
        navigation.issueNavigator().createSearch(jqlQuery);
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        assertEquals(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, navigation.issueNavigator().getCurrentEditMode());

        assertNavigatorState(options);
    }

    private void assertNavigatorState(AssertionOptions options)
    {
        //Check the standard version fields.
        assertOptions(FIELD_FIXFOR, options.getFixForVersionsOptions());
        assertOptions(FIELD_AFFECTS, options.getAffectsVersionsOptions());
        assertOptions(FIELD_COMPONENT, options.getComponentOptions());
        assertOptions(FIELD_STATUS, options.getStatusOptions());

        final List<String> customFieldOptions = options.getCustomFieldOptions();
        assertOptions(CF_SINGLEVERSION, customFieldOptions);
        assertOptions(CF_MULTIPLEVERSION, customFieldOptions);
        assertOptions(CF_CASCADINGSELECT, options.getCascadingSelectOptions());
        assertValues(CF_MULTI_CHECK, options.getMultiCheckOptions());
        assertOptions(CF_MULTI_SELECT, options.getMultiSelectOptions());
        assertOptions(CF_PROJECT_PICKER, options.getProjectPickerOptions());
        assertOptions(CF_SELECT_LIST, options.getSelectListOptions());
        assertValues(CF_RADIO, options.getRadioOptions());

        assertVisibility(CF_DATE, options.isDateCfVisible());
        assertVisibility(CF_DATE_TIME, options.isDateTimeCfVisible());
        assertVisibility(CF_FREE_TEXT, options.isFreeTextCfVisible());
        assertVisibility(CF_IMPORT_ID, options.isImportIdCfVisible());
        assertVisibility(CF_GROUP_PICKER, options.isGoupCfVisible());
        assertVisibility(CF_MULTI_GROUP, options.isMultiGroupCfVisible());
        assertVisibility(CF_MULTI_USER, options.isMultiUserPickerCfVisible());
        assertVisibility(CF_NUMBER, options.isNumberCfVisible());
        assertVisibility(CF_READ_TEXT, options.isReadCfVisible());
        assertVisibility(CF_TEXT, options.isTextCfVisible());
        assertVisibility(CF_URL, options.isUrlCfVisible());
        assertVisibility(CF_USER_SELECT, options.isUserSelectCfVisible());
    }

    private void assertVisibility(final String fieldName, final boolean visible)
    {
        if (visible)
        {
            tester.assertFormElementPresent(fieldName);
        }
        else
        {
            tester.assertFormElementNotPresent(fieldName);
        }
    }

    private void assertOptions(final String fieldName, List<String> options)
    {
        log("Checking that field '" + fieldName + "' has options: " + options);
        if (options.isEmpty())
        {
            tester.assertFormElementNotPresent(fieldName);
        }
        else
        {
            tester.assertFormElementPresent(fieldName);
            tester.assertOptionsEqual(fieldName, options.toArray(new String[options.size()]));
        }
    }

    private void assertValues(final String fieldName, List<String> values)
    {
        log("Checking that field '" + fieldName + "' has values: " + values);
        if (values.isEmpty())
        {
            tester.assertFormElementNotPresent(fieldName);
        }
        else
        {
            tester.assertFormElementPresent(fieldName);
            tester.assertOptionValuesEqual(fieldName, values.toArray(new String[values.size()]));
        }
    }


    private static class AssertionOptions
    {
        private List<String> releasedVersions;
        private List<String> unlreasedVersions;
        private List<String> components;
        private List<String> status;

        private List<String> cascaingSelect;
        private List<String> multiCheck;
        private List<String> multiSelect;
        private List<String> projectPicker;
        private List<String> selectList;
        private List<String> radioList;

        private boolean dateCf;
        private boolean dateTimeCf;
        private boolean freeTextCf;
        private boolean importIdCf;
        private boolean groupPickerCf;
        private boolean multiGroupPickerCf;
        private boolean multiUserPickerCf;
        private boolean numberCf;
        private boolean readCf;
        private boolean textCf;
        private boolean urlCf;
        private boolean userSelectCf;


        private AssertionOptions(AssertionOptions copy)
        {
            releasedVersions = new ArrayList<String>(copy.releasedVersions);
            unlreasedVersions = new ArrayList<String>(copy.unlreasedVersions);
            components = new ArrayList<String>(copy.components);
            status = new ArrayList<String>(copy.status);
            cascaingSelect = new ArrayList<String>(copy.status);
            multiCheck = new ArrayList<String>(copy.multiCheck);
            multiSelect = new ArrayList<String>(copy.multiSelect);
            projectPicker = new ArrayList<String>(copy.projectPicker);
            selectList = new ArrayList<String>(copy.selectList);
            radioList = new ArrayList<String>(copy.radioList);

            dateTimeCf = copy.dateTimeCf;
            dateCf = copy.dateCf;
            freeTextCf = copy.dateCf;
            importIdCf = copy.importIdCf;
            groupPickerCf = copy.groupPickerCf;
            multiGroupPickerCf = copy.multiGroupPickerCf;
            multiUserPickerCf = copy.multiUserPickerCf;
            numberCf = copy.numberCf;
            readCf = copy.readCf;
            textCf = copy.textCf;
            urlCf = copy.urlCf;
            userSelectCf = copy.userSelectCf;
        }

        private AssertionOptions()
        {
            releasedVersions = new ArrayList<String>();
            unlreasedVersions = new ArrayList<String>();
            components = new ArrayList<String>();
            status = new ArrayList<String>();
            cascaingSelect = new ArrayList<String>();
            multiCheck = new ArrayList<String>();
            multiSelect = new ArrayList<String>();
            projectPicker = new ArrayList<String>();
            selectList = new ArrayList<String>();
            radioList = new ArrayList<String>();

            dateTimeCf = false;
            dateCf = false;
            freeTextCf = false;
            importIdCf = true;
            groupPickerCf = false;
            multiGroupPickerCf = false;
            multiUserPickerCf = false;
            numberCf = false;
            readCf = true;
            textCf = false;
            urlCf = false;
            userSelectCf = false;
        }

        public AssertionOptions setVisibility(boolean visible)
        {
            setDateCfVisible(visible);
            setDateTimeCfVisible(visible);
            setFreeTextCfVisible(visible);
            setGroupCfVisible(visible);
            setMultiUserCfVisible(visible);
            setMultiGroupCfVisible(visible);
            setNumberCfVisible(visible);
            setTextCf(visible);
            setUrlCfVisible(visible);
            setUserSelectCfVisible(visible);

            return this;
        }

        public AssertionOptions addOptions(String...options)
        {
            addCascaingSelectOptions(options);
            addMultiSelectOptions(options);
            addSelectListOptions(options);

            return this;
        }

        public boolean isUrlCfVisible()
        {
            return urlCf;
        }

        public AssertionOptions setUrlCfVisible(final boolean urlCf)
        {
            this.urlCf = urlCf;
            return this;
        }

        public boolean isUserSelectCfVisible()
        {
            return userSelectCf;
        }

        public AssertionOptions setUserSelectCfVisible(final boolean userSelectCf)
        {
            this.userSelectCf = userSelectCf;
            return this;
        }

        public boolean isTextCfVisible()
        {
            return textCf;
        }

        public AssertionOptions setTextCf(final boolean textCf)
        {
            this.textCf = textCf;
            return this;
        }

        public boolean isReadCfVisible()
        {
            return readCf;
        }

        private AssertionOptions addRadioOptions(String... options)
        {
            radioList.addAll(Arrays.asList(options));
            return this;
        }

        private List<String> getRadioOptions()
        {
            return createOptionsList(radioList, "-1");
        }

        private AssertionOptions addSelectListOptions(String... options)
        {
            selectList.addAll(Arrays.asList(options));
            return this;
        }

        private List<String> getSelectListOptions()
        {
            return createOptionsList(selectList, "Any");
        }

        private AssertionOptions addDefaultProjectPickerOptions()
        {
            return addProjectPickerOptions("One", "Three", "Two");
        }

        private AssertionOptions addProjectPickerOptions(String... options)
        {
            projectPicker.addAll(Arrays.asList(options));
            return this;
        }

        private List<String> getProjectPickerOptions()
        {
            return createOptionsList(projectPicker, "None");
        }

        private AssertionOptions addMultiSelectOptions(String... options)
        {
            multiSelect.addAll(Arrays.asList(options));
            return this;
        }

        private List<String> getMultiSelectOptions()
        {
            return createOptionsList(multiSelect, "Any");
        }

        private AssertionOptions addMultiCheckOptions(String... options)
        {
            multiCheck.addAll(Arrays.asList(options));
            return this;
        }

        private List<String> getMultiCheckOptions()
        {
            return multiCheck;
        }

        private AssertionOptions addCascaingSelectOptions(String... options)
        {
            cascaingSelect.addAll(Arrays.asList(options));
            return this;
        }

        private List<String> getCascadingSelectOptions()
        {
            return createOptionsList(cascaingSelect, "Please select...", "Any");
        }

        private List<String> createOptionsList(final List<String> options, final String... extras)
        {
            if (options.isEmpty())
            {
                return Collections.emptyList();
            }
            else
            {
                List<String> newOptions = new ArrayList<String>();
                newOptions.addAll(Arrays.asList(extras));
                newOptions.addAll(options);

                return newOptions;
            }
        }

        private boolean isDateCfVisible()
        {
            return dateCf;
        }

        private AssertionOptions setDateCfVisible(boolean visible)
        {
            this.dateCf = visible;
            return this;
        }

        private boolean isFreeTextCfVisible()
        {
            return freeTextCf;
        }

        private AssertionOptions setFreeTextCfVisible(boolean visible)
        {
            this.freeTextCf = visible;
            return this;
        }

        private boolean isDateTimeCfVisible()
        {
            return dateTimeCf;
        }

        private AssertionOptions setDateTimeCfVisible(boolean visible)
        {
            dateTimeCf = visible;
            return this;
        }

        private boolean isGoupCfVisible()
        {
            return groupPickerCf;
        }

        private AssertionOptions setGroupCfVisible(boolean visible)
        {
            groupPickerCf = visible;
            return this;
        }

        private boolean isMultiGroupCfVisible()
        {
            return multiGroupPickerCf;
        }

        private AssertionOptions setMultiGroupCfVisible(boolean visible)
        {
            multiGroupPickerCf = visible;
            return this;
        }

        private AssertionOptions setMultiUserCfVisible(boolean visible)
        {
            multiUserPickerCf = visible;
            return this;
        }

        private boolean isMultiUserPickerCfVisible()
        {
            return multiUserPickerCf;
        }

        private boolean isImportIdCfVisible()
        {
            return importIdCf;
        }

        private boolean isNumberCfVisible()
        {
            return numberCf;
        }

        private AssertionOptions setNumberCfVisible(boolean visible)
        {
            numberCf = visible;
            return this;
        }

        private AssertionOptions addStatuses(String... statuses)
        {
            this.status.addAll(Arrays.asList(statuses));
            return this;
        }

        private List<String> getStatusOptions()
        {
            if (this.status.isEmpty())
            {
                return Collections.emptyList();
            }
            else
            {
                List<String> statusOptions = new ArrayList<String>();
                statusOptions.add("Any");
                statusOptions.addAll(this.status);
                return statusOptions;
            }
        }

        private AssertionOptions addComponents(String... components)
        {
            this.components.addAll(Arrays.asList(components));
            return this;
        }

        private List<String> getComponentOptions()
        {
            if (this.components.isEmpty())
            {
                return Collections.emptyList();
            }
            else
            {
                List<String> customFieldOptions = new ArrayList<String>();
                customFieldOptions.add("Any");
                customFieldOptions.add("No Component");
                customFieldOptions.addAll(components);
                return customFieldOptions;
            }
        }

        private AssertionOptions addReleasedVersions(String... versions)
        {
            releasedVersions.addAll(Arrays.asList(versions));
            return this;
        }

        private AssertionOptions addUnreleasedVersions(String... versions)
        {
            unlreasedVersions.addAll(Arrays.asList(versions));
            return this;
        }

        private List<String> getCustomFieldOptions()
        {
            if (releasedVersions.isEmpty() && unlreasedVersions.isEmpty())
            {
                return Collections.emptyList();
            }
            else
            {
                List<String> customFieldOptions = new ArrayList<String>();
                customFieldOptions.add("Unknown");
                customFieldOptions.addAll(releasedVersions);
                customFieldOptions.addAll(unlreasedVersions);
                return customFieldOptions;
            }
        }

        private List<String> getFixForVersionsOptions()
        {
            if (releasedVersions.isEmpty() && unlreasedVersions.isEmpty())
            {
                return Collections.emptyList();
            }
            else
            {
                List<String> fixFor = new ArrayList<String>();
                fixFor.add("Any");
                fixFor.add("No Version");
                if (!unlreasedVersions.isEmpty())
                {
                    fixFor.add("Unreleased Versions");
                    fixFor.addAll(unlreasedVersions);
                }
                if (!releasedVersions.isEmpty())
                {
                    fixFor.add("Released Versions");
                    fixFor.addAll(releasedVersions);
                }
                return fixFor;
            }
        }

        private List<String> getAffectsVersionsOptions()
        {
            if (releasedVersions.isEmpty() && unlreasedVersions.isEmpty())
            {
                return Collections.emptyList();
            }
            else
            {
                List<String> affects = new ArrayList<String>();
                affects.add("Any");
                affects.add("No Version");
                if (!releasedVersions.isEmpty())
                {
                    affects.add("Released Versions");
                    affects.addAll(releasedVersions);
                }
                if (!unlreasedVersions.isEmpty())
                {
                    affects.add("Unreleased Versions");
                    affects.addAll(unlreasedVersions);
                }
                return affects;
            }
        }
    }
}
