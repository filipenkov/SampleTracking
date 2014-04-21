package com.atlassian.renderer.wysiwyg;

import com.atlassian.renderer.*;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.macro.Macro;
import com.atlassian.renderer.v2.macro.MacroManager;
import com.atlassian.renderer.v2.macro.ResourceAwareMacroDecorator;
import com.opensymphony.util.TextUtils;
import org.w3c.dom.Node;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.html.HTMLDocument;
import org.apache.log4j.Category;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.cyberneko.html.filters.Writer;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class DefaultWysiwygConverter implements WysiwygConverter
{
    public static final String TEXT_SEPERATOR = "TEXTSEP";
    private static Category log = Category.getInstance(DefaultWysiwygConverter.class);
    private MacroManager macroManager;
    private static final Separation[] separators = new Separation[]{
            new Separation("list", "heading", "\n", null),
            new Separation("table", "table", "\n\n", null),
            new Separation("table", "div", "\n", null),
            new Separation("table", "a", "\n", null),
            new Separation("table", "p", "\n", null),
            new Separation("text", "table", "\n", null),
            new Separation("list", "p", "\n\n", null),
            new Separation("blockquote", "p", "\n", null),
            new Separation("p", "list", "\n", "\n"),
            new Separation("p", "table", "\n", null),
            new Separation("p", "text", "\n\n", null),
            new Separation("heading", "table", "\n", null),
            new Separation("heading", "div", "\n", "", ""),
            new Separation("heading", "span", "\n", null),
            new Separation("heading", "text", "\n", null),
            new Separation("heading", "heading", "\n", null),
            new Separation("heading", "p", "\n", null),
            new Separation("p", "p", "\n\n", "\\\\\n", "\\\\\n"),
            new Separation("tr", "tr", "\n", "\n"),
            new Separation("list", "list", "\n\n", null),
            new Separation("heading", "list", "\n", null),
            new Separation("text", "list", "\n", "\n"),
            new Separation("a", "list", "\n", "\n"),
            new Separation("br", "list", "", ""),
            new Separation("div", "list", "\n", "\n"),
            new Separation("div", "table", "\n", "\n"),
            new Separation("div", "imagelink", "\n", "\n"),
            new Separation("div", "text", "\n", "\n"),
            new Separation("div", "p", "\n", null),
            new Separation("list", "div", "\n\n", null),
            new Separation("list", "a", "\n\n", null),
            new Separation("list", "text", "\n\n", "\n"), // when text follows a list in a table we can't correctly make
            // it  a new paragraph, we can only make it part of the last list item.
            new Separation("list", "table", "\n\n", null),
            new Separation("list", "br", "\n", null),
            new Separation("forcedNewline", "list", "\n", "\n"),
            new Separation("forcedNewline", "li", "", ""),
            new Separation("forcedNewline", "text", "\n", "\n"),
            new Separation("forcedNewline", "forcedNewline", "\n", "\n"),
            new Separation("forcedNewline", "a", "\n", "\n"),
            new Separation("forcedNewline", "div", "\n", "\n"),
            new Separation("text", "forcedNewline", "\n", TEXT_SEPERATOR),
            new Separation("table", "forcedNewline", "\n", TEXT_SEPERATOR),
            new Separation("table", "text", "\n", null),
            new Separation("table", "list", "\n", null),
            new Separation("pre", "text", "\n", null),
            new Separation("pre", "pre", "\n", null),
            new Separation("li", "li", "\n", "\n"),
            new Separation("li", "list", "\n", "\n"),
            new Separation("list", "li", "\n", "\n"),
            new Separation("span", "heading", "\n", "", ""),
            new Separation("text", "heading", "\n", "", ""),
            new Separation("p", "heading", "\n", "", ""),
            new Separation("p", "div", "\n", "", "\n"),
            new Separation("text", "div", "\n", "\n", "\n"),
            new Separation("text", "p", "\n", null, null),
            new Separation("span", "div", "\n", "\n", "\n"),
            new Separation("span", "p", "\n", null, null),
            new Separation("table", "heading", "\n", "", ""),
            new Separation("div", "heading", "\n", "", ""),
            new Separation("div", "div", "\n", "", "\n"),
            new Separation("hr", "imagelink", "\n", "\n", "\n"),
            new Separation("hr", "div", "\n", "\n", "\n"),
            new Separation("hr", "list", "\n", "\n", "\n"),
            new Separation("hr", "table", "\n", "\n", "\n"),
            new Separation("hr", "p", "\n", null, null),
            new Separation("hr", "text", "\n", null, null),
            new Separation("font", "list", "\n", "\n", "\n"),
            new Separation("emoticon", "text", " ", "\n"),
    };

    private Map sepMap = null;
    protected static boolean debug = false;
    private IconManager iconManager;
    protected WikiStyleRenderer renderer;
    /**
     * This contains a set of macro names which we don't add to the markup, because they are handled in other ways.
     */
    protected HashSet macrosToIgnore = new HashSet();
    /**
     * Escape any characters which would be interpreted as markup
     */
    private static String[] escapedCharactersRegex = new String[]{"(\\\\)?\\[", "(\\\\)?\\]", "([^\\s])([\\*\\-\\+\\^~_](?:[\\s]|$))", "((?:^|[\\s]))([\\*\\-\\+\\^~_][^\\s])",
            "(\\\\)?!", /*"\\{", "\\}",*/ "(\\\\)?\\|", "(^\\s*)([\\*\\#\\-])",};
    private static String[] escapedCharactersReplacement = new String[]{"\\\\[", "\\\\]",
            "$1\\\\$2", "$1\\\\$2", "\\\\!", /*"\\\\{", "\\\\}",*/ "\\\\|", "$1\\\\$2"};

    public DefaultWysiwygConverter()
    {
        macrosToIgnore.add("color");
        sepMap = new HashMap();
        for (int i = 0; i < separators.length; ++i)
        {
            sepMap.put(separators[i], separators[i]);
        }
    }

    public void setWikiStyleRenderer(WikiStyleRenderer renderer)
    {
        this.renderer = renderer;
    }

    public void setMacroManager(MacroManager macroManager)
    {
        this.macroManager = macroManager;
    }

    public String getSep(Node previous, String current, boolean inTable, boolean inList)
    {
        String prevType;
        if (previous == null)
        {
            prevType = null;
        }
        else if (isTextNode(previous) || isWikiSrcDiv(previous))
        {
            prevType = "text";
        }
        else if (isForcedNewline(previous))
        {
            prevType = "forcedNewline";
        }
        else if (isList(previous))
        {
            prevType = "list";
        }
        else
        {
            prevType = previous.getNodeName().toLowerCase();
            if (prevType.equals("span") || prevType.equals("b") || prevType.equals("em") || prevType.equals("del") || prevType.equals("ins") || prevType.equals("sub") || prevType.equals("sup"))
            {
                prevType = "text";
            }
            else if (isHeading(prevType))
            {
                prevType = "heading";
            }
            else if(isEmoticon(previous, prevType))
            {
                prevType = "emoticon";
            }
        }
        String debugStr1 = "";
        String debugStr2 = "";
        if (debug)
        {
            debugStr1 = "[" + prevType + "-" + current;
            debugStr2 = inTable + "," + inList + "]";
        }
        Separation s = (Separation) sepMap.get(new Separation(prevType, current, null, null));
        if (s == null)
        {
            return debugStr1 + debugStr2;
        }
        else
        {
            String sep = inTable ? s.getTableSeparator() : (inList ? s.getListSeparator() : s.getSeparator());
            if (sep == null)
            {
                return debugStr1 + debugStr2;
            }
            return debugStr1 + sep + debugStr2;
        }
    }

    private boolean isEmoticon(Node node, String nodeName) {
        if(nodeName.equals("img") && getAttribute(node, "src") != null)
        {
            String src = node.getAttributes().getNamedItem("src").getNodeValue();
            if (src.indexOf("/images/icons/emoticons/") != -1)
                return true;
        }
        return false;
    }

    private boolean isWikiSrcDiv(Node node)
    {
        return getAttribute(node, "class") != null && getAttribute(node, "class").indexOf(RenderUtils.WIKI_SRC_CLASS) != -1;
    }

    private static boolean isTextNode(Node node)
    {
        return node.getNodeType() == Node.TEXT_NODE;
    }

    private static boolean isList(Node node)
    {
        return node.getNodeName().toLowerCase().equals("ol") || node.getNodeName().toLowerCase().equals("ul");
    }

    /**
     * determine whether we need to start a new list (CONF-7085)
     * @param parentNode - the parent node of the 'ul' node
     */
    private boolean isNewList(Node parentNode)
    {
        if(parentNode==null)
            return false;
        String parentNodeName = parentNode.getNodeName().toLowerCase();
        //start new list as long as the parent is not a list item and is not a list (ul or ol)
        if(!parentNodeName.equals("li") && !parentNodeName.equals("ul") && !parentNodeName.equals("ol"))
            return true;
        else
        {
            //exception - start new list if we have a wikisrcdiv inside a list item
            for(int i=0; i<parentNode.getChildNodes().getLength(); ++i)
            {
                Node n = parentNode.getChildNodes().item(i);
                if(n.getNodeName().toLowerCase().equals("div") && isWikiSrcDiv(n))
                    return true;
            }
        }
        return false;
    }

    public String convertChildren(Node node, Styles styles, ListContext listContext, boolean inTable, boolean inListItem, boolean ignoreText, boolean escapeWikiMarkup, Node previousSibling)
    {
        StringBuffer wikiText = new StringBuffer();
        //Node previousSibling = null;
        if (node != null && node.getChildNodes() != null)
        {
            for (int i = 0; i < node.getChildNodes().getLength(); ++i)
            {
                Node n = node.getChildNodes().item(i);
                String converted = convertNode(n, previousSibling, styles, listContext, inTable, inListItem, ignoreText, escapeWikiMarkup);
                if (converted != null)
                {
                    wikiText.append(converted);
                    previousSibling = n;
                }
            }
        }
        return wikiText.toString();
    }

    private boolean isTextNodeContainingWhitespaceOnly(Node n)
    {
        if (isTextNode(n))
        {
            String s = n.getNodeValue();
            s = s.replaceAll("(\n|\r|\t)+ *", "");
            return s.length() == 0;
        }
        return false;
    }

    // TODO: Get rid of this beast of a method. 361 lines, baby!
    private String convertNode(Node node, Node previousSibling, Styles styles, ListContext listContext, boolean inTable, boolean inListItem, boolean ignoreText, boolean escapeWikiMarkup)
    {

        if (isCommentNode(node))
        {
            return null;
        }
        if (isProcessingInstructionNode(node))
        {
            String s = convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            return s;
        }
        if (isDocumentFragmentNode(node))
        {
            String s = convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            return s;
        }
        if (isTextNode(node))
        {
            if (ignoreText)
            {
                return null;
            }
            String s = node.getNodeValue();
            s = s.replaceAll("^\n", "");
            s = s.replaceAll("\n$", "");
            s = s.replaceAll("(\n|\r)", " ");
            if (isTextNodeContainingWhitespaceOnly(node))
            {
                return null;
            }
            if (s.trim().equals(""))
            {
                // blank text should never cause a newline
                return " ";
            }
            StringBuffer sb = new StringBuffer();
            if (s.startsWith(" "))
            {
                sb.append(TEXT_SEPERATOR);
            }
            String sTrimmed = s.trim();
            //if (escapeWikiMarkup)
            {
                sTrimmed = escapeWikiMarkup(sTrimmed);
            }

            sb.append(styles.decorateText(sTrimmed));
            if (!s.equals(" ") && s.endsWith(" "))
            {
                sb.append(TEXT_SEPERATOR);
            }
            return getSep(previousSibling, "text", inTable, inListItem) + replaceEntities(sb.toString());
        }
        String name = node.getNodeName().toLowerCase();
        if (name == null)
        {
                log.error("Node with null name, of class " + node.getClass());
                return null;
        }
        else
        {
            // check if this node should be handled by special-purpose processing
            if (getAttribute(node, "wysiwyg") != null)
            {
                String converterName = getAttribute(node, "wysiwyg");
                if (converterName.equals("ignore"))
                {
                    return "";
                }
                else
                {
                    WysiwygNodeConverter c = findNodeConverter(converterName);
                    return c.convertXHtmlToWikiMarkup(previousSibling, node, this, styles, listContext, inTable, inListItem, false);
                }
            }
            else if (name.equals("br"))
            {
                if (isForcedNewline(node))
                {
                    return getSep(previousSibling, "forcedNewline", inTable, inListItem) + "\\\\ ";
                }
                return getSep(previousSibling, "br", inTable, inListItem) + (inTable ? "\\\\\n " : "\n");
            }
            else if (name.equals("p"))
            {
                String children = convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
                if (children.trim().equals(""))
                {
                    return null;
                }
//                else if ("atl_conf_pad".equals(getAttribute(node, "class")) && children.trim().equals("&nbsp;"))
//                {
//                    return null;
//                }
                else if (children.trim().equals("&nbsp;"))
                {
                    return getSep(previousSibling, "forcedNewline", inTable, inListItem) + "\\\\ ";
                }
                String s = getSep(previousSibling, "p", inTable, inListItem) + children;
                return s;
            }
            else if (name.equals("span"))
            {
                if (getAttribute(node, "class") != null && getAttribute(node, "class").indexOf("macro") != -1)
                {
                    if (getAttribute(node, "macrotext") != null)
                    {
                        String macroText = getAttribute(node, "macrotext");
                        String command = getAttribute(node, "command");
                        String childText = convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
                        if (macrosToIgnore.contains(command))
                        {
                            return getSep(previousSibling, "text", inTable, inListItem) + childText;
                        }
                        else
                        {
                            String s = getSep(previousSibling, "text", inTable, inListItem) + macroText + childText + "{" + command + "}";
                            return s;
                        }
                    }
                    else
                    {
                        // unknown in-line macro
                        return getSep(previousSibling, "text", inTable, inListItem) + getRawChildText(node, true);
                    }
                }
                else if (getAttribute(node, "class") != null && getAttribute(node, "class").indexOf(RenderUtils.WIKI_SRC_CLASS) != -1)
                {
                    // this span contains wiki text which was not understood, which should be preserved
                    return getRawChildText(node, false);
                }
                else if (getAttribute(node, "class") != null && getAttribute(node, "class").indexOf("error") != -1)
                {
                    // this span contains error text which must be discarded
                    return "";
                }

                // this is a span which we are ignoring
                return convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, previousSibling);
            }
            else if (name.equals("font"))
            {
                // FCK on IE uses font for colours, instead of a span and a style
                return convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("b") || name.equals("strong"))
            {
                Styles st = new Styles(node, styles);
                return getSep(previousSibling, "text", inTable, inListItem) + convertChildren(node, new Styles(Styles.BOLD, st), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("i") || name.equals("em"))
            {
                Styles st = new Styles(node, styles);
                return getSep(previousSibling, "text", inTable, inListItem) + convertChildren(node, new Styles(Styles.ITALIC, st), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("del") || name.equals("strike"))
            {
                Styles st = new Styles(node, styles);
                return getSep(previousSibling, "text", inTable, inListItem) + convertChildren(node, new Styles(Styles.STRIKETHROUGH, st), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("ins") || name.equals("u"))
            {
                Styles st = new Styles(node, styles);
                return getSep(previousSibling, "text", inTable, inListItem) + convertChildren(node, new Styles(Styles.UNDERLINE, st), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("sub"))
            {
                Styles st = new Styles(node, styles);
                return getSep(previousSibling, "text", inTable, inListItem) + convertChildren(node, new Styles(Styles.SUBSCRIPT, st), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("sup"))
            {
                Styles st = new Styles(node, styles);
                return getSep(previousSibling, "text", inTable, inListItem) + convertChildren(node, new Styles(Styles.SUPERSCRIPT, st), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("cite"))
            {
                Styles st = new Styles(node, styles);
                return getSep(previousSibling, "text", inTable, inListItem) + convertChildren(node, new Styles(Styles.CITE, st), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
            }
            else if (name.equals("ol"))
            {
                String s = getSep(previousSibling, "list", inTable, inListItem) + convertChildren(node, styles, new ListContext(ListContext.NUMBERED, listContext), inTable, inListItem, true, escapeWikiMarkup, null).trim();
                return s;
            }
            else if (name.equals("ul"))
            {
                String bulletType = ListContext.BULLETED;
                String typeAttribute = getAttribute(node, "type");
                if (typeAttribute != null && typeAttribute.equals("square"))
                {
                    bulletType = ListContext.SQUARE;
                }

                if(isNewList(node.getParentNode()))
                    listContext = new ListContext(bulletType);
                else
                    listContext = new ListContext(bulletType, listContext);

                String s = getSep(previousSibling, "list", inTable, inListItem) + convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, true, escapeWikiMarkup, null);
                return s;
            }
            else if (name.equals("li"))
            {
                String s = trimNewlines(convertChildren(node, new Styles(node, styles), listContext, inTable, true, false, escapeWikiMarkup, null));
                // if the list item was empty, or contained just a <br>, put an &nbsp; placeholder in it
                if (s.equals("") || s.trim().equals("\\\\"))
                {
                    s = "&nbsp;";
                }
                // fix for CONF-5210, problem 2, <li> with a <br/> at the end
                while (s.endsWith("\n"))
                {
                    s = s.substring(0, s.length() - 1);
                }
                // fix for CONF-5492, <li> with a <br> at the end which becomes "\\\\\n "
                while (s.endsWith("\\\\\n "))
                {
                    s = s.substring(0, s.length() - 4);
                }
                // if the only contents of this list item is a nested list, don't create a line for it
                if (s.trim().startsWith("##") || s.trim().startsWith("**"))
                {
                    return getSep(previousSibling, "li", inTable, inListItem) + s;
                }
                return getSep(previousSibling, "li", inTable, inListItem) + listContext.decorateText(s);
            }
            else if (name.equals("table"))
            {
                return getSep(previousSibling, "table", inTable, inListItem) + convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, true, escapeWikiMarkup, null);
            }
            else if (name.equals("tbody"))
            {
                return convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, true, escapeWikiMarkup, null);
            }
            else if (name.equals("tr"))
            {
                String s = convertChildren(node, new Styles(node, styles), listContext, true, inListItem, true, escapeWikiMarkup, null);
                if (s.length() > 0)
                {
                    if (s.startsWith("||"))
                    {
                        s = s + "||";
                    }
                    else
                    {
                        s = s + "|";
                    }
                    return getSep(previousSibling, "tr", inTable, inListItem) + s;
                }
                else
                {
                    return "";
                }
            }
            else if (name.equals("th"))
            {
                String s = convertChildren(node, new Styles(node, styles), listContext, true, inListItem, false, escapeWikiMarkup, null);
                s = trimTableCell(s);
                return "||" + s;
            }
            else if (name.equals("td"))
            {
                String s = convertChildren(node, new Styles(node, styles), listContext, true, inListItem, false, escapeWikiMarkup, null);
                s = trimTableCell(s);
                return "|" + s;
            }
            else if (name.equals("div"))
            {
                if (getAttribute(node, "class") != null && getAttribute(node, "class").indexOf("error") != -1)
                {
                    // this div contains error text which must be discarded
                    return "";
                }
                else if (getAttribute(node, "class") != null && getAttribute(node, "class").indexOf("macro") != -1)
                {
                    if (getAttribute(node, "macrotext") != null)
                    {
                        String macroText = getAttribute(node, "macrotext");
                        String command = getAttribute(node, "command");
                        String childText = convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
                        return getSep(previousSibling, "div", inTable, inListItem) + macroText + "\n" + trimNewlinesAndEscapedNewlines(childText) + "\n" + "{" + command + "}";
                    }
                    else
                    {
                        return getSep(previousSibling, "div", inTable, inListItem) + getRawChildText(node, true);
                    }
                }
                else if (isWikiSrcDiv(node))
                {
                    // this div contains wiki text which should be preserved
                    String s = getRawChildText(node, false);
                    return s;
                }
                else
                {
                    // miscellaneous formatting divs
                    // we are ignoring this div, so pass the current previousSibling as the initial previousSIbling of the contained block.
                    return convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, previousSibling);
                }
            }
            else if (isHeading(name))
            {
                String digit = name.substring(1);
                return "\n" + getSep(previousSibling, "heading", inTable, inListItem) + "h" + digit + ". " + convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null) + "\n";
            }
            else if (name.equals("img"))
            {
                if (getAttribute(node, "imagetext") != null)
                {
                    String imagetext = getAttribute(node, "imagetext");

                    // add image size params
                    if(getAttribute(node, "width") != null)
                    {
                        String params = "";
                        if(imagetext.indexOf("width=")!=-1)
                            imagetext= imagetext.replaceAll("width=\\d*", "width=" + getAttribute(node, "width"));
                        else
                            params += "width=" + getAttribute(node, "width");

                        if(getAttribute(node, "height") != null)
                        {
                            if(imagetext.indexOf("height=")!=-1)
                                imagetext = imagetext.replaceAll("height=\\d*", "height=" + getAttribute(node, "height"));
                            else
                                params += ",height=" + getAttribute(node, "height");
                        }
                        if(TextUtils.stringSet(params))
                        {
                            if(imagetext.indexOf("|")==-1)
                                imagetext += "|" + params;
                            else
                                imagetext += "," + params;
                        }
                    }

                    return getSep(previousSibling, "imagelink", inTable, inListItem) + TEXT_SEPERATOR + "!" + imagetext + "!";
                }
                if (getAttribute(node, "src") != null)
                {
                    String src = node.getAttributes().getNamedItem("src").getNodeValue();
                    if (src.indexOf("/images/icons/emoticons/") != -1)
                    {
                        return lookupEmoticonString(src.substring(src.indexOf("icons/emoticons/")));
                    }
                }
                return "";
            }
            else if (name.equals("a"))
            {
                if (getAttribute(node, "linktype") != null)
                {
                    // if the link is an error link, then the markup is simply the linktext surrounded by square brackets
                    if ("linkerror".equals(getAttribute(node, "class")))
                    {
                        return "[" + getAttribute(node, "linktext") + "]";
                    }
                    // CONF-4820: decorate links, so that markup is preserved
                    String alias = convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, false, previousSibling);
                    String linkWikiText = WysiwygLinkHelper.createLinkWikiText(node, alias, getRawChildText(node, true));
                    return getSep(previousSibling, "a", inTable, inListItem)
                        + styles.decorateText(linkWikiText);
                }
                else
                {
                    return getRawChildText(node, true);
                }
            }
            else if (name.equals("pre") || name.equals("textarea"))
            {
                String s = getSep(previousSibling, "pre", inTable, inListItem) + getRawChildText(node, false);
                return s;
            }
            else if (name.equals("hr"))
            {
                return "\n\n----";
            }
            else if (name.equals("link"))
            {
                // this is our stylesheet, ignore it
                return "";
            }
            else if (name.equals("blockquote"))
            {
                if (getAttribute(node, "markup") != null)
                {
                    // this comes from 'bq.' markup
                    // the body is contained in a <p> which we want to ignore
                    int childLength = node.getChildNodes().getLength();
                    if (childLength == 1)
                    {
                        node = node.getChildNodes().item(0);
                    }
                    String s = convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
                    return "\n" + "bq. " + s;
                }
                else
                {
                    // comes from a {quote} macro, already handled by an enclosing div
                    return convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null);
                }
            }
            else if (name.equals("tt"))
            {
                return "{{{}" + convertChildren(node, new Styles(node, styles), listContext, inTable, inListItem, false, escapeWikiMarkup, null) + "{}}}";
            }
            else
            {
                return getRawChildText(node, true);
            }
        }
    }

    private String trimNewlinesAndEscapedNewlines(String s)
    {
        if (s.startsWith("\n"))
        {
            s = s.substring(1);
        }
        if (s.endsWith("\n"))
        {
            s = s.substring(0, s.length() - 1);
        }
        if (s.startsWith("\\\\\n"))
        {
            s = s.substring(3);
        }
        if (s.endsWith("\\\\"))
        {
            s = s.substring(0, s.length() - 2);
        }

        return s;
    }

    private boolean isDocumentFragmentNode(Node node)
    {
        return node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE;
    }

    private boolean isProcessingInstructionNode(Node node)
    {
        return node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE;
    }

    private boolean isCommentNode(Node node)
    {
        return node.getNodeType() == Node.COMMENT_NODE;
    }

    private String trimWhitespace(String s)
    {
        return s.replace((char) 160, (char) 32).trim();
    }

    private String trimNewlines(String s)
    {
        if (s.startsWith("\n"))
        {
            s = s.replaceAll("^\n*(.*?)", "$1");
        }
        return s;
    }

    private WysiwygNodeConverter findNodeConverter(String converterName)
    {
        if (converterName.startsWith("macro:"))
        {
            String[] parts = converterName.split(":");
            if (parts.length != 2)
            {
                throw new RuntimeException("Illegal node converter name:'" + converterName + "'");
            }
            Macro m = macroManager.getEnabledMacro(parts[1]);
            if (m instanceof ResourceAwareMacroDecorator)
            {
                m = ((ResourceAwareMacroDecorator) m).getMacro();
            }
            if (!(m instanceof WysiwygNodeConverter))
            {
                throw new RuntimeException("Macro '" + parts[1] + "' implemented by " + m.getClass() + " does not implement WysiwygNodeConverter.");
            }
            return (WysiwygNodeConverter) m;
        }
        else
        {
            throw new RuntimeException("Unrecognized node converter name:'" + converterName + "'");
        }
    }

    public String getAttribute(Node node, String name)
    {
        NamedNodeMap map = node.getAttributes();
        if (map == null)
        {
            // this should never happen. Comment nodes have no attributes, but they should be filtered out
            // before getAttributes() is called
            log.error("Node " + node + " has null attribute map.");
            return null;
        }
        else
        {
            Node n = node.getAttributes().getNamedItem(name);
            return n != null ? n.getNodeValue() : null;
        }
    }

    private static boolean isHeading(String name)
    {
        return name.startsWith("h") && name.length() == 2 && Character.isDigit(name.charAt(1));
    }

    private static boolean isForcedNewline(Node node)
    {
        return node != null && node.getNodeName() != null && node.getNodeName().toLowerCase().equals("br") && node.getAttributes() != null &&  node.getAttributes().getNamedItem("clear") != null;
    }

    /**
     * Map of emoticon file names to emoticon symbols
     */
    private Map/*<String,String>*/ emoticonMap = null;

    private synchronized void initEmoticonMap()
    {
        emoticonMap = new HashMap();
        List symbolList = new ArrayList(Arrays.asList(iconManager.getEmoticonSymbols()));
        // sort this so that the result is deterministic
        Collections.sort(symbolList);
        for (Iterator i = symbolList.iterator(); i.hasNext(); )
        {
            String symbol = (String) i.next();
            Icon icon = iconManager.getEmoticon(symbol);
            emoticonMap.put(icon.getPath(), symbol);
        }
    }

    private String lookupEmoticonString(String fileName)
    {
        if (emoticonMap == null)
        {
            initEmoticonMap();
        }
        String symbol = (String) emoticonMap.get(fileName);
        if (symbol == null)
        {
            throw new RuntimeException("unrecognised emoticon " + fileName);
        }
        else
        {
            return symbol;
        }
    }

    private String replaceEntities(String s)
    {
        return s.replaceAll("\\u2014", "---").replaceAll("\\u2013", "--").replaceAll("\\u00A0", "&nbsp;");
    }

    /**
     * Return the text content of a node, adding newlines for &lt;br&gt; and &lt;p&gt; tags
     * @param node
     * @param stripNewlines if this is true then strip any newlines from the raw text, but still add newlines for br and p.
     * @return
     */
    private String getRawChildText(Node node, boolean stripNewlines)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < node.getChildNodes().getLength(); ++i)
        {
            Node n = node.getChildNodes().item(i);
            if (isTextNode(n))
            {
                String s = n.getNodeValue();
                if (stripNewlines)
                {
                    s = s.replaceAll("(\n|\r)", " ").trim();
                }
                sb.append(s);
            }
            else if (getNodeName(n).equals("br"))
            {
                sb.append("\n");
            }
            sb.append(getRawChildText(n, stripNewlines));
            if (getNodeName(n).equals("p"))
            {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String getNodeName(Node node)
    {
        return node.getNodeName().toLowerCase();
    }

    private String trimTableCell(String s)
    {
        String trimmed = trimWhitespace(s);
        trimmed = trimmed.replaceAll("^&nbsp;","");
        trimmed = trimmed.replaceAll("&nbsp;$","");
        if (trimmed.equals("&nbsp;"))
        {
            s = " ";
        }
        if (trimmed.equals(""))
        {
            s = " ";
        }
        else
        {
            s = " " + trimmed + " ";
        }
        return s;
    }

    private String escapeWikiMarkup(String s)
    {
        if(!debug)
        {
            for (int i = 0; i < escapedCharactersRegex.length; ++i)
            {
                s = s.replaceAll(escapedCharactersRegex[i], escapedCharactersReplacement[i]);
            }
        }
        return s;
    }

    public void setIconManager(IconManager iconManager)
    {
        this.iconManager = iconManager;
    }

    public String getMacroInfoHtml(RenderContext context, String name, int xOffset, int yOffset)
    {
        return "<img alt=\"" + name + "\" style=\"float:left;margin-right:-32;opacity:0.75;position:relative;left:" + xOffset + "px;top:" + yOffset + "px;\" src=\"" + context.getSiteRoot() + "/includes/js/editor/plugins/confluence/info.png\"/>";
    }

    public String convertXHtmlToWikiMarkup(String xhtml)
    {
        if (!TextUtils.stringSet(xhtml))
        {
            return "";
        }
        try
        {
            xhtml = xhtml.replaceAll("\r", "");
            xhtml = xhtml.replaceAll("\\u2009", "");
            xhtml = xhtml.replaceAll("&#8201;", "");
            
            DOMFragmentParser parser = new DOMFragmentParser();

            HTMLDocument document = new HTMLDocumentImpl();
            DocumentFragment fragment = document.createDocumentFragment();
            // NekoHTML doesn't seem to properly handle processing instructions created by MS Word pasted into IE -- a ProcessingInstructionImpl
            // node is created, but following HTML is ignored. So we strip the processing instruction first:
            xhtml = xhtml.replaceAll("<\\?xml.*?/>","");
            InputSource inputSource = new InputSource(new StringReader(xhtml));
            try
            {
                parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
                if(debug)
                {
                    parser.setProperty("http://cyberneko.org/html/properties/filters", new XMLDocumentFilter[] {new Writer()});
                }
                parser.parse(inputSource, fragment);
            } catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
            StringBuffer wikiText = new StringBuffer();

            wikiText.append(convertNode(fragment, null, new Styles(), new ListContext(), false, false, false, true));
            // fix consecutive newlines and other white space
            // Strip out trailing whitespace before newlines, "     \n" (Modified to fix CONF-4490)
            if (debug)
            {
                return wikiText.toString();
            }
            else
            {
                String s = wikiText.toString().replaceAll("[\\s&&[^\n]]*\n", "\n").trim();

                s = s.replaceAll(" (" + TEXT_SEPERATOR + ")+", " ");
                s = s.replaceAll("\n(" + TEXT_SEPERATOR + ")+", "\n");
                s = s.replaceAll("^(" + TEXT_SEPERATOR + ")+", "");
                s = s.replaceAll("\\[(" + TEXT_SEPERATOR + ")+", "[");
                s = s.replaceAll("(" + TEXT_SEPERATOR + ")+ ", " ");
                s = s.replaceAll("(" + TEXT_SEPERATOR + ")+", " ");
                s = s.replaceAll(" \n", "\n");
                // fix phrase style markers, making them simple ('*') instead of complex ('{*}') where possible.
                s = s.replaceAll(VALID_START + PHRASE_CLEANUP_REGEX, "$1");
                s = s.replaceAll(PHRASE_CLEANUP_REGEX + VALID_END, "$1");

                return s;
            }
        }
        catch (FactoryConfigurationError e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static final String VALID_START = "(?<![}\\p{L}\\p{Nd}\\\\])";
    public static final String VALID_END = "(?![{\\p{L}\\p{Nd}])";

    private static final String PHRASE_CLEANUP_REGEX = "\\{((?:\\?\\?)|(?:\\*)|(?:\\^)|(?:~)|(?:_)|(?:-)|(?:\\+)|(?:\\{\\{)|(?:\\}\\}))\\}";

    public String convertWikiMarkupToXHtml(RenderContext ctx, String wikiMarkup)
    {
        ctx.setRenderingForWysiwyg(true);
        String s = renderer.convertWikiToXHtml(ctx, wikiMarkup);
        // now we remove redundant padding
        s = s.replaceAll("<p class=\"atl_conf_pad\">&#8201;</p>\\s*<p","<p");
        return s;
    }

    private static class Separation
    {
        private final String previous;
        private final String current;

        public String getSeparator()
        {
            return separator;
        }

        public String getTableSeparator()
        {
            return tableSeparator;
        }

        public String getListSeparator()
        {
            return listSeparator;
        }

        private final String separator;
        private final String tableSeparator;
        private final String listSeparator;

        public Separation(String previous, String current, String separator, String tableSeparator, String listSeparator)
        {

            this.previous = previous;
            this.current = current;
            this.separator = separator;
            this.tableSeparator = tableSeparator;
            this.listSeparator = listSeparator;
        }

        public Separation(String previous, String current, String separator, String tableSeparator)
        {
            this.previous = previous;
            this.current = current;
            this.separator = separator;
            this.tableSeparator = tableSeparator;
            this.listSeparator = separator;
        }

        public boolean equals(Object obj)
        {
            if (obj instanceof DefaultWysiwygConverter.Separation)
            {
                DefaultWysiwygConverter.Separation s = (DefaultWysiwygConverter.Separation) obj;
                return ((previous == null && s.previous == null) || previous.equals(s.previous)) &&
                        ((current == null && s.current == null) || current.equals(s.current));
            }
            return false;
        }

        public int hashCode()
        {
            return 43 * (previous == null ? 1 : previous.hashCode()) + (current == null ? 1 : current.hashCode());
        }
    }

    private static void print(Node node, String indent) {
        System.out.println(indent+node.getClass().getName());
        Node child = node.getFirstChild();
        while (child != null) {
            print(child, indent+" ");
            child = child.getNextSibling();
        }
    }
}
