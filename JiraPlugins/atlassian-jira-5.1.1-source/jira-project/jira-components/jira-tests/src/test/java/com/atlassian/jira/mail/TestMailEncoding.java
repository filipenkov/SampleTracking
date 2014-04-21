package com.atlassian.jira.mail;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.TestAbstractJiraHome;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

public class TestMailEncoding extends LegacyJiraMockTestCase
{


    public TestMailEncoding(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {

        super.tearDown();
    }

    public void testFallbackEncoding()
    {
        ApplicationProperties ap = new ApplicationPropertiesImpl(
                new ApplicationPropertiesStore(ComponentAccessor.getComponent(PropertiesManager.class),
                new TestAbstractJiraHome.FixedHome()))
        {
            public String getDefaultBackedString(String name)
            {
                return null;
            }
        };

        assertEquals("Mail Encoding", ap.getMailEncoding(), ap.getEncoding());
        assertNotNull("Fallbak Encoding", ap.getMailEncoding());

    }

    public void testEncoding()
    {
        ApplicationProperties ap = new ApplicationPropertiesImpl(null)
        {
            public String getDefaultBackedString(String name)
            {
                return "iso-2022-jp";
            }
        };

        assertEquals("Mail Encoding", ap.getMailEncoding(), "iso-2022-jp");

    }

    public void testEmailCreationEncoding()
    {
        Email e = new Email("test@atlassian.com");

        assertEquals("Email Creation Encoding", e.getEncoding(), ComponentAccessor.getApplicationProperties().getMailEncoding());

    }


}
