package com.atlassian.soy.impl;

import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.Maps;

import java.io.StringWriter;
import java.util.Map;

/**
 * @since v5.0
 */
public class SoyTemplateRendererImpl implements SoyTemplateRenderer
{
    private final SoyManager soyManager;

    public SoyTemplateRendererImpl(SoyManager soyManager)
    {
        this.soyManager = soyManager;
    }

    @Override
    public String render(String completeModuleKey, String templateName, Map<String, Object> data) throws SoyException
    {
        StringBuilder sb = new StringBuilder();
        render(sb, completeModuleKey, templateName, data);
        return sb.toString();
    }

    @Override
    public void render(Appendable appendable, String completeModuleKey, String templateName, Map<String, Object> data) throws SoyException
    {
        render(appendable, completeModuleKey, templateName, data, Maps.<String, Object>newHashMap());
    }

    @Override
    public void render(Appendable appendable, String completeModuleKey, String templateName,
                       Map<String, Object> data, Map<String, Object> injectedData) throws SoyException
    {
        soyManager.render(appendable, completeModuleKey, templateName, data, injectedData);
    }
}
