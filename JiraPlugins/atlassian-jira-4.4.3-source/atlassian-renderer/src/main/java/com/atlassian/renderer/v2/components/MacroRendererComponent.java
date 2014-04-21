/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 3:35:05 PM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.macro.RadeoxCompatibilityMacro;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.WikiMarkupParser;
import com.atlassian.renderer.v2.macro.Macro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.MacroManager;
import com.atlassian.renderer.v2.macro.ResourceAwareMacroDecorator;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Category;

import java.util.HashMap;
import java.util.Map;

public class MacroRendererComponent extends AbstractRendererComponent
{
    private static Category log = Category.getInstance(MacroRendererComponent.class);

    private final MacroManager macroManager;
    private final SubRenderer subRenderer;

    public MacroRendererComponent(MacroManager macroManager, SubRenderer subRenderer)
    {
        this.macroManager = macroManager;
        this.subRenderer = subRenderer;
    }

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderMacros();
    }

    public String render(String wiki, RenderContext context)
    {
        WikiMarkupParser parser = new WikiMarkupParser(macroManager, new WikiContentRendererHandler(this,context));
        return parser.parse(wiki);
    }

    public void makeMacro(StringBuffer buffer, MacroTag startTag, String body, RenderContext context, boolean hasEndTag)
    {
        Macro macro = getMacroByName(startTag.command);
        Map params = makeParams(startTag.argString);
        if (context.isRenderingForWysiwyg())
        {
            // From a wysiwyg point of view there are four cases:
            // 1. Macros with unrendered bodies (or no bodies). These appear as {macro} ... unrendered body ... {macro}, so
            //    the user can edit the body text in wysiwyg mode.
            // 2. Macros with rendered bodies, but which the editor doesn't 'understand' -- that is, the editor can't
            //    manipulate the HTML produced by the macro. These are rendered as {macro} ... rendered body ... {macro}.
            //    A macro indicates that the editor doesn't understand it by returning true from suppressMacroRenderingDuringWysiwyg().
            //    Most macros should do this, unless the Wysiwyg converter understands how to create a new instance of the macro.
            // 3. Macros we fully understand. These are simply rendered as normal (but surrounded by a div or span describing them).
            //    These return false from suppressMacroRenderingDuringWysiwyg().
            // 4. Macros which are responsible for their own rendering. These return true from suppressSurroundingTagDuringWysiwygRendering()
            String tag = getWysiwygMacroElement(macro);
            // check whether we want to show the body of this macro rendered, or just show {macro} ... body ... {macro}
            if (macro == null
                    || (macro.getBodyRenderMode() != null && macro.getBodyRenderMode().equals(RenderMode.NO_RENDER))
                    || !macro.hasBody())
            {
                // case 1
                StringBuffer sb = new StringBuffer();
                if (tag != null)
                {
                    sb.append("<" + tag + " class=\"macro\">");
                    sb.append(startTag.originalText + body.replaceAll("\n", "<br/>"));
                    
                    //we may still require the endtag even if the body is empty CONF-6580
                    if(body.length() > 0 || hasEndTag)
                    {
                        sb.append( "{" + startTag.command + "}");
                    }

                    sb.append("</" + tag + ">");
                    buffer.append(context.getRenderedContentStore().addBlock(sb.toString()));
                }
                else
                {
                    // case 4
                    processMacro(startTag.command, macro, body, params, context, buffer);
                }
            }
            else if (!macro.suppressMacroRenderingDuringWysiwyg())
            {
                // case 3 and 4
                if (tag != null)
                {
                    buffer.append(context.getRenderedContentStore().addBlock("<" + tag + " class=\"macro\" macrotext=\"" + startTag.originalText + "\" command=\"" + startTag.command + "\">" /*+ getWysiwygConverter().getMacroInfoHtml(context, startTag.command, 0, 0)*/));
                }
                processMacro(startTag.command, macro, body, params, context, buffer);
                if (tag != null)
                {
                    buffer.append(context.getRenderedContentStore().addBlock("</" + tag + ">"));
                }
            }
            else
            {
                // case 2
                buffer.append(context.getRenderedContentStore().addBlock("<div class=\"wikisrc\">" + (macro.isInline() ? "" : "\n") + startTag.originalText + "</div>"));
                if (
                        (
                                !(macro instanceof RadeoxCompatibilityMacro)
                                        &&
                                !(
                                        macro instanceof ResourceAwareMacroDecorator
                                          &&
                                        ((ResourceAwareMacroDecorator)macro).getMacro() instanceof RadeoxCompatibilityMacro
                                 )
                        )
                        ||
                      !body.equals("")
                   )
                {
                    RenderMode renderMode = RenderMode.suppress(RenderMode.F_FIRST_PARA);
                    if (macro.getBodyRenderMode() != null)
                    {
                        renderMode = macro.getBodyRenderMode().and(renderMode);
                    }
                    buffer.append(context.addRenderedContent(subRenderer.render(body, context, renderMode)));
                    buffer.append(context.getRenderedContentStore().addBlock("<div class=\"wikisrc\">" + (macro.isInline() ? "" : "\n") + "{" + startTag.command + "}" + "</div>"));
                }
            }
            if (tag != null && tag.equals("div"))
            {
                // this provides a place for users to add more text. The 'atl_conf_pad' class allows us to ignore it when
                // converting back to markup, if the user hasn't changed the content.
                // In fact, simply having this class set seems to stop TinyMCE converting this into <p>&nbsp;</p>, so
                // the stripping code is never executed, unless the user adds something to this paragraph.
                buffer.append(context.getRenderedContentStore().addBlock("<p class=\"atl_conf_pad\">&#8201;</p>"));
            }
            else if (!macro.suppressSurroundingTagDuringWysiwygRendering()) // don't do this for the color macro
            {
                buffer.append(context.getRenderedContentStore().addBlock("&#8201;"));
            }
        }
        else
        {
            if (macro != null)
            {
                processMacro(startTag.command, macro, body, params, context, buffer);
            }
            else
            {
                if(!context.getRenderMode().renderMacroErrorMessages())
                {
                    HtmlEscapeRendererComponent htmlEscapeRendererComponent = new HtmlEscapeRendererComponent();
                    StringBuffer errorBuffer = new StringBuffer();
                    // there must be a more efficient way to do this escaping, it seems there are not simple html
                    // escape utilities available to the renderer other than this component
                    errorBuffer.append(htmlEscapeRendererComponent.render(startTag.originalText, context));
                    // append the body and closing tags if there is one
                    if(body != null && !"".equals(body.trim()))
                    {
                        errorBuffer.append(subRenderer.render(body, context, context.getRenderMode().and(RenderMode.suppress(RenderMode.F_FIRST_PARA | RenderMode.F_PARAGRAPHS))));
                        errorBuffer.append("{" + htmlEscapeRendererComponent.render(startTag.command, context) + "}");
                    }
                    buffer.append(context.getRenderedContentStore().addBlock(errorBuffer.toString()));
                }
                else
                {
                    buffer.append(makeMacroError(context, "Unknown macro: {" + startTag.command + "}", body));
                }
            }
        }
    }

    private Macro getMacroByName(String name)
    {
        if (name == null)
        {
            return null;
        }

        return macroManager.getEnabledMacro(name.toLowerCase());
    }

    private Map makeParams(String paramString)
    {
        Map params = new HashMap();
        params.put(Macro.RAW_PARAMS_KEY, paramString == null ? "" : paramString);

        if (!TextUtils.stringSet(paramString))
        {
            return params;
        }

        String[] paramStrs = paramString.split("\\|");

        for (int i = 0; i < paramStrs.length; i++)
        {
            String paramStr = paramStrs[i];
            int idx;

            if ((idx = paramStr.indexOf("=")) != -1)
            {
                if (idx == paramStr.length() - 1)
                {
                    params.put(paramStr.substring(0, idx).trim(), "");
                }
                else
                {
                    params.put(paramStr.substring(0, idx).trim(), paramStr.substring(idx + 1).trim());
                }
            }
            else
            {
                params.put(String.valueOf(i), paramStr);
            }
        }

        return params;
    }
    
    private String getWysiwygMacroElement(Macro macro)
    {
        if (macro == null)
        {
            return "div";
        }
        if (macro.suppressSurroundingTagDuringWysiwygRendering())
        {
            return null;
        }
        if (macro.isInline())
        {
            return "span";
        }
        else
        {
            return "div";
        }
    }

    private void processMacro(String command, Macro macro, String body, Map params, RenderContext context, StringBuffer buffer)
    {
        String renderedBody = body;
        try
        {
            if (TextUtils.stringSet(body) && macro.getBodyRenderMode() != null && !macro.getBodyRenderMode().renderNothing())
            {
                renderedBody = subRenderer.render(body, context, macro.getBodyRenderMode());
            }

            String macroResult = macro.execute(params, renderedBody, context);

            if (macro.getBodyRenderMode() == null)
            {
                /* We need to render the macros only here as the other render components will be run after this one.
                   This is to ensure macros with macros inside the body also get rendered correctly. (RNDR-4) */
                buffer.append(subRenderer.render(macroResult, context, RenderMode.MACROS_ONLY));
            }
            else if (macro.isInline())
            {
                buffer.append(context.getRenderedContentStore().addInline(macroResult));
            }
            else
            {
                buffer.append(context.addRenderedContent(macroResult));
            }

        }
        catch (MacroException e)
        {
            log.info("Error formatting macro: " + command + ": " + e, e);
            buffer.append(makeMacroError(context, command + ": " + e.getMessage(), body));
        }
        catch (Throwable t)
        {
            if (isJunitError(t))
            {
                throw (RuntimeException) t;
            }

            log.error("Unexpected error formatting macro: " + command, t);
            buffer.append(makeMacroError(context, "Error formatting macro: " + command + ": " + t.toString(), body));
        }
    }

    private String makeMacroError(RenderContext context, String errorMessage, String body)
    {
        return context.addRenderedContent(RenderUtils.blockError(errorMessage, renderErrorBody(body, context)));
    }

    private String renderErrorBody(String body, RenderContext context)
    {
        return context.addRenderedContent(subRenderer.render(body, context, null));
    }

    // This method makes tests run more smoothly without introducing a dependency
    // between this class and the junit libraries
    private boolean isJunitError(Throwable t)
    {
        return t.getClass().getName().startsWith("junit.");
    }
}