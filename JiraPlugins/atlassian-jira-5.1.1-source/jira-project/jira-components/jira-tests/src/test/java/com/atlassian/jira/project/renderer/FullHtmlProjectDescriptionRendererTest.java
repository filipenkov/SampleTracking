package com.atlassian.jira.project.renderer;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.project.Project;
import org.junit.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FullHtmlProjectDescriptionRendererTest extends ListeningTestCase
{
    private Project project = mock(Project.class);

    private final FullHtmlProjectDescriptionRenderer renderer = new FullHtmlProjectDescriptionRenderer();

    @Test
    public void testRenderViewHtml()
    {
        final String projectDescriptionAsHtml = "<p>my project with html</p>";
        when(project.getDescription()).thenReturn(projectDescriptionAsHtml);

        final String html = renderer.getViewHtml(project);
        assertThat(html, is(projectDescriptionAsHtml));
    }

    @Test
    public void testRenderEditHtml()
    {
        final String projectDescriptionAsHtml = "<p>my project with html</p>";
        when(project.getDescription()).thenReturn(projectDescriptionAsHtml);

        final String html = renderer.getEditHtml(project);
        assertThat(html, allOf(startsWith("<textarea"), containsString(projectDescriptionAsHtml), endsWith("</textarea>")));
    }
}
