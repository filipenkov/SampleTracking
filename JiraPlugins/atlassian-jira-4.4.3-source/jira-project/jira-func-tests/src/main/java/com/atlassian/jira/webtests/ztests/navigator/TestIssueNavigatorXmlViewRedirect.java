package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractTestIssueNavigatorView;
import com.meterware.httpunit.WebResponse;
import org.apache.commons.collections.map.ListOrderedMap;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tests the redirect configuration for old URLs on XML views. These used to be
 * known as RSS views way back in the olden days before we knew better.
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.ISSUES })
public class TestIssueNavigatorXmlViewRedirect extends AbstractTestIssueNavigatorView
{
    private static final String XPATH_ITEM_COUNT = "count(/rss/channel/item)";

    public TestIssueNavigatorXmlViewRedirect(String name)
    {
        super(name);
    }

    // Note that the URL is rather badly formatted. But that worked in 3.6 so we need to test just that.
    private static String URL_3_6_5 = "/secure/IssueNavigator.jspa?view=rss&&type=1&pid=10000&resolution=-1&sorter/field=issuekey&sorter/order=DESC&tempMax=25&reset=true&decorator=none";

    private static String URL_VIEW_PARAM_LAST = "/secure/IssueNavigator.jspa?type=1&pid=10000&resolution=-1&sorter/field=issuekey&sorter/order=DESC&tempMax=25&reset=true&decorator=none&view=rss";

    private static String URL_VIEW_PARAM_MIDDLE = "/secure/IssueNavigator.jspa?reset=true&decorator=none&view=rss&type=1&pid=10000&resolution=-1&sorter/field=issuekey&sorter/order=DESC&tempMax=25";

    private static String URL_RESET_PARAM = "/secure/IssueNavigator.jspa?view=rss&reset=true";
    private static String URL_NO_PARAMS = "/secure/IssueNavigator.jspa?view=rss";

    private static final String URL_3_6_5_DATE_CREATED = "/secure/IssueNavigator.jspa?view=rss&&type=1&pid=10000&resolution=-1&sorter/field=datecreated&sorter/order=DESC&tempMax=25&reset=true&decorator=none";
    private static final String URL_3_6_5_DATE_UPDATED = "/secure/IssueNavigator.jspa?view=rss&&type=1&pid=10000&resolution=-1&sorter/field=lastupdated&sorter/order=DESC&tempMax=25&reset=true&decorator=none";

    private static final class UrlGenerator
    {
        private static final Map VIEWS = new HashMap();

        static
        {
            VIEWS.put("searchrequest-printable", "html");
            VIEWS.put("searchrequest-fullcontent", "html");
            VIEWS.put("searchrequest-xml", "xml");
            VIEWS.put("searchrequest-rss", "xml");
            VIEWS.put("searchrequest-comments-rss", "xml");
            VIEWS.put("searchrequest-word", "doc");
            VIEWS.put("searchrequest-excel-all-fields", "xls");
            VIEWS.put("searchrequest-excel-current-fields", "xls");
        }

        public static List getDateCreatedUrls()
        {
            return getUrls("datecreated");
        }

        public static List getLastUpdatedUrls()
        {
            return getUrls("lastupdated");
        }

        /**
         * Generates a list of URLs for various Issue Navigator views
         *
         * @param sorterField sort field
         * @return a list of URLs, never null
         */
        private static List getUrls(final String sorterField)
        {
            List urls = new ArrayList(VIEWS.size());
            for (Iterator i = VIEWS.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry entry = (Map.Entry) i.next();
                final Object view = entry.getKey();
                final Object fileExt = entry.getValue();
                StringBuffer sb = new StringBuffer();
                sb.append("/sr/jira.issueviews:").append(view).append("/temp/SearchRequest.").append(fileExt);
                sb.append("?sorter/field=").append(sorterField);
                sb.append("&amp;sorter/order=DESC&amp;tempMax=10");
                urls.add(sb.toString());
            }
            return urls;
        }
    }

    public void testRedirectFrom365ForDateCreated() throws Exception
    {
        restoreData("TestIssueNavigatorXmlViewRedirect.xml");
        final String oldParam = "sorter/field=datecreated";
        final String newParam = "sorter/field=created";
        assertRedirection(URL_3_6_5_DATE_CREATED, oldParam, newParam);
        final List originalUrls = TestIssueNavigatorXmlViewRedirect.UrlGenerator.getDateCreatedUrls();
        assertRedirection(originalUrls, oldParam, newParam);
    }

