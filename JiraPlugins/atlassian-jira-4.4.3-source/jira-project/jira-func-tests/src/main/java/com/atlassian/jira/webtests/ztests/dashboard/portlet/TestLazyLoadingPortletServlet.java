package com.atlassian.jira.webtests.ztests.dashboard.portlet;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * JRA-13965 - Test the response of the LazyLoadingPortletServlet in different situations.
 *
 * @since v3.12.4
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS, Category.PORTLETS })
public class TestLazyLoadingPortletServlet extends FuncTestCase
{
    public void testBadPortletId()
    {
        // Blank Instance has the Saved Filters portlet as id 10012
        administration.restoreBlankInstance();

        // try a non-existant id
        String id = "99999";
        tester.gotoPage("/lazyLoader?portletId=" + id);
        Locator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] {"WARNING", "The gadget with id ", id, "no longer exists or is invalid."});

        // try non-numeric portlet id
        id = "1234ABCDEF";
        tester.gotoPage("/lazyLoader?portletId=" + id);
        locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] {"WARNING", "The gadget with id ", id, "no longer exists or is invalid."});

        // try XSS in portlet id
        id = "<script>alert('Owned')</script>";
        String idEncoded = "&lt;script&gt;alert(&#39;Owned&#39;)&lt;/script&gt;";
        tester.gotoPage("/lazyLoader?portletId=" + id);
        locator = new WebPageLocator(tester);
        text.assertTextNotPresent(locator.getHTML(), id);
        text.assertTextSequence(locator.getHTML(), new String[] {"WARNING", "The gadget with id ", idEncoded, "no longer exists or is invalid."});
    }
}
