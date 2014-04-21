package com.atlassian.soy.impl;

import com.atlassian.sal.api.ApplicationProperties;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.internal.base.CharEscaper;
import com.google.template.soy.internal.base.CharEscapers;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcFunction;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple function that returns the current request context to the caller.
 *
 * @since v4.4
 */
@Singleton
public class ContextFunction implements SoyJsSrcFunction, SoyTofuFunction
{
    private final String contextPath;

    @Inject
    public ContextFunction(ApplicationProperties appProps) throws URISyntaxException {
        String baseUrl = stripTrailingSlash(appProps.getBaseUrl());
        contextPath = new URI(baseUrl).getPath();
    }

    private static String stripTrailingSlash(String base) {
        if (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    @Override
    public String getName()
    {
        return "contextPath";
    }

    @Override
    public Set<Integer> getValidArgsSizes()
    {
        HashSet<Integer> set = new HashSet<Integer>();
        set.add(0);
        return set;
    }

    @Override
    public JsExpr computeForJsSrc(List<JsExpr> args)
    {
        CharEscaper jsEscaper = CharEscapers.javascriptEscaper();
        return new JsExpr('"' + jsEscaper.escape(contextPath) + '"', Integer.MAX_VALUE);
    }

    @Override
    public SoyData computeForTofu(List<SoyData> soyDatas)
    {
        return StringData.forValue(contextPath);
    }
}
