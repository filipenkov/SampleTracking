package com.atlassian.jira.event.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.local.ListeningTestCase;
import com.google.common.collect.ImmutableMap;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link com.atlassian.jira.event.issue.DefaultIssueEventManager}.
 *
 * @since v4.4
 */
public class TestDefaultIssueEventManager extends ListeningTestCase
{
    private static final String DEFAULT_BASE_URL = "http://jira.atlassian.com";
    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Issue issue;

    @Mock
    private User user;

    private DefaultIssueEventManager tested;

    private IssueEvent publishedEvent;

    @Before
    public void initMocks()
    {
        EasyMockAnnotations.initMocks(this);
        stubEventPublisher();
        this.tested = new DefaultIssueEventManager(applicationProperties, eventPublisher);
    }

    private void stubEventPublisher()
    {
        eventPublisher.publish(anyObject());
        expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer() throws Throwable
            {
                publishedEvent = (IssueEvent) EasyMock.getCurrentArguments()[0];
                return null;
            }
        });
        replay(eventPublisher);
    }

    @Test
    public void shouldDispatchSimpleEventSendMail()
    {
        stubDefaultProperties();
        replay(issue, user);
        tested.dispatchEvent(1L, issue, user, true);
        assertEquals(1L, publishedEvent.getEventTypeId().longValue());
        assertEquals(issue, publishedEvent.getIssue());
        assertEquals(user, publishedEvent.getUser());
        assertTrue(publishedEvent.isSendMail());
        assertDefaultParameters();
    }

    @Test
    public void shouldDispatchEventWithCustomParameters()
    {
        stubDefaultProperties();
        replay(issue, user);
        Map<String,Object> parameters = ImmutableMap.<String,Object>of("customParam", "customValue");
        tested.dispatchEvent(1L, issue, parameters, user, false);
        assertEquals(1L, publishedEvent.getEventTypeId().longValue());
        assertEquals(issue, publishedEvent.getIssue());
        assertEquals(user, publishedEvent.getUser());
        assertFalse(publishedEvent.isSendMail());
        assertCustomParameters(parameters);
    }

    private void stubDefaultProperties()
    {
        expect(applicationProperties.getString(APKeys.JIRA_BASEURL)).andReturn(DEFAULT_BASE_URL);
        replay(applicationProperties);
    }

    private void assertDefaultParameters()
    {
        // just one default param
        assertEquals(1, publishedEvent.getParams().size());
        assertEquals(DEFAULT_BASE_URL, publishedEvent.getParams().get(IssueEvent.BASE_URL_PARAM_NAME));
    }

    private void assertCustomParameters(Map<String,Object> parameters)
    {
        Map<String,Object> expected = ImmutableMap.<String, Object>builder()
                .putAll(parameters)
                .put(IssueEvent.BASE_URL_PARAM_NAME, DEFAULT_BASE_URL).build();
        assertEquals(expected, publishedEvent.getParams());
    }

}