    public void testRedirectFrom365ForLastUpdated() throws Exception
    {
        restoreData("TestIssueNavigatorXmlViewRedirect.xml");
        final String oldParam = "sorter/field=lastupdated";
        final String newParam = "sorter/field=updated";
        assertRedirection(URL_3_6_5_DATE_UPDATED, oldParam, newParam);
        assertRedirection(UrlGenerator.getLastUpdatedUrls(), oldParam, newParam);
    }

    private void assertRedirection(List originalUrls, String oldParam, String newParam)
    {
        for (Iterator i = originalUrls.iterator(); i.hasNext();)
        {
            String originalUrl = (String) i.next();
            assertRedirection(originalUrl, oldParam, newParam);
        }
    }

    private void assertRedirection(String originalUrl, String oldParam, String newParam)
    {
        log("Checking for redirection: " + originalUrl);
        gotoPage(originalUrl);

        assertTrue("Original URL does not contain old parameter '" + oldParam + "': " + originalUrl,
                originalUrl.indexOf(oldParam) >= 0);
        assertFalse("Original URL contains new parameter '" + newParam + "': " + originalUrl,
                originalUrl.indexOf(newParam) >= 0);

        // Ensure that we actually got redirected
        final String actualUrl = getDialog().getResponse().getURL().toString();
        assertTrue("Redirected URL does not contain expected parameter '" + newParam + "': " + actualUrl,
                actualUrl.indexOf(newParam) >= 0);
        assertFalse("Redirected URL contains old parameter '" + oldParam + "': " + actualUrl,
                actualUrl.indexOf(oldParam) >= 0);
    }

    /**
     * Checks the redirect works with the exact URL given from version 3.6.5
     * which has the view=rss parameter first in the query part.
     *
     * @throws Exception in case of error
     */
    public void testRedirectFromVersion365() throws Exception
    {
        // We need to preserve insertion order!!!
        Map xpaths = new ListOrderedMap();
        xpaths.put(XPATH_ITEM_COUNT, "2");
        xpaths.put("/rss/channel/item[1]/key", "PX-3");
        xpaths.put("/rss/channel/item[2]/key", "PX-2");

        testRedirect(URL_3_6_5, xpaths);
    }

    public void testRedirectViewParamMiddle() throws Exception
    {
        Map xpaths = new ListOrderedMap();
        xpaths.put(XPATH_ITEM_COUNT, "2");
        xpaths.put("/rss/channel/item[1]/key", "PX-3");
        xpaths.put("/rss/channel/item[2]/key", "PX-2");

        testRedirect(URL_VIEW_PARAM_MIDDLE, xpaths);
    }

    public void testRedirectViewParamLast() throws Exception
    {
        Map xpaths = new ListOrderedMap();
        xpaths.put(XPATH_ITEM_COUNT, "2");
        xpaths.put("/rss/channel/item[1]/key", "PX-3");
        xpaths.put("/rss/channel/item[2]/key", "PX-2");

        testRedirect(URL_VIEW_PARAM_LAST, xpaths);
    }

    public void testRedirectResetParams() throws Exception
    {
        _testRedirectAllIssues(URL_RESET_PARAM);
    }

    public void testRedirectNoParams() throws Exception
    {
        _testRedirectAllIssues(URL_NO_PARAMS);
    }

    private void _testRedirectAllIssues(final String url) throws Exception
    {
        Map xpaths = new ListOrderedMap();
        xpaths.put(XPATH_ITEM_COUNT, "6");
        xpaths.put("/rss/channel/item[1]/key", "PX-6");
        xpaths.put("/rss/channel/item[2]/key", "PX-5");
        xpaths.put("/rss/channel/item[3]/key", "PX-4");
        xpaths.put("/rss/channel/item[4]/key", "PX-3");
        xpaths.put("/rss/channel/item[5]/key", "PX-2");
        xpaths.put("/rss/channel/item[6]/key", "PX-1");

        testRedirect(url, xpaths);
    }

    /**
     * Evaluates a series of xpath values on the XML retrieved from the given URL.
     *
     * @param url    the URL from which the XML is retrieved to test.
     * @param xpaths a Map&lt;String,String&gt; of xpath to expected value tests for the resulting XML
     * @throws Exception in case of error
     */
    private void testRedirect(String url, Map xpaths) throws Exception
    {
        restoreData("TestIssueNavigatorXmlViewRedirect.xml");
        gotoPage(url);

        final WebResponse response = getDialog().getResponse();

        // Ensure that we actually got redirected
        assertFalse(url.equals(response.getURL().toString()));

        String responseText = response.getText();
        Document doc = XMLUnit.buildControlDocument(responseText);

        for (Iterator iterator = xpaths.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry e = (Map.Entry) iterator.next();
            String xpath = (String) e.getKey();
            String expectedValue = (String) e.getValue();
            XMLAssert.assertXpathEvaluatesTo(expectedValue, xpath, doc);
        }
    }
}
