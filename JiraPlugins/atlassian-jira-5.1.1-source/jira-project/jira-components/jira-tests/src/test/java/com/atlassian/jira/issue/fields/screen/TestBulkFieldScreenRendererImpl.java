package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.BulkFieldScreenRendererImpl}.
 *
 * @since v4.1
 */
public class TestBulkFieldScreenRendererImpl extends ListeningTestCase
{
    @Test
    public void testGetFieldScreenRenderTabs() throws Exception
    {
        final MockFieldScreenRendererTab fieldScreenRendererTab = new MockFieldScreenRendererTab();

        final List<FieldScreenRenderTab> expectedList = Collections.<FieldScreenRenderTab>singletonList(fieldScreenRendererTab);
        BulkFieldScreenRendererImpl renderer = new BulkFieldScreenRendererImpl(expectedList);
        assertEquals(expectedList, renderer.getFieldScreenRenderTabs());
    }

    @Test
    public void testGetFieldLayout() throws Exception
    {
        BulkFieldScreenRendererImpl renderer = new BulkFieldScreenRendererImpl(Collections.<FieldScreenRenderTab>emptyList());
        try
        {
            renderer.getFieldLayout();
            fail("This should throw an exception.");
        }
        catch (UnsupportedOperationException e)
        {
            //expected.
        }
    }
}
