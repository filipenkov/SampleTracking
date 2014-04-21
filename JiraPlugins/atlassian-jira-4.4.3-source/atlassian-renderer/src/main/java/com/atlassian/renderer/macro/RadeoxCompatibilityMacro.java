package com.atlassian.renderer.macro;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.Macro;
import com.atlassian.renderer.v2.macro.MacroException;
import org.apache.log4j.Category;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.macro.parameter.BaseMacroParameter;
import org.radeox.util.StringBufferWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A wrapper macro that retains compatibility with our old Radeox-style macros
 */
public class RadeoxCompatibilityMacro extends com.atlassian.renderer.v2.macro.BaseMacro
{
    public static final String RENDER_CONTEXT = "RENDER_CONTEXT";
    public static final String ATTACHMENTS_PATH = "ATTACHMENTS_PATH";
    public static final String EXTRACTED_EXTERNAL_REFERENCES = "EXTRACTED_EXTERNAL_REFERENCES";

    public static final Category log = Category.getInstance(RadeoxCompatibilityMacro.class);
    private static final Pattern INLINE_PATTERN = Pattern.compile("^\\s*<(span|code|a |font).*", Pattern.DOTALL);
    private com.atlassian.renderer.macro.Macro radeoxMacro;
    private String lastContent;
    private Boolean inline;

    private static class BogusRadeoxContext implements InitialRenderContext
    {
        public RenderEngine getRenderEngine()
        {
            throw new UnsupportedOperationException("Radeox compatibility layer does not have a render engine");
        }

        public void setRenderEngine(RenderEngine renderEngine)
        {
        }

        public Object get(String s)
        {
            throw new UnsupportedOperationException("Radeox compatibility layer does not have context properties");
        }

        public void set(String s, Object o)
        {
            throw new UnsupportedOperationException("Radeox compatibility layer does not have context properties");
        }

        public Map getParameters()
        {
            throw new UnsupportedOperationException("Radeox compatibility layer does not have context properties");
        }

        public void setParameters(Map map)
        {
            throw new UnsupportedOperationException("Radeox compatibility layer does not have context properties");
        }

        public void setCacheable(boolean b)
        {
        }

        public void commitCache()
        {
            throw new UnsupportedOperationException("Radeox compatibility layer does not have a cache");
        }

        public boolean isCacheable()
        {
            return false;
        }
    }

    public RadeoxCompatibilityMacro(com.atlassian.renderer.macro.Macro radeoxMacro)
    {
        this.radeoxMacro = radeoxMacro;

        try
        {
            radeoxMacro.setInitialContext(new BogusRadeoxContext());
        }
        catch (Exception e)
        {
            log.warn("Error wrapping radeox macro: {" + radeoxMacro.getName() + "} - " + e.getMessage());
        }
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.COMPATIBILITY_MODE;
    }

    public com.atlassian.renderer.macro.Macro getRadeoxMacro()
    {
        return radeoxMacro;
    }

    public boolean hasBody()
    {
        return true;
    }

    public boolean isInline()
    {
        // This is tricky. Basically, we check the results of the first macro we render. If the first HTML
        // tag is span, code, a or font, we assume it's inline.

        if (inline != null)
            return inline.booleanValue();

        if (lastContent == null || lastContent.trim().length() == 0)
            return false;

        if (INLINE_PATTERN.matcher(lastContent).matches())
            inline = Boolean.TRUE;
        else
            inline = Boolean.FALSE;

        lastContent = null;
        return inline.booleanValue();
    }

    public String execute(Map parameters, String content, RenderContext context) throws MacroException
    {
        final BaseRenderContext renderContext = new BaseRenderContext();
        BaseMacroParameter mParams = new BaseMacroParameter(renderContext);
        renderContext.setParameters(new HashMap());

        mParams.getContext().getParameters().put(RENDER_CONTEXT, context);
        String attachmentsPath = context.getAttachmentsPath();
        if(attachmentsPath != null)
            mParams.getContext().getParameters().put(ATTACHMENTS_PATH, attachmentsPath);

        mParams.setContent(content);
        mParams.setContentStart(0);
        mParams.setContentEnd(content.length());
        mParams.setStart(0);
        mParams.setEnd(content.length());
        mParams.setParams((String) parameters.get(Macro.RAW_PARAMS_KEY));
        StringBuffer output = new StringBuffer();
        try
        {
            radeoxMacro.execute(new StringBufferWriter(output), mParams);

            if (inline == null)
                lastContent = output.toString();

            return output.toString();
        }
        catch (IOException e)
        {
            throw new MacroException(e.getMessage(), e);
        }
    }
}

