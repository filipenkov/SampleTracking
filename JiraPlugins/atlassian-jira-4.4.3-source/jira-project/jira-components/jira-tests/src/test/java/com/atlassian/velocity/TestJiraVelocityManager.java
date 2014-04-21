package com.atlassian.velocity;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.OutlookDate;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestJiraVelocityManager extends ListeningTestCase
{
    @Test
    public void testGetBaseUrl() throws Exception
    {
        final JiraAuthenticationContext context = new MockSimpleAuthenticationContext(null);
        final JiraVelocityManager manager = new JiraVelocityManager(context);
        final Map<String, ?> contextParameters = manager.createContextParams("someurl", Collections.emptyMap());
        assertNotNull(contextParameters.get("baseurl"));
        assertEquals("someurl", contextParameters.get("baseurl"));
    }

    @Test
    public void testBaseUrlIsOverridenByMapValue() throws Exception
    {
        final JiraAuthenticationContext context = new MockSimpleAuthenticationContext(null);
        final JiraVelocityManager manager = new JiraVelocityManager(context);
        final Map<String, ?> contextParameters = manager.createContextParams("someurl", Collections.singletonMap("baseurl", "differenturl"));
        assertNotNull(contextParameters.get("baseurl"));
        assertEquals("differenturl", contextParameters.get("baseurl"));
    }

    @Test
    public void testGetFormatter() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final JiraAuthenticationContext context = new MockSimpleAuthenticationContext(null)
        {
            @Override
            public OutlookDate getOutlookDate()
            {
                return new OutlookDate(Locale.US, new MockApplicationProperties(), null, null)
                {
                    @Override
                    public DateFormat getCompleteDateFormat()
                    {
                        called.set(true);
                        return new SimpleDateFormat();
                    }
                };
            }
        };
        final JiraVelocityManager manager = new JiraVelocityManager(context);
        final Map<String, ?> contextParameters = manager.createContextParams("someurl", Collections.emptyMap());
        assertFalse(called.get());
        assertNotNull(contextParameters.get("formatter"));
        assertFalse(called.get());
        ((DateFormat) contextParameters.get("formatter")).format(new Date());
        assertTrue(called.get());
    }
}
