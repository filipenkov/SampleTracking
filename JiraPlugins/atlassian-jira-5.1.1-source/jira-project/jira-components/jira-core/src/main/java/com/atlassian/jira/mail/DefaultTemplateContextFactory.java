/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.util.DiffViewRenderer;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Locale;

public class DefaultTemplateContextFactory implements TemplateContextFactory
{
    private final TemplateIssueFactory templateIssueFactory;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final EventTypeManager eventTypeManager;
    private final DiffViewRenderer diffViewRenderer;
    private final WebResourceManager webResourceManager;
    private final WebResourceManager resourceManager;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory beanFactory;

    public DefaultTemplateContextFactory(TemplateIssueFactory templateIssueFactory, FieldLayoutManager fieldLayoutManager,
            RendererManager rendererManager, JiraDurationUtils jiraDurationUtils, EventTypeManager eventTypeManager,
            DiffViewRenderer diffViewRenderer, WebResourceManager webResourceManager, I18nHelper.BeanFactory beanFactory,
            ApplicationProperties applicationProperties, WebResourceManager resourceManager)
    {
        this.templateIssueFactory = templateIssueFactory;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.eventTypeManager = eventTypeManager;
        this.diffViewRenderer = diffViewRenderer;
        this.webResourceManager = webResourceManager;
        this.beanFactory = beanFactory;
        this.applicationProperties = applicationProperties;
        this.resourceManager = resourceManager;
    }

    @Override
    public TemplateContext getTemplateContext(final Locale locale)
    {
        return new DefaultTemplateContext(locale, resourceManager, applicationProperties, beanFactory);
    }

    @Override
    public TemplateContext getTemplateContext(final Locale locale, final IssueEvent issueEvent)
    {
        return new IssueTemplateContext(locale, issueEvent, templateIssueFactory, fieldLayoutManager, rendererManager,
                jiraDurationUtils, eventTypeManager, diffViewRenderer, webResourceManager, applicationProperties, beanFactory);
    }
}
