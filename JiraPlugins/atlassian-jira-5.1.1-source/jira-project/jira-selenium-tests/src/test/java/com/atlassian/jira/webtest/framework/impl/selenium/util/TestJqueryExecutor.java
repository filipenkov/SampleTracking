package com.atlassian.jira.webtest.framework.impl.selenium.util;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.SeleniumClient;
import com.atlassian.selenium.mock.MockSeleniumConfiguration;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Test case for {@link JqueryExecutor}.
 *
 * @since v4.2
 */
public class TestJqueryExecutor extends TestCase
{

    private SeleniumContext mockContext;
    private SeleniumClient mockClient;
    private JqueryExecutor tested;

    @Override
    protected void setUp() throws Exception
    {
        mockClient = createNiceMock(SeleniumClient.class);
        expect(mockClient.getEval((String) anyObject())).andAnswer(new IAnswer<String>()
        {
            public String answer() throws Throwable
            {
                String script = (String) EasyMock.getCurrentArguments()[0];
                if (isJQueryCheck(script))
                {
                    return "true";
                }
                return script;
            }
        }).anyTimes();
        replay(mockClient);
        mockContext = new SeleniumContext(mockClient, new MockSeleniumConfiguration());
        tested = new JqueryExecutor(mockContext);
        super.setUp();
    }

    private boolean isJQueryCheck(String script)
    {
        return script.equals(JqueryExecutor.JQUERY_CHECK_IS_DEFINED) || script.equals(JqueryExecutor.JQUERY_CHECK_IS_FUNCTION);
    }

    public void testSingleJqueryCall()
    {
        assertEquals("selenium.browserbot.getUserWindow().jQuery('#someid')", tested.execute("jQuery('#someid')").now());
    }

    public void testSingleJqueryCallNow()
    {
        assertEquals("selenium.browserbot.getUserWindow().jQuery('#someid')", tested.executeNow("jQuery('#someid')"));
    }

    public void testMultipleJqueryCalls()
    {
        String result = tested.execute("var $mainDiv = jQuery('div.main'); jQuery('.someclass').each("
            + "new function(elem, idx) { jQuery(elem).appendTo($mainDiv);}); return $mainDiv.attr('id')").now();
        assertEquals("var $mainDiv = selenium.browserbot.getUserWindow().jQuery('div.main'); "
                + "selenium.browserbot.getUserWindow().jQuery('.someclass').each(new function(elem, idx) { "
                + "selenium.browserbot.getUserWindow().jQuery(elem).appendTo($mainDiv);}); return $mainDiv.attr('id')",
                result);
    }

    public void testJqueryCallWithDoubleQuotes()
    {
        assertEquals("selenium.browserbot.getUserWindow().jQuery(\"#someid, #otherid\")",
                tested.execute("jQuery(\"#someid, #otherid\")").now());
    }

    public void testJqueryCallUncapitalized()
    {
        assertEquals("selenium.browserbot.getUserWindow().jQuery('#someid')", tested.execute("jquery('#someid')").now());
    }

    public void testJqueryCallsWithFunctionAndNamespace()
    {
        String result = tested.execute("jQuery.noConflict(); var $mainDiv = jQuery('div.main'); jQuery('.someclass').each("
            + "new function(elem, idx) { jQuery(elem).appendTo($mainDiv);}); return $mainDiv.attr('id')").now();
        assertEquals("selenium.browserbot.getUserWindow().jQuery.noConflict(); "
                + "var $mainDiv = selenium.browserbot.getUserWindow().jQuery('div.main'); "
                + "selenium.browserbot.getUserWindow().jQuery('.someclass').each(new function(elem, idx) { "
                + "selenium.browserbot.getUserWindow().jQuery(elem).appendTo($mainDiv);}); return $mainDiv.attr('id')",
                result);
    }


    // TODO test canExecute/execute/safeExecute with timing
}
