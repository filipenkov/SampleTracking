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
 * Test case for {@link com.atlassian.jira.webtest.framework.impl.selenium.component.SectionListHandler}.
 *
 * @since v4.3
 */
public class TestItemListHandler extends TestCase
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
            public AjsDropdown<PageObject> open()
            {
                return this;
            }
        };
    }

    public void testEmpty()
    {
        SeleniumDDSection<PageObject> section = new SeleniumDDSection<PageObject>(dropDown, context, "test-section", "Some header");
        client.genericScriptResult("");
        assertTrue(new ItemListHandler<PageObject>(section, context).items().isEmpty());
        verifyScriptCalls();
    }

    public void testBlank()
    {
        SeleniumDDSection<PageObject> section = new SeleniumDDSection<PageObject>(dropDown, context, "test-section", "Some header");
        client.genericScriptResult("    ");
        assertTrue(new ItemListHandler<PageObject>(section, context).items().isEmpty());
        verifyScriptCalls();
    }

    public void testSingleFullObject()
    {
        SeleniumDDSection<PageObject> section = new SeleniumDDSection<PageObject>(dropDown, context, "test-section", "Some header");
        client.genericScriptResult(" [ { \"name\":\"name one\", \"irrelevant\":\"irrelevant\"} \t]");
        List<AjsDropdown.Item<PageObject>> result = new ItemListHandler<PageObject>(section, context).items();
        assertEquals(1, result.size());
        assertEquals("name one", result.get(0).name());
        assertSame(section, result.get(0).parent());
        verifyScriptCalls();
    }

    public void testMultipleObjects()
    {
        SeleniumDDSection<PageObject> section = new SeleniumDDSection<PageObject>(dropDown, context, "test-section", null);
        client.genericScriptResult(" [ { \"name\":\"somename\", \"irrelevant\":\"something\"} \t, {\"name\":\"blah\"}]");
        List<AjsDropdown.Item<PageObject>> result = new ItemListHandler<PageObject>(section, context).items();
        assertEquals(2, result.size());
        assertEquals("somename", result.get(0).name());
        assertSame(section, result.get(0).parent());
        assertEquals("blah", result.get(1).name());
        assertSame(section, result.get(1).parent());
        verifyScriptCalls();
    }

    public void testInvalidData()
    {
        SeleniumDDSection<PageObject> section = new SeleniumDDSection<PageObject>(dropDown, context, "test-section", "Some header");
        client.genericScriptResult(" [{ \"notreallyname\":\"name one\", \"irrelevant\":\"irrelevant\"}]");
        try
        {
            new ItemListHandler<PageObject>(section, context).items();
            fail("Expected IllegalStateException");
        }
        catch(IllegalStateException expected) {}
        verifyScriptCalls();
    }

    private void verifyScriptCalls()
    {
        assertEquals(3, client.executedScripts().size());
        assertTrue("expected value not found in the executed script " + client.executedScripts().get(2),
                client.executedScripts().get(2).contains("#test-section"));
    }
}
