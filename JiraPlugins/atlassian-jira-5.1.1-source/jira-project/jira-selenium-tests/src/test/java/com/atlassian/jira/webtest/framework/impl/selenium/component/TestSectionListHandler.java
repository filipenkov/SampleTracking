package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.mock.MockCondition;
import com.atlassian.jira.webtest.framework.core.mock.MockPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.util.JQueryExecutorSetup;
import com.atlassian.selenium.mock.MockSeleniumClient;
import com.atlassian.selenium.mock.MockSeleniumConfiguration;
import junit.framework.TestCase;

import java.util.List;

/**
 * Test case for {@link SectionListHandler}.
 *
 * @since v4.3
 */
public class TestSectionListHandler extends TestCase
{

    private SeleniumContext context;
    private MockSeleniumClient client;
    private AbstractSeleniumDropdown<PageObject> dropDown;

    @Override
    protected void setUp() throws Exception
    {
        client = new MockSeleniumClient();
        context = new SeleniumContext(client, new MockSeleniumConfiguration());
        dropDown = createDropDown();
        JQueryExecutorSetup.setUpSelenium(client);
    }

    private AbstractSeleniumDropdown<PageObject> createDropDown()
    {
        return new AbstractSeleniumDropdown<PageObject>("test", new MockPageObject(), context)
        {
            @Override
            protected TimedCondition isOpenableByContext()
            {
                return new MockCondition(false);
            }

            @Override
            public AjsDropdown open()
            {
                return this;
            }
        };
    }

    public void testEmpty()
    {
        client.genericScriptResult("");
        assertTrue(new SectionListHandler<PageObject>(dropDown, context).sections().isEmpty());
        verifyScriptCalls();
    }

    public void testBlank()
    {
        client.genericScriptResult("    ");
        assertTrue(new SectionListHandler<PageObject>(dropDown, context).sections().isEmpty());
        verifyScriptCalls();
    }

    public void testSingleFullObject()
    {
        client.genericScriptResult(" [ { \"id\":\"first\", \"header\":\"some header\"} \t]");
        List<AjsDropdown.Section<PageObject>> result = new SectionListHandler<PageObject>(dropDown, context).sections();
        assertEquals(1, result.size());
        assertEquals("first", result.get(0).id());
        assertTrue(result.get(0).hasHeader());
        assertEquals("some header", result.get(0).header());
        verifyScriptCalls();
    }

    public void testMultipleObjects()
    {
        client.genericScriptResult(" [ { \"id\":\"first\", \"header\":\"someheader\"} \t, {\"id\":\"second\"}]");
        List<AjsDropdown.Section<PageObject>> result = new SectionListHandler<PageObject>(dropDown, context).sections();
        assertEquals(2, result.size());
        assertEquals("first", result.get(0).id());
        assertTrue(result.get(0).hasHeader());
        assertEquals("someheader", result.get(0).header());
        assertEquals("second", result.get(1).id());
        assertFalse(result.get(1).hasHeader());
        verifyScriptCalls();
    }

    private void verifyScriptCalls()
    {
        assertEquals(3, client.executedScripts().size());
        assertTrue("expected value not found in the executed script " + client.executedScripts().get(2),
                client.executedScripts().get(2).contains("div#test.ajs-list"));
    }
}
