package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserNameIsEncoded extends FuncTestCase
{
    public void testUserNamesAreHtmlEncoded() throws SAXException
    {
        final String brokenUsername = "&quot;my &lt;input&gt; name";
        administration.restoreData("TestUsernameIsEncoded.xml");
        navigation.issue().gotoIssue("HSP-1");
        text.assertTextPresent(locator.page(), "Is my broken-name really bad?");

        // assert that all four links are properly encoded
        text.assertTextSequence(tester.getDialog().getResponseText(), "Assignee:", brokenUsername);
        text.assertTextSequence(tester.getDialog().getResponseText(), "Reporter:", brokenUsername);
        text.assertTextSequence(tester.getDialog().getResponseText(), "field1:", brokenUsername);
        text.assertTextSequence(tester.getDialog().getResponseText(), "field2:", brokenUsername);

        // verify that all properly encoded usernames are links
        final WebLink[] webLinks = tester.getDialog().getResponse().getLinks();
        int count = 0;
        for (WebLink webLink : webLinks)
        {
            if (webLink.asText().indexOf("\"my <input> name") >= 0)
            {
                count++;
            }
        }
        assertEquals(4, count);

        // check the correct values appear on the edit screen
        tester.clickLink("editIssue");
        text.assertTextPresent(locator.page(), "Edit Issue");
        tester.selectOption("assignee", "\"my <input> name");
        tester.assertFormElementEquals("reporter", "broken");
        tester.assertFormElementEquals("customfield_10000", "broken");
        tester.assertFormElementEquals("customfield_10001", "admin, broken");
    }
}
