package com.atlassian.renderer.wysiwyg;

import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;
import com.opensymphony.util.TextUtils;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This tests the renderers handling of things like the layout macros, not the Confluence layout macros themselves.
 */
public class TestLayoutMacros extends WysiwygTest
{
    private static class ColumnMacro extends BaseMacro implements WysiwygNodeConverter
    {
        private WysiwygConverter wysiwygConverter;

        public boolean suppressSurroundingTagDuringWysiwygRendering()
        {
            return true;
        }

        public boolean suppressMacroRenderingDuringWysiwyg()
        {
            return true;
        }

        public String getName()
        {
            return "column";
        }

        public boolean isInline()
        {
            return false;
        }

        public boolean hasBody()
        {
            return true;
        }

        public RenderMode getBodyRenderMode()
        {
            return RenderMode.ALL;
        }

        public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
        {
            String width = null;

            if (TextUtils.stringSet((String) parameters.get("width")))
            {
                width = (String) parameters.get("width");
            }
            String wysiwyg = "";
            String wysiwygInfo = "";
            if (renderContext.isRenderingForWysiwyg())
            {
                wysiwyg = "wysiwyg=\"macro:" + getName() + "\"";
                //wysiwygInfo = wysiwygConverter.getMacroInfoHtml(renderContext, getName(), 32, 0);
            }
            return "<td class=\"confluenceTd\" " + wysiwyg + " valign=\"top\"" +
                    (width != null ? " width=\"" + width + "\"" : "") +
                    ">" + wysiwygInfo +
                    body + "</td>";
        }

        public String convertXHtmlToWikiMarkup(Node previousSibling, Node node, WysiwygConverter helper, Styles styles, ListContext listContext, boolean inTable, boolean inListItem, boolean ignoreText)
        {
            String width = "";
            if (helper.getAttribute(node, "width") != null)
            {
                width = ":width=" + helper.getAttribute(node, "width");
            }
            boolean escapeWikiMarkup = true;
            return wysiwygConverter.getSep(previousSibling, "div", inTable, inListItem) + "\n{column" + width + "}\n" + helper.convertChildren(node, styles, listContext, inTable, inListItem, ignoreText, escapeWikiMarkup, null) + "\n{column}";
        }

        public void setWysiwygConverter(WysiwygConverter wysiwygConverter)
        {
            this.wysiwygConverter = wysiwygConverter;
        }
    }

    private static class SectionMacro extends BaseMacro implements WysiwygNodeConverter
    {
        private WysiwygConverter wysiwygConverter;

        public boolean suppressSurroundingTagDuringWysiwygRendering()
        {
            return true;
        }

        public boolean suppressMacroRenderingDuringWysiwyg()
        {
            return true;
        }

        public String getName()
        {
            return "section";
        }

        public boolean isInline()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean hasBody()
        {
            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public RenderMode getBodyRenderMode()
        {
            return RenderMode.ALL;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
        {
            String cssClass = " class=\"sectionMacro\"";

            if ("true".equals(parameters.get("border")))
            {
                cssClass = " class=\"sectionMacroWithBorder\"";
            }

            String wysiwyg = "";
            String wysiwygInfo = "";
            if (renderContext.isRenderingForWysiwyg())
            {
                wysiwyg = " wysiwyg=\"macro:" + getName() + "\"";
                //wysiwygInfo = wysiwygConverter.getMacroInfoHtml(renderContext, getName(), 0, 0);
            }
            return "<table" + cssClass + wysiwyg + " border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\">" + wysiwygInfo + "<tbody><tr" + wysiwyg + ">" + body + "</tr></tbody></table>";
        }

        public String convertXHtmlToWikiMarkup(Node previousSibling, Node node, WysiwygConverter helper, Styles styles, ListContext listContext, boolean inTable, boolean inListItem, boolean ignoreText)
        {
            boolean escapeWikiMarkup = true;
            if (node.getNodeName().toLowerCase().equals("table"))
            {
                return wysiwygConverter.getSep(previousSibling, "div", inTable, inListItem) + "\n{section}\n" + helper.convertChildren(node, styles, listContext, inTable, inListItem, ignoreText, escapeWikiMarkup, null) + "\n{section}";
            }
            else if (node.getNodeName().toLowerCase().equals("tr"))
            {
                return helper.convertChildren(node, styles, listContext, inTable, inListItem, ignoreText, escapeWikiMarkup, null);
            }
            else
            {
                throw new RuntimeException("unexpected handler called for tag '" + node.getNodeName() + "'");
            }
        }

        public void setWysiwygConverter(WysiwygConverter wysiwygConverter)
        {
            this.wysiwygConverter = wysiwygConverter;
        }
    }

    public void setUp() throws Exception
    {
        super.setUp();
        macroManager.registerMacro("column", new ColumnMacro());
        macroManager.registerMacro("section", new SectionMacro());
    }

    public void tearDown() throws Exception
    {
        macroManager.unregisterMacro("column");
        macroManager.unregisterMacro("section");
        super.tearDown();
    }

    public void testLayout()
    {
        testMarkup("{section}\n{column}\ntest\n{column}\n{column}\nanother test\n{column}\n{section}");
    }
}
