package com.atlassian.gadgets.embedded.internal;

import java.io.IOException;
import java.io.Writer;

import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.ViewComponent;
import com.atlassian.gadgets.view.ViewType;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

class GadgetViewComponent implements ViewComponent
{
    private final ModuleId id;
    private final ViewType viewType;
    private final GadgetSpec spec;
    private final String renderedUrl;

    GadgetViewComponent(ModuleId id, ViewType viewType, GadgetSpec spec, String renderedUrl)
    {
        // TODO change to be module id when AG-396 is implemented  
        this.id = id;
        this.viewType = viewType;
        this.spec = spec;
        this.renderedUrl = renderedUrl;
    }

    public void writeTo(Writer writer) throws IOException
    {
        long rpcToken = Math.round(0x7FFFFFFF * Math.random());
        String renderedUrlWithRpcToken = escapeHtml(renderedUrl + "#rpctoken=" + rpcToken);
        String iframeId = "gadget-" + escapeHtml(id.toString());

        writer.write("<iframe id=\"");
        writer.write(iframeId);
        writer.write("\" name=\"");
        writer.write(iframeId);
        writer.write("\" class=\"gadget\" src=\"");
        writer.write(renderedUrlWithRpcToken);
        writer.write("\" frameborder=\"0\"");
        writer.write(" scrolling=\"");
        writer.write(spec.isScrolling()?"auto":"no");
        writer.write("\" ");
        if (spec.getHeight() > 0)
        {
            writer.write(" height=\"");
            writer.write(Integer.toString(spec.getHeight()));
            writer.write("\"");
        }
        if (viewType == ViewType.CANVAS)
        {
            writer.write(" width=\"100%\"");
        }
        else if (spec.getWidth() > 0)
        {
            writer.write(" width=\"");
            writer.write(Integer.toString(spec.getWidth()));
            writer.write("\"");
        }
        writer.write(">");
        writer.write("<a href=\"");
        writer.write(renderedUrlWithRpcToken);
        writer.write("\">");
        writer.write(escapeHtml(spec.getTitle()));
        writer.write("</a></iframe>");
    }
}
