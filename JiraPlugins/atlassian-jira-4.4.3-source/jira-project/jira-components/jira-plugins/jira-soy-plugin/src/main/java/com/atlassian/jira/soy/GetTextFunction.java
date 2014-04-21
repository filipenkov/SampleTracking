package com.atlassian.jira.soy;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.internal.base.CharEscaper;
import com.google.template.soy.internal.base.CharEscapers;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcFunction;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * I18N getText() soy function.
 *
 * @since v4.4
 */
@Singleton
public class GetTextFunction implements SoyJsSrcFunction
{
    private static final Pattern STRING_ARG = Pattern.compile("^'(.*)'$");

    private static final Set<Integer> ARGS_SIZES;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    static
    {
        final ImmutableSet.Builder<Integer> args = ImmutableSet.builder();
        // we support 1 or more args, lets just pick an upper limit
        for (int i = 1; i < 20; i++)
        {
            args.add(i);
        }
        ARGS_SIZES = args.build();

    }

    @Inject
    public GetTextFunction(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public String getName()
    {
        return "getText";
    }

    @Override
    public Set<Integer> getValidArgsSizes()
    {
        return ARGS_SIZES;
    }

    @Override
    public JsExpr computeForJsSrc(List<JsExpr> args)
    {
        JsExpr keyExpr = args.get(0);
        final Matcher m = STRING_ARG.matcher(keyExpr.getText());
        if (!m.matches())
        {
            throw new SoySyntaxException("Argument to getText() is not a literal string: " + keyExpr.getText());
        }
        String key = m.group(1);

        final I18nHelper i18n = jiraAuthenticationContext.getI18nHelper();

        CharEscaper jsEscaper = CharEscapers.javascriptEscaper();

        StringBuilder call = new StringBuilder();
        if (args.size() == 1)
        {
            // don't bother with call to format, render on server side
            call.append("\"").append(jsEscaper.escape(i18n.getText(key))).append("\"");
        }
        else
        {
            final String msg = i18n.getUnescapedText(key);
            call.append("AJS.format(");
            call.append("\"").append(jsEscaper.escape(msg)).append("\"");
            for (int i = 1; i < args.size(); i++)
            {
                JsExpr arg = args.get(i);
                call.append(",");
                call.append(arg.getText());
            }
            call.append(")");
        }

        return new JsExpr(call.toString(), Integer.MAX_VALUE);
    }
}
