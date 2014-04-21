package com.atlassian.jira.webtest.selenium.fields;

import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.WorkflowTransitionDialog;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;
import com.atlassian.jira.webtest.selenium.framework.model.WorkflowTransition;

import java.util.Arrays;
import java.util.List;

/**
 * JRADEV-2828: Tests that frother controls can be enabled & disabled for all components & version fields.
 */
@WebTest({Category.SELENIUM_TEST })
public class TestFrotherControlRenderers extends JiraSeleniumTest
{
    private static final String MULTI_VERSION_CF_ID = "customfield_10030";
    private static final String SINGLE_VERSION_CF_ID = "customfield_10031";
    private WorkflowTransitionDialog resolveIssueDialog;

    private static class Field
    {
        static Field AFFECTS_VERSIONS = new Field("versions", true);
        static Field COMPONENTS = new Field("components", true);
        static Field LABELS = new Field("labels", true);
        static Field FIX_VERSIONS = new Field("fixVersions", true);

        static Field SINGLE_VERSION_CF = new Field(SINGLE_VERSION_CF_ID, false);
        static Field MULTI_VERSION_CF = new Field(MULTI_VERSION_CF_ID, true);

        public String fieldId;
        public boolean isFrotherControl;

        private Field(final String fieldId, final boolean frotherControl)
        {
            this.fieldId = fieldId;
            isFrotherControl = frotherControl;
        }
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestFrotherControlRenderers.xml");
        this.resolveIssueDialog = new WorkflowTransitionDialog(context(), WorkflowTransition.RESOLVE);
    }

    public void testSwitchRenderers()
    {
        final List<Field> allFields = Arrays.asList(Field.AFFECTS_VERSIONS, Field.COMPONENTS, Field.LABELS, Field.FIX_VERSIONS, Field.SINGLE_VERSION_CF, Field.MULTI_VERSION_CF);
        final List<Field> toggleableFields = Arrays.asList(Field.AFFECTS_VERSIONS, Field.COMPONENTS, Field.FIX_VERSIONS, Field.MULTI_VERSION_CF);
        //the data has them set as autocomplete so check first if the frother control renders in all the right places
        assertFieldRendererStates(allFields);

        for (Field field : toggleableFields)
        {
            getAdministration().setRendererForField(field.fieldId, "Select List Renderer");
            field.isFrotherControl = false;
            assertFieldRendererStates(allFields);
        }

        for (Field field : toggleableFields)
        {
            getAdministration().setRendererForField(field.fieldId, "Autocomplete Renderer");
            field.isFrotherControl = true;
            assertFieldRendererStates(allFields);
        }
    }

    //unfortunately we have 2 jsps rendering the default field config and custom field configs so we
    //just neet to make sure that the custom one shows renderers as well.
    public void testCustomFieldConfigAlsoShowsRenderers()
    {
        getNavigator().gotoAdmin();
        client.click("field_configuration", true);

        client.type("fieldLayoutName", "Some random field configuration");
        client.click("add_submit", true);

        client.click("id=configure-Some random field configuration", true);
        assertThat.textPresent("Autocomplete Renderer");
    }

    private void assertFieldRendererStates(final List<Field> allFields)
    {
        //check edit issue
        getNavigator().issue().editIssue("MKY-1");
        assertFrotherControlEnabledForAllFields(allFields);

        //check bulk edit
        getNavigator().findIssuesWithJql("project=MKY").bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES).selectAllIssues().
                chooseOperation(BulkChangeWizard.BulkOperations.EDIT);
        assertFrotherControlEnabledForAllFields(allFields);

        //check bulk transition
        getNavigator().findIssuesWithJql("project=MKY").bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES).selectAllIssues();
        client.check("operation", "bulk.workflowtransition.operation.name");
        client.click("id=Next", true);
        client.check("wftransition", "jira_5_5");
        client.click("id=Next", true);
        assertFrotherControlEnabledForAllFields(allFields);

        //check resolve screen in dialog
        getNavigator().issue().viewIssue("MKY-1");
        resolveIssueDialog.openFromViewIssue();
        assertFrotherControlEnabledForAllFields(allFields, ".aui-popup");
        resolveIssueDialog.cancel(CancelType.BY_CLICK);

        //check create issue
        getNavigator().gotoCreateIssueScreen("monkey", "Bug");
        assertFrotherControlEnabledForAllFields(allFields);


        //The searchers for these fields should NEVER display the frother control!
        getNavigator().issueNavigator().createSearch("project=MKY").gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        getNavigator().expandAllNavigatorSections();
        assertFrotherControlNotShown(null, "searcher-fixfor");
        assertFrotherControlNotShown(null, "searcher-component");
        assertFrotherControlNotShown(null, "searcher-version");
        assertFrotherControlNotShown(null, "searcher-labels");
        assertFrotherControlNotShown(null, MULTI_VERSION_CF_ID);
        assertFrotherControlNotShown(null, SINGLE_VERSION_CF_ID);
    }

    private void assertFrotherControlEnabledForAllFields(List<Field> fields)
    {
        assertFrotherControlEnabledForAllFields(fields, null);
    }

    private void assertFrotherControlEnabledForAllFields(List<Field> fields, String domContext)
    {
        for (Field field : fields)
        {
            if (field.isFrotherControl)
            {
                assertFrotherControlShown(domContext, field.fieldId);
            }
            else
            {
                assertFrotherControlNotShown(domContext, field.fieldId);
            }
        }
    }

    private void assertFrotherControlNotShown(String domContext, String fieldId)
    {
        final String selector = getSelector(domContext, fieldId);
        assertThat.elementNotPresentByTimeout(selector + "-textarea", DROP_DOWN_WAIT);
        assertThat.visibleByTimeout(selector, DROP_DOWN_WAIT);
    }

    private void assertFrotherControlShown(String domContext, String fieldId)
    {
        final String selector = getSelector(domContext, fieldId);
        assertThat.elementPresentByTimeout(selector + "-textarea", DROP_DOWN_WAIT);
        assertThat.notVisibleByTimeout(selector, DROP_DOWN_WAIT);
    }

    private String getSelector(final String domContext, final String fieldId)
    {
        return domContext != null ? "jquery=" + domContext + " #" + fieldId : "jquery=#" + fieldId;
    }
}
