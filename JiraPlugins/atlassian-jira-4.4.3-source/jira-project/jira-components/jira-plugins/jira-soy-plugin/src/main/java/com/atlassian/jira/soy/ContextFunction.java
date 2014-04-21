package com.atlassian.jira.soy;

import com.atlassian.jira.web.ExecutingHttpRequest;
import com.google.template.soy.internal.base.CharEscaper;
import com.google.template.soy.internal.base.CharEscapers;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcFunction;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple function that returns the current request context to the caller.
 *
 * @since v4.4
 */
@Singleton
public class ContextFunction implements SoyJsSrcFunction
{
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
        String contextPath = ExecutingHttpRequest.get().getContextPath();
        return new JsExpr('"' + jsEscaper.escape(contextPath) + '"', Integer.MAX_VALUE);
    }
}
