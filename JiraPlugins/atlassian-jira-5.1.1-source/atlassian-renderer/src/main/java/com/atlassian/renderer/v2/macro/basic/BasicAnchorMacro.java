package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.util.RendererUtil;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Category;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: detkin
 * Date: Aug 19, 2005
 * Time: 11:10:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class BasicAnchorMacro extends BaseMacro
{
    private static final Category log = Category.getInstance(BasicAnchorMacro.class);

    public boolean isInline()
    {
        return true;
    }

    public boolean hasBody()
    {
        return false;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.INLINE;
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        if (!TextUtils.stringSet((String) parameters.get("0")))
            return "";

        return "<a name=\"" + getAnchor(renderContext, (String)parameters.get("0")) + "\">" + body + "</a>";
    }

    public static String getAnchor(RenderContext context, String body)
    {
        String result = "";

        result += RendererUtil.summarise(TextUtils.noNull(RenderedContentStore.stripTokens(body)).trim()); // strip formatting chars with summarise()
        result = result.replaceAll(" ", ""); // strip spaces
        try
        {
            result = URLEncoder.encode(result, context.getCharacterEncoding());
        }
        catch (UnsupportedEncodingException e)
        {
            log.warn("Unable to escape anchor value because of an unsupported character encoding of: " + context.getCharacterEncoding());
        }
        return result;
    }
}
