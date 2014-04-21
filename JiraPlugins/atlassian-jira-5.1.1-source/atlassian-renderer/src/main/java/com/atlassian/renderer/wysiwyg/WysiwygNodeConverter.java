package com.atlassian.renderer.wysiwyg;

import org.w3c.dom.Node;

public interface WysiwygNodeConverter
{
    String convertXHtmlToWikiMarkup(Node previousSibling, Node node, WysiwygConverter helper,  Styles styles, ListContext listContext, boolean inTable, boolean inListItem, boolean ignoreText);
}
