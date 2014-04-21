package com.atlassian.jira.web.util;

import com.atlassian.plugin.webresource.WebResourceIntegration;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.EasyMock;

import javax.servlet.http.HttpServletRequest;

public class TestCssLookAndFeelSubstitutionWebResourceTransformer extends ListeningTestCase
{
    private MockApplicationProperties properties;

    @Before
    public void setUp() throws Exception
    {
        properties = new MockApplicationProperties();
        properties.setString(APKeys.JIRA_LF_LOGO_URL, "http://foo/bar.png");
        properties.setString(APKeys.JIRA_LF_TOP_BGCOLOUR, "#112233");
    }

    @After
    public void tearDown() throws Exception
    {
        ExecutingHttpRequest.clear();
    }

    @Test
    public void testSubstitutions() {
        mockRequest("/jira");
        LookAndFeelBean lAndF = LookAndFeelBean.getInstance(properties);

        String input = "@logoUrl @topBackgroundColour @topBackgroundColourNoHash @contextPath/foo.png @nochange";
        assertEquals("http://foo/bar.png #112233 112233 /jira/foo.png @nochange", transform(input, lAndF));
    }

    @Test
    public void testChangesToLookAndFeel() {
        mockRequest("/jira");
        LookAndFeelBean lAndF = LookAndFeelBean.getInstance(properties);

        String input = "@logoUrl @topBackgroundColour @topBackgroundColourNoHash @contextPath/foo.png @nochange";
        assertEquals("http://foo/bar.png #112233 112233 /jira/foo.png @nochange", transform(input, lAndF));

        properties.setString(APKeys.JIRA_LF_TOP_BGCOLOUR, "#445566");
        assertEquals("http://foo/bar.png #445566 445566 /jira/foo.png @nochange", transform(input, lAndF));
    }

    @Test
    public void testContextPath() {
        LookAndFeelBean lAndF = LookAndFeelBean.getInstance(properties);

        String input = "@contextPath/foo.png";

        mockRequest("/jira");
        assertEquals("/jira/foo.png", transform(input, lAndF));

        mockRequest("");
        assertEquals("/foo.png", transform(input, lAndF));
    }

    private void mockRequest(String contextPath)
    {
        final HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getContextPath()).andStubReturn(contextPath);
        EasyMock.replay(request);
        ExecutingHttpRequest.set(request,null);
    }

    private String transform(String input, LookAndFeelBean lAndF)
    {
        DownloadableResource resource = EasyMock.createNiceMock(DownloadableResource.class);
        WebResourceIntegration wri = EasyMock.createNiceMock(WebResourceIntegration.class);
        EasyMock.expect(wri.getSystemBuildNumber()).andReturn("4321").anyTimes();
        EasyMock.expect(wri.getSystemCounter()).andReturn("9876").anyTimes();
        EasyMock.replay(wri);
        CssSubstitutionWebResourceTransformer.CssSubstitutionDownloadableResource r = new CssSubstitutionWebResourceTransformer.CssSubstitutionDownloadableResource(resource, lAndF, wri);
        return r.transform(input);
    }

}
