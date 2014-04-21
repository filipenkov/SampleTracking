package com.atlassian.jira.web.servlet;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.easymock.MockControl;
import org.apache.velocity.exception.VelocityException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.parser.AbstractPage;
import com.opensymphony.module.sitemesh.parser.AbstractHTMLPage;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockHttpServletRequest;

import javax.servlet.ServletException;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

public class TestVelocityDecoratorServlet extends ListeningTestCase
{
    private MockControl mockApplicationPropertiesControl;
    private ApplicationProperties mockApplicationProperties;
    private MockControl mockJiraAuthenticationContextControl;
    private JiraAuthenticationContext mockJiraAuthenticationContext;
    private MockControl mockVelocityManagerControl;
    private VelocityManager mockVelocityManager;
    private VelocityDecoratorServlet velocityDecoratorServlet;
    private Map velocityContext;

    @Before
    public void setUp() throws Exception
    {
        mockApplicationPropertiesControl = MockControl.createNiceControl(ApplicationProperties.class);
        mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockJiraAuthenticationContextControl = MockControl.createNiceControl(JiraAuthenticationContext.class);
        mockJiraAuthenticationContext = (JiraAuthenticationContext) mockJiraAuthenticationContextControl.getMock();
        mockVelocityManagerControl = MockControl.createControl(VelocityManager.class);
        mockVelocityManager = (VelocityManager) mockVelocityManagerControl.getMock();

        velocityContext = new HashMap();

        velocityDecoratorServlet = new VelocityDecoratorServlet()
        {
            ApplicationProperties getApplicationProperties()
            {
                return mockApplicationProperties;
            }

            VelocityManager getVelocityManager()
            {
                return mockVelocityManager;
            }

            JiraAuthenticationContext getJiraAuthenticationContext()
            {
                return mockJiraAuthenticationContext;
            }

            Map getDefaultVelocityParams()
            {
                return velocityContext;
            }
        };
    }

    private void replay()
    {
        mockApplicationPropertiesControl.replay();
        mockJiraAuthenticationContextControl.replay();
        mockVelocityManagerControl.replay();
    }

    private void verify()
    {
        mockApplicationPropertiesControl.verify();
        mockJiraAuthenticationContextControl.verify();
        mockVelocityManagerControl.verify();
    }

    @Test
    public void testServiceDispatched() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityManager.getEncodedBody("", "path", "context", null, velocityContext);
        mockVelocityManagerControl.setReturnValue("rendered body");

        replay();
        velocityDecoratorServlet.service(createRequest(createPage("title", "head", "body"), "path", true), response);

        assertEquals("rendered body", response.getOutputStreamContents());

        assertEquals("title", velocityContext.get("title"));
        assertEquals("head", velocityContext.get("head"));
        assertEquals("body", velocityContext.get("body").toString());

        verify();
    }

    @Test
    public void testServiceNotDispatched() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityManager.getEncodedBody("", "path", "context", null, velocityContext);
        mockVelocityManagerControl.setReturnValue("rendered body");

        replay();
        velocityDecoratorServlet.service(createRequest(createPage("title", "head", "body"), "path", false), response);

        assertEquals("rendered body", response.getOutputStreamContents());

        assertEquals("title", velocityContext.get("title"));
        assertEquals("head", velocityContext.get("head"));
        assertEquals("body", velocityContext.get("body").toString());

        verify();
    }

    @Test
    public void testServiceNormalPage() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityManager.getEncodedBody("", "path", "context", null, velocityContext);
        mockVelocityManagerControl.setReturnValue("rendered body");

        replay();
        velocityDecoratorServlet.service(createRequest(createPage("title", null, "body"), "path", true), response);

        assertEquals("rendered body", response.getOutputStreamContents());

        assertEquals("title", velocityContext.get("title"));
        assertEquals("body", velocityContext.get("body").toString());

        verify();
    }

    @Test
    public void testServiceNoPage() throws Exception
    {
        MockHttpServletResponse response = createResponse();

        replay();
        try
        {
            velocityDecoratorServlet.service(createRequest(null, "path", true), response);
            fail("Exception not thrown");
        }
        catch (ServletException se)
        {
            // Pass
        }

        verify();
    }

    @Test
    public void testServiceVelocityError() throws Exception
    {
        MockHttpServletResponse response = createResponse();
        mockVelocityManager.getEncodedBody("", "path", "context", null, velocityContext);
        mockVelocityManagerControl.setThrowable(new VelocityException("Hello"));
        replay();
        velocityDecoratorServlet.service(createRequest(createPage("title", "head", "body"), "path", true), response);

        // Remember, the contents has new lines in it so we need dotall mode
        assertTrue(response.getOutputStreamContents().matches("(?s)Exception rendering velocity file path.*Hello.*"));

        verify();

    }

    private Page createPage(final String title, final String head, final String body) throws Exception
    {
        AbstractPage page;
        if (head != null)
        {
            page = new AbstractHTMLPage()
            {
                public void writeBody(Writer writer) throws IOException
                {
                    writer.write(body);
                }

                public void writeHead(Writer writer) throws IOException
                {
                    writer.write(head);
                }

                public String getHead()
                {
                    return head;
                }

                public String getTitle()
                {
                    return title;
                }
            };
        }
        else
        {
            page = new AbstractPage()
            {
                public void writeBody(Writer writer) throws IOException
                {
                    writer.write(body);
                }

                public String getTitle()
                {
                    return title;
                }
            };
        }
        return page;
    }

    private MockHttpServletRequest createRequest(final Page page, final String path, final boolean dispatched)
    {
        // Don't really want to use the mockobjects version, but using it is easier than implementing the raw interface
        return new MockHttpServletRequest()
        {
            public Object getAttribute(String s)
            {
                if (s.equals(RequestConstants.PAGE))
                {
                    return page;
                }
                else if (dispatched && s.equals("javax.servlet.include.servlet_path"))
                {
                    return path;
                }
                return null;
            }

            public String getServletPath()
            {
                if (!dispatched)
                {
                    return path;
                }
                else
                {
                    return "irrelaventpath";
                }
            }

            public String getContextPath()
            {
                return "context";
            }
        };
    }

    private MockHttpServletResponse createResponse()
    {
        return new MockHttpServletResponse()
        {
            private PrintWriter writer = null;

            public PrintWriter getWriter() throws IOException
            {
                if (writer == null)
                {
                    writer = super.getWriter();
                }
                return writer;
            }

            public String getOutputStreamContents()
            {
                if (writer != null)
                {
                    writer.flush();
                }
                return super.getOutputStreamContents();
            }
        };
    }
}
