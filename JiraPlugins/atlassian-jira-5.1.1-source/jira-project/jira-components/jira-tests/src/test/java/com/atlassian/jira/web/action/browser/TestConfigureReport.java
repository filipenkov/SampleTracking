package com.atlassian.jira.web.action.browser;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.controller.MockController;
import webwork.action.ActionContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
public class TestConfigureReport extends LegacyJiraMockTestCase
{

    MockController mockController;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockController = new MockController();
    }

    public void testGetQueryString()
    {
        final Map<String, String[]> parameters = new LinkedHashMap<String, String[]>();

        parameters.put("a", ww("b"));
        parameters.put("encode", ww("yes sir"));
        parameters.put("funny", ww("ch(ara=cter&s"));
        parameters.put("done", ww("ok"));
        ActionContext.setParameters(parameters);

        final ConfigureReport report = mockController.instantiate(ConfigureReport.class);
        assertEquals("a=b&encode=yes+sir&funny=ch%28ara%3Dcter%26s&done=ok", report.getQueryString());

        parameters.clear();
        parameters.put("one", ww("value"));
        ActionContext.setParameters(parameters);

        assertEquals("one=value", report.getQueryString());


    }

    private String[] ww(String val)
    {
        return new String[] { val };
    }
}
