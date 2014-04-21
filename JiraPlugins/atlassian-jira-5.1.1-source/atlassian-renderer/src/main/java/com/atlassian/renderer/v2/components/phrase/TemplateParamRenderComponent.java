package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.v2.components.AbstractRegexRendererComponent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class TemplateParamRenderComponent extends AbstractRegexRendererComponent
{
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("@([\\p{L}0-9_|\\(\\),]+)@");

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderTemplate();
    }

    public String render(String wiki, RenderContext context)
    {
        if (wiki.indexOf("@") == -1)
        {
            return wiki;
        }
        return regexRender(wiki, context, VARIABLE_PATTERN);
    }

    public void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher)
    {
        buffer.append(context.getRenderedContentStore().addInline(makeFormElement(matcher)));
    }

    private String makeFormElement(Matcher match)
    {
        String resultStr = match.group(1);
        StringBuffer result = new StringBuffer();

        String paramName;

        if (resultStr.indexOf('|') < 0)
        {
            paramName = resultStr;
            result.append("<input type=\"text\" name=\"variableValues." + paramName + "\" size=\"12\" onkeyup=\"updateOthers(this)\" />");
        }
        else
        {
            paramName = resultStr.substring(0, resultStr.indexOf('|'));
            String paramType = resultStr.substring(resultStr.indexOf('|') + 1);

            if (paramType.toLowerCase().startsWith("textarea"))
            {
                handleTextArea(result, paramName, paramType);
            }
            else if (paramType.toLowerCase().startsWith("list"))
            {
                handleList(result, paramName, paramType);
            }
        }

        result.append("&nbsp;<span class=\"templateparameter\">(" + paramName + ")</span>");
        return result.toString();
    }

    private void handleList(StringBuffer stringBuffer, String paramName, String paramType)
    {
        List paramParameters = getParameters(paramType);

        stringBuffer.append("<select name=\"variableValues." + paramName + "\">");

        for (Iterator iterator = paramParameters.iterator(); iterator.hasNext();)
        {
            String param = (String) iterator.next();
            stringBuffer.append("<option value=\"" + param + "\">" + param + "</option>");
        }
        stringBuffer.append("</select>");
    }

    private void handleTextArea(StringBuffer stringBuffer, String paramName, String paramType)
    {
        String rows = "4";
        String cols = "40";

        List paramParameters = getParameters(paramType);

        if (paramParameters.size() > 0)
            rows = (String) paramParameters.get(0);

        if (paramParameters.size() > 1)
            cols = (String) paramParameters.get(1);

        stringBuffer.append("<textarea name=\"variableValues." + paramName + "\" rows=\"" + rows + "\" cols=\"" + cols + "\"></textarea>");
    }

    private List getParameters(String paramType)
    {
        int firstBrace = paramType.indexOf('(');

        if (firstBrace < 0)
            return Collections.EMPTY_LIST;

        int lastBrace = paramType.lastIndexOf(')');

        if (lastBrace < 0)
            return Collections.EMPTY_LIST;

        StringTokenizer tokens = new StringTokenizer(paramType.substring(firstBrace + 1, lastBrace), ",");
        List result = new ArrayList(tokens.countTokens());

        while (tokens.hasMoreTokens())
        {
            result.add(tokens.nextToken());
        }

        return result;
    }

}
