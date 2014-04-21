/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.util.DiffViewRenderer;
import com.atlassian.plugin.webresource.WebResourceManager;

public class DefaultTemplateContextFactory implements TemplateContextFactory
{
    private final TemplateIssueFactory templateIssueFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final EventTypeManager eventTypeManager;
    private final DiffViewRenderer diffViewRenderer;
    private final WebResourceManager webResourceManager;

    public DefaultTemplateContextFactory(TemplateIssueFactory templateIssueFactory, FieldLayoutManager fieldLayoutManager,
            RendererManager rendererManager, JiraDurationUtils jiraDurationUtils, EventTypeManager eventTypeManager,
            DiffViewRenderer diffViewRenderer, WebResourceManager webResourceManager)
    {
        this.templateIssueFactory = templateIssueFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.eventTypeManager = eventTypeManager;
        this.diffViewRenderer = diffViewRenderer;
        this.webResourceManager = webResourceManager;
    }

    public TemplateContext getTemplateContext(IssueEvent issueEvent)
    {
        return new TemplateContext(issueEvent, templateIssueFactory, fieldLayoutManager, rendererManager,
                jiraDurationUtils, eventTypeManager, diffViewRenderer, webResourceManager);
    }
}
