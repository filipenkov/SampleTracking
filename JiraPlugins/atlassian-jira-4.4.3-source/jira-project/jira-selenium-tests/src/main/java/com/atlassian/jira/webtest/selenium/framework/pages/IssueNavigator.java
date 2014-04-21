package com.atlassian.jira.webtest.selenium.framework.pages;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.components.IssueNavResults;
import com.thoughtworks.selenium.SeleniumException;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Representation of the Issue Navigator in the Selenium World.
 *
 * @since 4.2
 * @deprecated use {@link com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator} instead
 */
@Deprecated
public class IssueNavigator extends AbstractGlobalPage<IssueNavigator> implements Page
{
    private static final String DETECTOR = "jquery=h1.item-summary:contains(Issue Navigator)";
    private static final String GLOBAL_LINK = "find_link";

    private final IssueNavResults results;

    public IssueNavigator(SeleniumContext context)
    {
        super(IssueNavigator.class, context);
        this.results = new IssueNavResults(context);
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected String detector()
    {
        return DETECTOR;
    }

    @Override
    protected String linkLocator()
    {
        return GLOBAL_LINK;
    }

    /* ---------------------------------------------- ACTIONS ------------------------------------------------------- */

    /**
     * Switch to simple search mode.
     *
     * @return this issue navigator instance
     */
    public IssueNavigator toSimpleMode()
    {
        goTo();
        if(!isSimpleMode())
        {
            clickAndWaitForPageLoad("switchnavtype");
        }
        return this;
    }

    /**
     * Switch to advanced (JQL) search mode.
     *
     * @return this issue navigator instance
     */
    public IssueNavigator toJqlMode()
    {
        goTo();
        if(!isJqlMode())
        {
            clickAndWaitForPageLoad("switchnavtype");
        }
        return this;
    }

    /**
     * Find all issues.
     *
     * @return this issue navigator instance
     */
    public IssueNavigator findAll()
    {
        return runJql("");
    }


    public IssueNavigator runJql(String jql)
    {
        toJqlMode();
        client.type("jqlQuery", jql);
        return submitJql();
    }

    public IssueNavigator submitJql()
    {
        clickAndWaitForPageLoad("jqlrunquery");
        return this;
    }

    public IssueNavigator toPage(int pageNumber)
    {
        client.click("page_" + pageNumber, true);
        return this;
    }

    public IssueNavigator toNextPage()
    {
        client.click("css=.icon-next", true);
        return this;
    }

    public IssueNavigator toPreviousPage()
    {
        client.click("css=.icon-previous", true);
        return this;
    }


    public IssueNavigator sortByColumn(final int i)
    {
        client.click("jquery=#issuetable th:nth(" + i + ")", true);
        return this;
    }


    // TODO JQL search / simple search

    private void clickAndWaitForPageLoad(final String linkId)
    {
        client.click(linkId, true);
    }

    /* -------------------------------------------------- QUERIES --------------------------------------------------- */

    /**
     * Checks if simple  search mode is on.
     *
     * @return <code>true</code>, if simple search mode is on, <code>false<code> otherwise (i.e. JQL mode)
     */
    public boolean isSimpleMode()
    {
        return isAt() && !isJqlMode();
    }

    /**
     * Checks if advanced (JQL) search mode is on.
     *
     * @return <code>true</code>, if JQL search mode is on, <code>false<code> otherwise (i.e. simple mode)
     */
    public boolean isJqlMode()
    {
        return isAt() && client.isTextPresent("Switch to simple searching");
    }

    /* ------------------------------------------------ PAGE ELEMENTS ----------------------------------------------- */

    public IssueNavResults results()
    {
        return results;
    }




    /* ------------------------------------------------- OLD STUFF -------------------------------------------------- */

    /**
     * Whether at initial page render time the JQL textarea had focus.
     *
     * <p>This is a proxy to whether it does have focus, since some JavaScript must run to actually
     * tell the browser to give focus to the JQL textarea upon seeing the 'focused' class.</p>
     *
     * @return <code>true</code>, if JQL text area had focus on page load
     * @deprecated this belongs to one specific test and shouldnt be here, expose locator for JQL textarea and let
     * the test handle the rest 
     */
    @Deprecated
    public boolean isJqlFocusedOnPageLoad()
    {
        if (!client.isElementPresent("css=#jqltext"))
        {
            return false;
        }
        else
        {
            String classAttribute;
            try
            {
                classAttribute = client.getAttribute("css=#jqltext @class");
            }
            catch (SeleniumException e)
            {
                // Selenium throws an exception when the attribute is not present.
                return false;
            }
            return new HashSet<String>(Arrays.asList(classAttribute.split("\\s"))).contains("focused");
        }
    }

    public void viewSummaryTab()
    {
        client.click("viewfilter", true);
    }

    public void viewEditTab()
    {
        client.click("editfilter", true);
    }

    public void assertNumberOfIssues(final Integer from, final Integer to, final Integer total)
    {
        assertThat.elementContainsText("//div[@class='results-count']", from.toString() + " to " + to.toString());
        assertThat.elementContainsText("id=results-count-total", total.toString());
    }

}
