package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.ISSUES })
public class TestIssueNavigatorEncoding extends FuncTestCase
{
    private static final String PROJECT_14_INCH_MONITORS = "14&quot; monitors";
    private static final String PROJECT_SCRIPT_HACK = "&lt;script&gt;alert(&quot;hack&quot;)&lt;/script&gt;";

    public void testProjectNameIsEncoded() throws Exception
    {
        administration.restoreData("TestIssueNavigatorEncoding.xml");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // go to Issue Navigator
        navigation.issueNavigator().gotoNavigator();

        // verify the names are encoded in the select box
        tester.assertTextPresent(">" + PROJECT_14_INCH_MONITORS + "</option>");
        tester.assertTextPresent(">" + PROJECT_SCRIPT_HACK + "</option>");
    }

    public void testHtmlCustomFieldValuesNotDoubleEncoded()
    {
        // data contains a filter called "Fields with HTML Values" that has the following criteria:
        // My Multi Checkbox: <b>My Option</b>
        // My Multi Select: <b>My Option 2</b>
        // My Radio Buttons: <b>My Option 3</b>
        // My Select List: <b>My Option</b>
        // since the values already contain HTML, the view should not HTML encode them
        administration.restoreData("TestXssCustomFields.xml");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10010");
        tester.assertTextPresent("Fields with HTML Values");

        Locator pageLocator = new WebPageLocator(tester);

        // Only search in the body, as the escaped JQL may appear in the filter-jql meta tag.
        String html = pageLocator.getHTML();
        int bodyIndex = html.indexOf("<body ");
        String body = html.substring(bodyIndex);
        text.assertTextSequence(body, new String[] { "My Multi Checkbox", "<b>My Option</b>",
                "My Multi Select", "<b>My Option 2</b>",
                "My Radio Buttons", "<b>My Option 3</b>",
                "My Select List", "<b>My Option</b>" });
        text.assertTextNotPresent(body, "&lt;b&gt;My Option&lt;/b&gt;");
        text.assertTextNotPresent(body, "&lt;b&gt;My Option 2&lt;/b&gt;");
        text.assertTextNotPresent(body, "&lt;b&gt;My Option 3&lt;/b&gt;");
    }

    public void testCustomFieldValuesThatShouldBeEncoded()
    {
        // data contains a filter called "Fields that should be encoded" that has the following criteria:
        // My Free Text: <xxx>freetext</xxx>
        // My Group: <xxx>delta</xxx>
        // My Multi Group: <xxx>gamma</xxx>
        // My Multi User: <xxx>alpha</xxx>
        // My Text: <xxx>smalltext</xxx>
        // My User: <xxx>beta</xxx>
        // these values come largely from user input, and so should be encoded by the view when displayed
        // e.g. currently <xxx>alpha</xxx> is a valid user name
        administration.restoreData("TestXssCustomFields.xml");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        tester.gotoPage("secure/IssueNavigator.jspa?mode=hide&requestId=10020");
        tester.assertTextPresent("Fields that should be encoded");

        Locator pageLocator = new WebPageLocator(tester);

        text.assertTextSequence(pageLocator.getHTML(), new String[] { "My Free Text", "&lt;xxx&gt;freetext&lt;/xxx&gt;",
                "My Group", "&lt;xxx&gt;delta&lt;/xxx&gt;",
                "My Multi Group", "&lt;xxx&gt;gamma&lt;/xxx&gt;",
                "My Multi User", "&lt;xxx&gt;alpha&lt;/xxx&gt;",
                "My Text", "&lt;xxx&gt;smalltext&lt;/xxx&gt;",
                "My User", "&lt;xxx&gt;beta&lt;/xxx&gt;" });
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>freetext</xxx>");
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>delta</xxx>");
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>gamma</xxx>");
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>alpha</xxx>");
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>smalltext</xxx>");
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>beta</xxx>");

        // check the issue navigator output - need to refresh search request
        tester.setFormElement("searchString", " ");
        tester.submit();
        pageLocator = new WebPageLocator(tester);
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>delta</xxx>");
        text.assertTextPresent(pageLocator.getHTML(), "&lt;xxx&gt;delta&lt;/xxx&gt;");

        // check the view issue page
        tester.clickLinkWithText("HSP-1");
        pageLocator = new WebPageLocator(tester);
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>delta</xxx>");
        text.assertTextPresent(pageLocator.getHTML(), "&lt;xxx&gt;delta&lt;/xxx&gt;");
    }

    //JRADEV-1042 - custom field labels should be encoded properly
    public void testCustomFieldLabelsEncoded()
    {
        administration.restoreData("TestIssueNavigatorCustomFieldLabelXss.xml");

        //test filter summary and header row
        navigation.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10030");
        assertCustomFieldLabelEncoded(new WebPageLocator(tester));

        //custom field label in the navigator on left and header row
        navigation.issueNavigator().displayAllIssues();
        assertCustomFieldLabelEncoded(new WebPageLocator(tester));

        //configure columns header row
        navigation.gotoPage("/secure/ViewUserIssueColumns!default.jspa");
        assertCustomFieldLabelEncoded(new WebPageLocator(tester));
    }

    private void assertCustomFieldLabelEncoded(final Locator locator)
    {
        text.assertTextPresent(locator.getHTML(), "&quot;&gt;&lt;iframe src=&quot;http://www.google.com&quot;&gt;&lt;/iframe&gt;&lt;a href=&quot;#&quot; rel=&quot;");
        text.assertTextNotPresent(locator.getHTML(), "\"><iframe src=\"http://www.google.com\"></iframe><a href=\"#\" rel=\"");
    }
}
