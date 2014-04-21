package com.atlassian.soy.impl;

import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.inject.Singleton;
import com.google.template.soy.data.SanitizedContent;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;

import javax.inject.Inject;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Singleton
public class IncludeResourcesFunction implements SoyTofuFunction
{

    private static final Set<Integer> ARGS_SIZE = Collections.singleton(0);

    private final WebResourceManager webResourceManager;

    @Inject
    public IncludeResourcesFunction(WebResourceManager webResourceManager)
    {
        this.webResourceManager = webResourceManager;
    }

    @Override
    public SoyData computeForTofu(List<SoyData> args)
    {
        StringWriter writer = new StringWriter();
        webResourceManager.includeResources(writer, UrlMode.AUTO);
        return new SanitizedContent(writer.toString(), SanitizedContent.ContentKind.HTML);
    }

    @Override
    public String getName()
    {
        return "webResourceManager_includeResources";
    }

    @Override
    public Set<Integer> getValidArgsSizes()
    {
        return ARGS_SIZE;
    }

}
