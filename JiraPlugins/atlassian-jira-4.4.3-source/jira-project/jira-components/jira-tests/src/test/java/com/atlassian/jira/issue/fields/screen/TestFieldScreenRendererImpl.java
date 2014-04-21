package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayout;

import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.FieldScreenRendererImpl}.
 *
 * @since v4.1
 */
public class TestFieldScreenRendererImpl extends ListeningTestCase
{
    @Test
    public void testGetFieldScreenRenderTabs() throws Exception
    {
        final MockFieldScreenRendererTab fieldScreenRendererTab = new MockFieldScreenRendererTab();
        final List<FieldScreenRenderTab> expectedList = Collections.<FieldScreenRenderTab>singletonList(fieldScreenRendererTab);
        FieldScreenRendererImpl renderer = new FieldScreenRendererImpl(expectedList, null);
        assertEquals(expectedList, renderer.getFieldScreenRenderTabs());
    }

    @Test
    public void testGetFieldLayout() throws Exception
    {
        final MockFieldScreenRendererTab fieldScreenRendererTab = new MockFieldScreenRendererTab();
        final List<FieldScreenRenderTab> expectedList = Collections.<FieldScreenRenderTab>singletonList(fieldScreenRendererTab);
        final MockFieldLayout fieldLayout = new MockFieldLayout();

        FieldScreenRendererImpl renderer = new FieldScreenRendererImpl(expectedList, fieldLayout);
        assertSame(fieldLayout, renderer.getFieldLayout());
    }
}
