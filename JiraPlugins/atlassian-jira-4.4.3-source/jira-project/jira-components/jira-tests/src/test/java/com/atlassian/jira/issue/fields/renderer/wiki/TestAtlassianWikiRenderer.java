package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.renderer.RenderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class TestAtlassianWikiRenderer extends ListeningTestCase
{
    @Mock
    private EventPublisher mockPublisher;

    @Before
    public void initMocks()
    {
        EasyMockAnnotations.initMocks(this);
    }

    @Test
    public void testGetRenderContextWithNullIssueRenderContext()
    {
        AtlassianWikiRenderer atlassianWikiRenderer = new AtlassianWikiRenderer(mockPublisher);
        RenderContext renderContext = atlassianWikiRenderer.getRenderContext(null);

        // Make sure that the renderContext does not contain an issue parameter
        assertNull(renderContext.getParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY));
    }

    @Test
    public void testGetRenderContextWithIssue()
    {
        AtlassianWikiRenderer atlassianWikiRenderer = new AtlassianWikiRenderer(mockPublisher);
        MockIssue issue = new MockIssue();
        RenderContext renderContext = atlassianWikiRenderer.getRenderContext(new IssueRenderContext(issue));

        // Make sure that the renderContext does not contain an issue parameter
        assertEquals(issue, renderContext.getParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY));
    }
}
