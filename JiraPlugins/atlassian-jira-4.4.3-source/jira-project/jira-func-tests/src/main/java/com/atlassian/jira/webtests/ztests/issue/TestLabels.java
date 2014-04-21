package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryParser;
import com.atlassian.jira.functest.framework.labels.Labels;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.JQL })
public class TestLabels extends FuncTestCase
{
    public void testSearching()
    {
        administration.restoreData("TestLabels.xml");
        _testSearching("labels", "labels", "Labels");
    }

    public void testCustomFieldSearching()
    {
        administration.restoreData("TestLabels.xml");
        _testSearching("customfield_10000", "Epic", "Epic");
    }

    // The "edit" pencil shouldn't show when the issue is closed
    public void testNoEditWhenClosed() throws Exception
    {
        administration.restoreData("TestLabels.xml");
        navigation.issue().closeIssue("HSP-1", "Fixed", "closing for testing purposes");

        // view issue
        navigation.issue().gotoIssue("HSP-1");
        assertions.assertNodeDoesNotExist("//*[contains(@class, 'edit-labels')]");

        // issue navigator
        navigation.issueNavigator().createSearch("key = \"HSP-1\"");
        assertions.assertNodeDoesNotExist("id('labels-10001-value')//*[contains(@class, 'edit-labels')]");
        assertions.assertNodeDoesNotExist("id('customfield_10000-10001-value')//*[contains(@class, 'edit-labels')]");
    }

    public void testProjectTabPanel()
    {
        administration.restoreData("TestLabelsProjectTabPanel.xml");

        navigation.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project%3Alabels-heatmap-panel");
        //first assert we're on the right view
        tester.assertLinkNotPresentWithText("Popular Labels");
        tester.assertLinkNotPresentWithText("alphabetically");
        tester.assertLinkPresentWithText("All Labels");
        tester.assertLinkPresentWithText("Epic");
        tester.assertLinkPresentWithText("Stuff");
        tester.assertLinkPresentWithText("popularity");
        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "Popular Labels");
        text.assertTextPresent(locator, "Labels");
        text.assertTextPresent(locator, "alphabetically");

        text.assertTextPresent(locator, "Below are the 8 most popular labels. The bigger the text, the more popular the label. Click on a label to see its associated content.");
        text.assertTextSequence(locator, "aa", "bb", "cc", "dd", "duck", "duffy", "mickey", "mouse");

