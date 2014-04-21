package com.atlassian.jira.trackback;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.trackback.Trackback;
import com.atlassian.trackback.TrackbackException;
import com.mockobjects.servlet.MockHttpServletRequest;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

public class TestOfbizTrackbackStore extends TestCase
{
    Trackback tb;

    @Before
    public void setUp() throws Exception
    {
        tb = new Trackback();
        tb.setBlogName("MyBlog");
        tb.setUrl("http://myblog.com");
        tb.setExcerpt("Test blog entry, mentioning JRA-123");
        tb.setTitle("Bugs I Have Known..");
    }

    @Test
    public void testStoreTrackback() throws GenericEntityException, TrackbackException
    {
        final IssueManager issueManager = EasyMock.createMock(IssueManager.class);
        final TrackbackManager trackbackManager = EasyMock.createMock(TrackbackManager.class);
        final ApplicationProperties applicationProperties = EasyMock.createMock(ApplicationProperties.class);

        final MockGenericValue issue = new MockGenericValue("Issue");
        EasyMock.expect(issueManager.getIssue("JRA-123")).andReturn(issue);

        trackbackManager.storeTrackback(tb, issue);
        EasyMock.expectLastCall();

        EasyMock.expect(applicationProperties.getOption("jira.option.trackback.receive")).andReturn(true);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setupPathInfo("/JRA-123");

        EasyMock.replay(issueManager, trackbackManager, applicationProperties);
        OfbizTrackbackStore store = new OfbizTrackbackStore()
        {
            @Override
            protected IssueManager getIssueManager()
            {
                return issueManager;
            }

            @Override
            protected ApplicationProperties getApplicationProperties()
            {
                return applicationProperties;
            }

            @Override
            protected TrackbackManager getTrackbackManager()
            {
                return trackbackManager;
            }
        };

        store.storeTrackback(tb, req);

        EasyMock.verify(trackbackManager);
        req.verify();
    }
}
