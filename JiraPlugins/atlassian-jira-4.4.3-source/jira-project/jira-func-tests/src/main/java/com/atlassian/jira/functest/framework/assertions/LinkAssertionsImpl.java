package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebLink;
import junit.framework.Assert;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link LinkAssertions} interface.
 *
 * @since v3.13
 */
public class LinkAssertionsImpl extends AbstractFuncTestUtil implements LinkAssertions
{
    public LinkAssertionsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    public void assertLinkLocationEndsWith(final String linkText, final String endsWith)
    {
        final WebLink webLink = getLinkWithText(linkText);
        final String urlString = webLink.getURLString().trim();
        if (!urlString.endsWith(endsWith))
        {
            Assert.fail("Location of link with text '" + linkText + "' expected to end with '" + endsWith + "' but points to '" + urlString + "'.");
        }
    }

    public void assertLinkLocationEndsWith(final WebLink link, final String endsWith)
    {
        final String urlString = link.getURLString().trim();
        if (!urlString.endsWith(endsWith))
        {
            Assert.fail("Location of link expected to end with '" + endsWith + "' but points to '" + urlString + "'.");
        }
    }
    public void assertLinkAtNodeEndsWith(final String xpath, final String endsWith)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        Assert.assertTrue("Node '" + xpath + "' does not exist.", locator.exists());
        final String href = locator.getNode().getAttributes().getNamedItem("href").getNodeValue();
        if (!href.endsWith(endsWith))
        {
            Assert.fail("Location of link expected to end with '" + endsWith + "' but points to '" + href + "'.");
        }
    }

    public void assertLinkAtNodeContains(final String xpath, final String containsUrl)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        Assert.assertTrue("Node '" + xpath + "' does not exist.", locator.exists());
        final String href = locator.getNode().getAttributes().getNamedItem("href").getNodeValue();
        if (!href.contains(containsUrl))
        {
            Assert.fail("Location of link expected to contain '" + containsUrl + "' but points to '" + href + "'.");
        }
    }

    public void assertLinkIdQueryStringContainsJqlQuery(final String linkId, final String expectedJqlQuery)
    {
        assertLinkQueryStringContainsJqlQuery(getLinkWithId(linkId), expectedJqlQuery);
    }

    public void assertLinkTextQueryStringContainsJqlQuery(final String xpath, final String linkText, final String expectedJqlQuery)
    {
        final List<String> hrefs = getHrefsFromLinkNodeWithExactText(xpath, linkText);
        for (String href : hrefs)
        {
            final boolean found = doesHrefContainsJqlQuery(href, expectedJqlQuery);
            if (found)
            {
                return;
            }
        }

        Assert.fail(String.format("Could not find any links with text '%s' in xpath '%s' that contained the jql '%s'", linkText, xpath, expectedJqlQuery));
    }

    public void assertLinkQueryStringContainsJqlQuery(final WebLink link, final String expectedJqlQuery)
    {
        if (StringUtils.isEmpty(link.getURLString()))
        {
            Assert.fail("No URL for link with id [" + link.getID() + "]");
        }

        final boolean foundSubString = doesHrefContainsJqlQuery(link.getURLString(), expectedJqlQuery);
        Assert.assertTrue(String.format("Could not find jql '%s' in URL '%s'", expectedJqlQuery, link.getURLString()), foundSubString);
    }

    private boolean doesHrefContainsJqlQuery(final String href, final String expectedJqlQuery)
    {
        try
        {
            final String urlSubString = "jqlQuery=" + URLEncoder.encode(expectedJqlQuery, "UTF-8");
            return href.indexOf(urlSubString) != -1;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void assertLinkIdLocationEndsWith(final String linkId, final String endsWith)
    {
        final WebLink webLink = getLinkWithId(linkId);
        final String urlString = webLink.getURLString().trim();
        if (!urlString.endsWith(endsWith))
        {
            Assert.fail("Location of link with id '" + linkId + "' expected to end with '" + endsWith + "' but points to '" + urlString + "'.");
        }
    }

    public void assertLinkIdLocationMatchesRegex(final String linkId, final String regex)
    {
        final WebLink webLink = getLinkWithId(linkId);
        final String urlString = webLink.getURLString().trim();

        if (!urlString.matches(regex))
        {
            Assert.fail("Location of link with id '" + linkId + "' expected to match regex '" + regex + "' but points to '" + urlString + "'.");
        }

    }

    public void assertLinkPresentWithExactText(final String xpath, final String text)
    {
        XPathLocator locator = new XPathLocator(tester, xpath + "//a[text()=\"" + text + "\"]");
        Assert.assertTrue("Could not find a single link with the exact text \"" + text + "\" using locator: " + locator, locator.getNodes().length > 0);
    }

    public void assertLinkNotPresentWithExactText(final String xpath, final String text)
    {
        XPathLocator locator = new XPathLocator(tester, xpath + "//a[text()=\"" + text + "\"]");
        Assert.assertTrue("Found a link with the exact text \"" + text + "\" using locator: " + locator, locator.getNodes().length == 0);
    }

    private List<String> getHrefsFromLinkNodeWithExactText(final String xpath, final String text)
    {
        XPathLocator locator = new XPathLocator(tester, xpath + "//a");
        if (locator.getNodes().length == 0)
        {
            throw new IllegalArgumentException("Could not find any links with the text '" + text + "' from xpath '" + xpath + "'.");
        }

        final List<Node> matchedNodes = new ArrayList<Node>();
        for (Node node : locator.getNodes())
        {
            String nodeText = locator.getText(node);
            if (text.equals(nodeText))
            {
                matchedNodes.add(node);
            }
        }

        final List<String> hrefs = new ArrayList<String>();
        for (Node aNode : matchedNodes)
        {
            hrefs.add(aNode.getAttributes().getNamedItem("href").getNodeValue());
        }
        return hrefs;
    }

    private WebLink getLinkWithText(final String text)
    {
        final WebLink webLink;
        try
        {
            final HttpUnitDialog dialog = tester.getDialog();
            if (!dialog.isLinkPresentWithText(text))
            {
                Assert.fail("Link with text'" + text + "' does not exist.");
            }
            webLink = dialog.getResponse().getLinkWith(text);
        }
        catch (SAXException e)
        {
            AssertionError error = new AssertionError("Link with text '" + text + "' does not exist.");
            error.initCause(e);

            throw error;
        }
        return webLink;
    }


    private WebLink getLinkWithId(final String linkId)
    {
        final WebLink webLink;
        try
        {
            final HttpUnitDialog dialog = tester.getDialog();
            if (!dialog.isLinkPresent(linkId))
            {
                Assert.fail("Link with id'" + linkId + "' does not exist.");
            }
            webLink = dialog.getResponse().getLinkWithID(linkId);
        }
        catch (SAXException e)
        {
            AssertionError error = new AssertionError("Link with id '" + linkId + "' does not exist.");
            error.initCause(e);

            throw error;
        }
        return webLink;
    }
}
