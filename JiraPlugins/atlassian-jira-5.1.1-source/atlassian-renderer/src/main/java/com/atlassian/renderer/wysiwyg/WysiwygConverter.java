package com.atlassian.renderer.wysiwyg;

import org.w3c.dom.Node;
import com.atlassian.renderer.RenderContext;

public interface WysiwygConverter
{
    public String convertChildren(Node node, Styles styles, ListContext listContext, boolean inTable, boolean inListItem, boolean ignoreText, boolean escapeWikiMarkup, Node overridePreviousSibling);
    public String getAttribute(Node node, String name);
    public String convertXHtmlToWikiMarkup(String xhtml);
    public String convertWikiMarkupToXHtml(RenderContext ctx, String wikiMarkup);
    public String getMacroInfoHtml(RenderContext context, String name, int xOffset, int yOffset);
    public String getSep(Node previous, String next, boolean inTable, boolean inList);
}
