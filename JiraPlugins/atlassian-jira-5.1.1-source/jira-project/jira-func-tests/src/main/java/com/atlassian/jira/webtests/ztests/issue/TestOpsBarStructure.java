package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestOpsBarStructure extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestOpsBar.xml");
    }

    public void testWorkflows()
    {
        // may be invoked with reference plugin - this adds a reference Action
        // there will therefore be an extra transition

        navigation.issue().viewIssue("HSP-1");
        assertExtraTransitionsExist();

        navigation.issue().viewIssue("HSP-2");
        if (referencePluginEnabled())
        {
            assertOnlyReferenceTransitionExists();
        }
        else
        {
            assertOnlyStandardTransitions();
        }

        navigation.issue().viewIssue("HSP-3");
        if (referencePluginEnabled())
        {
            assertOnlyReferenceTransitionExists();
        }
        else
        {
            assertOnlyStandardTransitions();
        }

        navigation.issue().viewIssue("HSP-4");
        if (referencePluginEnabled())
        {
            assertOnlyReferenceTransitionExists();
        }
        else
        {
           assertNoTransitions();
        }
     }

    private boolean referencePluginEnabled()
    {
        return new XPathLocator(tester, "//a[@id='reference-operation']").exists();
    }

    private void assertNoTransitions()
    {
        assertions.assertNodeDoesNotExist("//span[@id='opsbar-transitions_more']");
        assertions.assertNodeDoesNotExist("//a[@id='opsbar-transitions_more']");
    }

    private void assertOnlyStandardTransitions()
    {
        assertions.assertNodeExists("//span[@id='opsbar-transitions_more']");
        assertions.assertNodeDoesNotExist("//a[@id='opsbar-transitions_more']");
    }

    private void assertExtraTransitionsExist()
    {
        assertions.assertNodeDoesNotExist("//span[@id='opsbar-transitions_more']");
        assertions.assertNodeExists("//a[@id='opsbar-transitions_more']");
    }

    private void assertOnlyReferenceTransitionExists()
    {
         assertions.assertNodeExists(new CssLocator(tester, "ul.aui-last > li > #reference-operation"));
    }

    public void testConjoined()
    {
        navigation.issue().viewIssue("HSP-5");
        String operationsGroup = "//div[@class='ops-menus aui-toolbar']/div/ul[2]";
        assertions.assertNodeHasText(operationsGroup + "/li[1]", "Assign");
        assertions.assertNodeHasText(operationsGroup + "/li[2]", "Assign To Me");
        assertions.assertNodeHasText(operationsGroup + "/li[3]", "Comment");
        assertions.assertNodeDoesNotHaveText(operationsGroup + "/li[3]", "More Actions");
        assertions.assertNodeByIdExists("opsbar-operations_more");

        navigation.issue().viewIssue("HSP-2");
        assertions.assertNodeHasText(operationsGroup + "/li[1]", "Assign");
        assertions.assertNodeHasText(operationsGroup + "/li[2]", "Comment");
        assertions.assertNodeByIdExists("opsbar-operations_more");

    }

    public void testLoginButton()
    {
        navigation.logout();
        navigation.issue().viewIssue("ANONED-1");

        IdLocator locator = new IdLocator(tester, "ops-login-lnk");
        assertEquals(0, locator.getNodes().length);

        navigation.issue().viewIssue("ANON-1");

        locator = new IdLocator(tester, "ops-login-lnk");
        assertEquals(1, locator.getNodes().length);

        tester.clickLink("ops-login-lnk");

        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();

        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "Anon viewable issue");

        locator = new IdLocator(tester, "ops-login-lnk");
        assertEquals(0, locator.getNodes().length);
    }

    public void testOpsbarTransitionOrder()
    {
        administration.restoreData("TestOpsbarTransitionOrder.xml");

        navigation.issue().viewIssue("HSP-1");
        final String operationsGroup = "//div[@class='ops-menus aui-toolbar']/div/ul[3]";
        final String[] originalOrder = {"Start Progress", "Resolve Issue", "Close Issue"};
        final String[] newOrder =  {"Close Issue", "Resolve Issue", "Start Progress"};
        assertTransitionOrder(operationsGroup, originalOrder);

        //now change the order!
        //go to workflows admin section
        //create a draft workflow
        administration.workflows().goTo().createDraft("Copy of jira");

        //change the order of start progress to 100.
        tester.clickLinkWithText("Start Progress");
        tester.clickLinkWithText("properties of this transition");
        tester.clickLink("del_meta_opsbar-sequence");
        tester.setFormElement("attributeKey", "opsbar-sequence");
        tester.setFormElement("attributeValue", "100");
        tester.submit("Add");
        text.assertTextSequence(new TableLocator(tester, "metas_table"), "opsbar-sequence", "100");

        //change the order of Close issue to 20
        administration.workflows().goTo().workflowSteps("Copy of jira");
        tester.clickLinkWithText("Close Issue");
        tester.clickLinkWithText("properties of this transition");
        tester.clickLink("del_meta_opsbar-sequence");
        tester.setFormElement("attributeKey", "opsbar-sequence");
        tester.setFormElement("attributeValue", "20");
        tester.submit("Add");
        text.assertTextSequence(new TableLocator(tester, "metas_table"), "opsbar-sequence", "20");

        //now publish the edited workflow
        administration.workflows().goTo().publishDraft("Copy of jira").publish();

        //go back to the issue and assert the order has changed!
        navigation.issue().viewIssue("HSP-1");
        assertTransitionOrder(operationsGroup, newOrder);

    }

    private void assertTransitionOrder(final String operationsGroup, final String[] expectedText)
    {
        assertions.getTextAssertions().assertTextSequence(new XPathLocator(tester, operationsGroup), expectedText);
    }
}