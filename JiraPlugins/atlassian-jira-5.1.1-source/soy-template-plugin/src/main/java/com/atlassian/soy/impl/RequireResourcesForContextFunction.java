package com.atlassian.soy.impl;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.inject.Singleton;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Singleton
public class RequireResourcesForContextFunction implements SoyTofuFunction
{

    private static final Set<Integer> ARGS_SIZE = Collections.singleton(1);

    private final WebResourceManager webResourceManager;

    @Inject
    public RequireResourcesForContextFunction(WebResourceManager webResourceManager)
    {
        this.webResourceManager = webResourceManager;
    }

    @Override
    public SoyData computeForTofu(List<SoyData> args)
    {
        final SoyData data = args.get(0);
        if (!(data instanceof StringData))
        {
            throw new SoySyntaxException("Argument to " + getName() + "() is not a literal string");
        }
        final String context = data.stringValue();
        webResourceManager.requireResourcesForContext(context);
        return StringData.EMPTY_STRING;
    }

    @Override
    public String getName()
    {
        return "webResourceManager_requireResourcesForContext";
    }

    @Override
    public Set<Integer> getValidArgsSizes()
    {
        return ARGS_SIZE;
    }
}