        //now lets sort by popularity!
        navigation.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:labels-heatmap-panel&labels.order=pop&selected.field=labels");
        tester.assertLinkNotPresentWithText("Popular Labels");
        tester.assertLinkNotPresentWithText("popularity");
        tester.assertLinkPresentWithText("All Labels");
        tester.assertLinkPresentWithText("Epic");
        tester.assertLinkPresentWithText("Stuff");
        tester.assertLinkPresentWithText("alphabetically");
        locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "Popular Labels");
        text.assertTextPresent(locator, "Labels");
        text.assertTextPresent(locator, "popularity");

        text.assertTextPresent(locator, "Below are the 8 most popular labels. The bigger the text, the more popular the label. Click on a label to see its associated content.");
        text.assertTextSequence(locator, "mouse", "mickey", "duffy", "duck", "dd", "cc", "bb", "aa");

        //now try the other custom fields
        navigation.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:labels-heatmap-panel&labels.view=popular&selected.field=customfield_10031&labels.order=pop");
        //make sure all the other options were retained!
        tester.assertLinkNotPresentWithText("Popular Labels");
        tester.assertLinkNotPresentWithText("popularity");
        tester.assertLinkNotPresentWithText("Epic");
        tester.assertLinkPresentWithText("All Labels");
        tester.assertLinkPresentWithText("Stuff");
        tester.assertLinkPresentWithText("alphabetically");
        locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "Popular Labels");
        text.assertTextPresent(locator, "Labels");
        text.assertTextPresent(locator, "Epic");
        text.assertTextPresent(locator, "popularity");

        text.assertTextPresent(locator, "Below are the 4 most popular labels. The bigger the text, the more popular the label. Click on a label to see its associated content.");
        text.assertTextSequence(locator, "monroe", "marylyn", "lewis", "john");

        //the last customfield doesn't have any labels
        navigation.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:labels-heatmap-panel&labels.view=popular&selected.field=customfield_10033&labels.order=pop");
        //make sure all the other options were retained!
        tester.assertLinkNotPresentWithText("Popular Labels");
        tester.assertLinkNotPresentWithText("Stuff");
        tester.assertLinkPresentWithText("All Labels");
        tester.assertLinkPresentWithText("Epic");
        locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "Popular Labels");
        text.assertTextPresent(locator, "Stuff");
        text.assertTextPresent(locator, "No Labels Found");

        //finally lets go to the all labels view for the epic custom field field
        navigation.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:labels-heatmap-panel&labels.view=all&selected.field=customfield_10031");
        tester.assertLinkNotPresentWithText("All Labels");
        tester.assertLinkNotPresentWithText("Epic");
        tester.assertLinkPresentWithText("Popular Labels");
        tester.assertLinkPresentWithText("Stuff");
        tester.assertLinkPresentWithText("Labels");
        locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "All Labels");
        text.assertTextPresent(locator, "Labels");

        text.assertTextPresent(locator, "Below are the 4 most recently used labels, listed alphabetically. Click on a label to see its associated content.");
        text.assertTextSequence(locator, "A-Z", "john", "lewis", "marylyn", "monroe");

        //finally the all labels view should also work with no labels
        navigation.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project:labels-heatmap-panel&labels.view=all&selected.field=customfield_10033");
        locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "No Labels Found");
    }

    private void _testSearching(String fieldId, String field, String fieldName)
    {
        //check in works case insensitive
        navigation.issueNavigator().createSearch(field + " in (aa, TeSt)");
        TableLocator locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-2");
        text.assertTextPresent(locator, "HSP-1");

        //switch to simple mode and make sure the labels field still has the labels entered
        tester.clickLinkWithText("simple");
        assertEquals("aa TeSt", tester.getDialog().getFormParameterValue(fieldId));

        //now check an equals search!
        //check in works case insensitive
        navigation.issueNavigator().createSearch(field + " = \"fIRst\"");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-1");
        text.assertTextNotPresent(locator, "HSP-2");

        //switch to simple mode and make sure the labels field still has the labels entered
        tester.clickLinkWithText("simple");
        assertEquals("fIRst", tester.getDialog().getFormParameterValue(fieldId));

        //switch back to advanced and check the jql query is still there!
        tester.clickLinkWithText("advanced");
        tester.setWorkingForm("jqlform");
        assertEquals(field + " = \"fIRst\"", tester.getDialog().getForm().getParameterValue("jqlQuery"));
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-1");
        text.assertTextNotPresent(locator, "HSP-2");

        //search for empty...shouldn't find anything
        navigation.issueNavigator().createSearch(field + " is empty");
        text.assertTextPresent(new WebPageLocator(tester), "No matching issues found. ");

        final String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Another test issue!!!!");
        //search for empty...should find the new issue now
        navigation.issueNavigator().createSearch(field + " is empty");
        text.assertTextNotPresent(new WebPageLocator(tester), "No matching issues found. ");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, issueKey);

        //lets created a saved filter
        navigation.issueNavigator().createSearch(field + " in (TeST, aa)");
        final long filterId = navigation.issueNavigator().saveCurrentAsNewFilter("labelsearch", "in search", true, null);
        navigation.issueNavigator().loadFilter(filterId, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-1");
        text.assertTextPresent(locator, "HSP-2");

        tester.clickLink("viewfilter");
        text.assertTextSequence(new IdLocator(tester, "filter-summary"), fieldName + ":", "TeST aa");

        //JRADEV-1342: test not searching
        navigation.issueNavigator().createSearch(field + " != couple");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextPresent(locator, "HSP-2");
        text.assertTextNotPresent(locator, "HSP-1");
        text.assertTextNotPresent(locator, "HSP-3");

        navigation.issueNavigator().createSearch(field + " not in (aa, bb)");
        locator = new TableLocator(tester, "issuetable");
        text.assertTextNotPresent(locator, "HSP-2");
        text.assertTextPresent(locator, "HSP-1");
        text.assertTextNotPresent(locator, "HSP-3");
    }

    public void testEditIssueLabelsDoesCreateChangeItem() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoEditIssue("HSP-3");
        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, "issue-edit","Update");
        formParameterUtil.addOptionToHtmlSelect("labels", new String[]{"Label"});
        formParameterUtil.submitForm();
        navigation.issue().gotoIssueChangeHistory("HSP-3");
        assertHistoryContains(ADMIN_FULLNAME, "Labels", "", "Label");
    }

    public void testCreateIssueLabelsDoesNotCreateChangeItem() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        Map<String,String[]>  params = new MapBuilder<String,String[]>().add("labels",new String[]{"label"}).toMap();
        String issueKey = navigation.issue().createIssue("homosapien","Bug","Bug With Labels",params);
        navigation.issue().gotoIssueChangeHistory(issueKey);
        assertNoHistory();
    }

    //JRADEV-1397 - Simple to Advanced uses the fieldID rather than the field name in JQL.
    public void testSimpleToAdvanced()
    {
        administration.restoreData("TestLabels.xml");
        final IssueNavigatorNavigation issueNav = navigation.issueNavigator();

        issueNav.gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        tester.setFormElement("customfield_10000", "whatever");
        issueNav.runSearch();

        issueNav.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        //Make sure that we use the label name rather than the custom field id.
        assertions.getIssueNavigatorAssertions().assertAdvancedSearch(tester, "Epic", "=", "whatever");
        issueNav.gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.assertFormElementEquals("customfield_10000", "whatever");
    }

    public void testEditIssueLabelsStandaloneDoesNotSuck()
    {
        final Labels expectedLabels = new Labels(true, true, true, "TEST", "aaafirst", "couple", "first", "labels", "of", "test");
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        final String labelsDomId = "labels-10000-labels";
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);
        navigation.issue().editLabels(10000);
        assertions.assertNodeExists("//input[@type='hidden'][@name='id']");
        assertions.assertNodeExists("//select[@id='labels']");
        tester.submit("edit-labels-submit");
        // Haven't changed the selected labels, so all should remain.
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);
    }

    public void testEditCustomIssueLabelsStandaloneDoesNotSuck()
    {
        final String customFieldName = "customfield_10000";
        final String labelsDomId = "labels-10000-" + customFieldName;
        final Labels expectedLabels = new Labels(true, true, true, "TEST", "aaafirst", "couple", "first", "labels", "of", "test");

        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);

        navigation.issue().editCustomLabels(10000, 10000);
        assertions.assertNodeExists("//input[@type='hidden'][@name='id']");
        assertions.assertNodeExists("//input[@type='hidden'][@name='customFieldId']");
        assertions.assertNodeExists("//select[@id='" + customFieldName + "']");
        tester.submit("edit-labels-submit");
        // Haven't changed the selected labels, so all should remain.
        assertions.getLabelAssertions().assertLabels("10000", "labels", expectedLabels);
    }

    public void testCancelOnEditLabelsStandaloneNavigatesToIssue()
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        navigation.issue().editLabels(10000);
        tester.clickLink("cancel");
        assertions.getViewIssueAssertions().assertOnViewIssuePage("HSP-1");
    }

    public void testEditSystemLabelsValidation() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        navigation.issue().editLabels(10000);
        FormParameterUtil parameterUtil = new FormParameterUtil(tester, "edit-labels-form","edit-labels-submit");
        _addInvalidLabel(parameterUtil);
        _assertLabelValidation("labels", parameterUtil);
    }

    public void testEditCustomLabelsValidation() throws Exception
    {
        administration.restoreData("TestLabelsHistory.xml");
        navigation.issue().gotoIssue("HSP-1");
        navigation.issue().editCustomLabels(10000, 10000);
        FormParameterUtil parameterUtil = new FormParameterUtil(tester,"edit-labels-form","edit-labels-submit");
        _addInvalidLabel(parameterUtil);
        _assertLabelValidation("customfield_10000", parameterUtil);
    }

    public void testLabelsJqlLinks()
    {
        administration.restoreData("TestLabels.xml");

        final int issueId = 10000;
        final int customFieldId = 10000;
        final String[] indexToLabel = { "TEST", "aaafirst", "couple", "first", "labels", "of", "test" };

        // View issue.
        for (int i = 0; i < indexToLabel.length; i++)
        {
            navigation.issue().gotoIssue("HSP-1");
            assertJqlLinkForSystemLabel(issueId, i, indexToLabel[i]);
            navigation.issue().gotoIssue("HSP-1");
            assertJqlLinkForLabel(issueId, customFieldId, i, indexToLabel[i]);
        }

        // Issue navigator.
        navigation.issueNavigator().createSearch("");
        for (int i = 0; i < indexToLabel.length; i++)
        {
            navigation.issueNavigator().gotoNavigator();
            assertJqlLinkForSystemLabel(issueId, i, indexToLabel[i]);
            navigation.issueNavigator().gotoNavigator();
            assertJqlLinkForLabel(issueId, customFieldId, i, indexToLabel[i]);
        }
    }

    private void assertJqlLinkForSystemLabel(final int issueId, final int labelIndex, final String labelText)
    {
        assertJqlLinkForLabel(issueId, null, labelIndex, labelText);
    }

    private void assertJqlLinkForLabel(final int issueId, final Integer customFieldId, final int labelIndex, final String labelText)
    {
        final String fieldId = customFieldId == null ? "labels" : "customfield_" + customFieldId;
        String jqlHref = xpath("//ul[@id='" + fieldId + "-" + issueId + "-value']//a").getNodes()[labelIndex]
                .getAttributes()
                .getNamedItem("href")
                .getNodeValue();
        navigation.gotoPage(jqlHref);
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        final String jqlFieldName = customFieldId == null ? "labels" : "cf[" + customFieldId + "]";
        try
        {
            assertions.getIssueNavigatorAssertions().assertAdvancedSearch(tester, jqlFieldName + " = " + labelText);
        }
        catch (Throwable ignore)
        {
            assertions.getIssueNavigatorAssertions().assertAdvancedSearch(tester, jqlFieldName + " = \"" + labelText + "\"");
        }
    }

    private void _addInvalidLabel(final FormParameterUtil formParameterUtil) {
        formParameterUtil.addOptionToHtmlSelect("labels", new String[]{"A B"});
    }

    private void _assertLabelValidation(final String fieldName, final FormParameterUtil formParameterUtil) throws IOException, SAXException
    {
        final String domId = fieldName + "-error";
        XPathLocator locator = new XPathLocator(formParameterUtil.submitForm(), String.format("//*[@id='%s']", domId));
        assertNotNull(String.format("%s-error should exist", domId), locator.getNode());
    }

    private void assertNoHistory() throws Exception
    {
        ChangeHistoryList actualList = ChangeHistoryParser.getChangeHistory(tester);
        assertTrue(actualList.isEmpty());
    }

    private void assertHistoryContains(String changedBy, String fieldName, String oldValue, String newValue) throws Exception
    {
        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet(changedBy).add(fieldName,oldValue,newValue);
        ChangeHistoryList actualList = ChangeHistoryParser.getChangeHistory(tester);
        actualList.assertContainsChangeHistory(expectedList);
    }
}
