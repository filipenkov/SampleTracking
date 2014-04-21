package com.atlassian.jira.template.soy;

import com.atlassian.jira.ComponentManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

public class SoyTemplateRendererProviderImpl implements SoyTemplateRendererProvider
{
    @Override
    public SoyTemplateRenderer getRenderer()
    {
        return ComponentManager.getOSGiComponentInstanceOfType(SoyTemplateRenderer.class);
    }
}
