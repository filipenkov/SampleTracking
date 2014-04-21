package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import net.sourceforge.jwebunit.WebTester;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestSearchXmlView extends FuncTestCase
{
    public void testFilterAllIssues() throws IOException, DocumentException
    {
        administration.restoreData("TestXMLIssueView.xml");
        long filterId = createFilterForAllIssues();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=200");
        Document doc = getDocument(tester);
        XPath xpath = DocumentHelper.createXPath("//item/title");
        
        List nodes = xpath.selectNodes(doc);
        assertEquals(3, nodes.size());
        assertTrue(((Element) nodes.get(0)).getText().startsWith("[MKY-1]"));
        assertTrue(((Element) nodes.get(1)).getText().startsWith("[HSP-2]"));
        assertTrue(((Element) nodes.get(2)).getText().startsWith("[HSP-1]"));
    }
    
    public void testFilterAllIssuesWithCustomSort() throws IOException, DocumentException
    {
        administration.restoreData("TestXMLIssueView.xml");
        long filterId = createFilterForAllIssues();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=200&sorter/field=issuekey&sorter/order=ASC");
        Document doc = getDocument(tester);
        XPath xpath = DocumentHelper.createXPath("//item/title");
        
        List nodes = xpath.selectNodes(doc);
        assertEquals(3, nodes.size());
        assertTrue(((Element) nodes.get(0)).getText().startsWith("[HSP-1]"));
        assertTrue(((Element) nodes.get(1)).getText().startsWith("[HSP-2]"));
        assertTrue(((Element) nodes.get(2)).getText().startsWith("[MKY-1]"));
    }
    
    public void testFilterAllIssuesWithCustomSortAndPaging() throws IOException, DocumentException
    {
        administration.restoreData("TestXMLIssueView.xml");
        long filterId = createFilterForAllIssues();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=2&sorter/field=issuekey&sorter/order=ASC&pager/start=1");
        Document doc = getDocument(tester);
        XPath xpath = DocumentHelper.createXPath("//item/title");
        
        List nodes = xpath.selectNodes(doc);
        assertEquals(2, nodes.size());
        assertTrue(((Element) nodes.get(0)).getText().startsWith("[HSP-2]"));
        assertTrue(((Element) nodes.get(1)).getText().startsWith("[MKY-1]"));
    }

    private long createFilterForAllIssues()
    {
        IssueNavigatorNavigation issueNav = navigation.issueNavigator();
        issueNav.createSearch(new NavigatorSearch(Collections.EMPTY_LIST));
        return issueNav.saveCurrentAsNewFilter("All Issues", "Shows all issues", false, Collections.singleton(new GroupTestSharingPermission("jira-users")));
    }

    private Document getDocument(WebTester tester) throws IOException, DocumentException
    {
        SAXReader reader = new SAXReader();
        return reader.read(tester.getDialog().getResponse().getInputStream());
    }
}
