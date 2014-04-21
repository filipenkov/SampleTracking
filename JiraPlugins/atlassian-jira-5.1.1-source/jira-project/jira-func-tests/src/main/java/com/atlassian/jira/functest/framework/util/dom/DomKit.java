package com.atlassian.jira.functest.framework.util.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

/**
 * This is some methods to manipulate the DOM and text in it
 *
 * @since v3.13
 */
public class DomKit
{

    /**
     * ThreadLocal cache of non validating <code>DocumentBuilder</code> instances.
     */
    private static final ThreadLocal nonvalidatingDocumentBuilders = new ThreadLocal()
    {

        /** @see java.lang.ThreadLocal#initialValue() */
        protected Object initialValue()
        {
            return buildDocumentBuilder();
        }
    };

    /**
     * Called to return a non validating <code>DocumentBuilder</code>
     */
    private static DocumentBuilder buildDocumentBuilder()
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.valueOf(false));
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder;
        }
        catch (ParserConfigurationException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieves a thread-specific non validating <code>DocumentBuilder</code>.  We do this because they are expensive to build.
     *
     * @return the <code>DocumentBuilder</code> serving the current thread.
     */
    public static DocumentBuilder getNonValidatingDocumentBuilder()
    {
        return (DocumentBuilder) nonvalidatingDocumentBuilders.get();
    }

    /**
     * Returns the first HTML element of the Document or null if there isnt one
     *
     * @param document the Document to search
     * @return an Element of tag name body
     */
    public static Element getHtmlElement(Document document)
    {
        return getFirstElementByTag(document, "HTML");
    }

    /**
     * Returns the first BODY element of the Document or null if there isnt one
     *
     * @param document the Document to search
     * @return an Element of tag name body
     */
    public static Element getBodyElement(Document document)
    {
        return getFirstElementByTag(document, "BODY");
    }

    /**
     * @param element the element to start searching from
     * @param parentTagName the tag name of the parent to retrieve
     * @return the first parent element of this tag name, or null if not found
     */
    public static Element getFirstParentByTag(Element element, String parentTagName)
    {
        Element el = (Element) element.getParentNode();
        while (el != null)
        {
            if (el.getTagName().equalsIgnoreCase(parentTagName))
            {
                return el;
            }
            el = (Element) el.getParentNode();
        }
        return null;
    }

    static Element getFirstElementByTag(Document document, String tagName)
    {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0)
        {
            return (Element) nodeList.item(0);
        }
        nodeList = document.getElementsByTagName(tagName.toLowerCase());
        if (nodeList.getLength() > 0)
        {
            return (Element) nodeList.item(0);
        }
        return null;
    }

    /**
     * Gets all the text nodes of an Element.  Does a deep walk of the node tree.
     *
     * @param element the Element in play
     * @return a non null array of TextNodes
     */
    public static Text[] getTextNodes(Element element)
    {
        List textNodeList = new ArrayList();
        walkDOMAndGatherTextNodes(element, textNodeList);
        return (Text[]) textNodeList.toArray(new Text[textNodeList.size()]);
    }


    /**
     * Returns the collapsed text of the specified Node.  The collapsing of text text is done in the following way :
     * <ul>
     * <li>First find all the child text nodes of the Node (deeply)</li>
     * <li>If they begin with white space, collapse that into one space</li>
     * <li>If they end with white space, collapse that into one space</li>
     * <li>Append them together as one string, collapsing any white space into a single white while appending.</li>
     * <li>Any white space  at the front and back of the final string is removed.</li>
     * </ul>
     * <p/>
     * So if ^ represent spaces then the string "^^<b>^^some^bold%^text</b>^and^<span>some</span>other^text"
     * will become "some bold text and someother text".
     *
     * @param node the Node to search
     * @return the collapsed text as a String
     */
    public static String getCollapsedText(Node node)
    {
        List textList = new ArrayList();
        walkDOMAndGatherText(node, textList);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < textList.size(); i++)
        {
            String text = (String) textList.get(i);
            text = canonicalTextNode(text);
            smooshTogether(sb, text);
        }
        // now we dont want an preceding and trailing text here
        return sb.toString().trim();
    }

    /**
     * This returns a deep search of the raw text of a specified node.
     *
     * @param node the {@link org.w3c.dom.Node} in question
     * @return the raw text of the noed with no trim processing done.
     */
    public static String getRawText(Node node)
    {
        List textList = new ArrayList();
        walkDOMAndGatherText(node, textList);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < textList.size(); i++)
        {
            String text = (String) textList.get(i);
            sb.append(text);
        }
        return sb.toString();
    }

    /**
     * This will append the string to the end of stringbuffer, respecting the
     * white space rules.  eg if the string buffer ends in white space
     * and the text node begins with white space, it will ne collapsed into one
     * lot of white space
     *
     * @param sb   StringBuffer
     * @param text text to smoosh in
     * @return the same StringBuffer object
     */
    private static StringBuffer smooshTogether(StringBuffer sb, String text)
    {
        if (endsWithWhitespace(sb.toString()) && startsWithWhitespace(text))
        {
            sb = rightTrim(sb);
            text = leftTrim(text);
            sb.append(" ");
        }
        sb.append(text);
        return sb;
    }

    /**
     * Collapses white space surrounding the text node into single spaces and substities &nbsp; into a single space characters
     *
     * @param textNodeStr the text to make into "whitespace" canonical form
     * @return the cleaned text node
     */
    private static String canonicalTextNode(String textNodeStr)
    {
        String[] cleanupStrs = new String[]{
                "\u00A0", // this is &nbsp in;
        };
        for (int i = 0; i < cleanupStrs.length; i++)
        {
            String cleanupStr = cleanupStrs[i];
            // clean up space entities
            int si = textNodeStr.indexOf(cleanupStr);
            if (si != -1)
            {
                int nbsplen = cleanupStr.length();
                StringBuilder sb = new StringBuilder(textNodeStr);
                while (si != -1)
                {
                    sb.replace(si, si + nbsplen, " ");
                    si++;
                    si = sb.indexOf(cleanupStr, si);
                }
                textNodeStr = sb.toString();
            }

        }

        // now normalize whitespace
        boolean hasFrontWhiteSpace = startsWithWhitespace(textNodeStr);
        boolean hasBackWhiteSpace = endsWithWhitespace(textNodeStr);
        textNodeStr = textNodeStr.trim();
        if (hasFrontWhiteSpace)
        {
            textNodeStr = " " + textNodeStr;
        }
        if (hasBackWhiteSpace)
        {
            textNodeStr = textNodeStr + " ";
        }
        /// handl edge case of all white space
        if (isAllWhitespace(textNodeStr))
        {
            textNodeStr = " ";
        }
        return textNodeStr;
    }

    private static boolean startsWithWhitespace(String s)
    {
        char[] chars = s.toCharArray();
        int len = chars.length;
        return (len > 0 && Character.isWhitespace(chars[0]));
    }

    private static boolean endsWithWhitespace(String s)
    {
        char[] chars = s.toCharArray();
        int len = chars.length;
        return (len > 0 && Character.isWhitespace(chars[len - 1]));
    }

    private static boolean isAllWhitespace(String s)
    {
        char[] chars = s.toCharArray();
        int len = chars.length;
        int i = 0;
        while (i < len)
        {
            if (!Character.isWhitespace(chars[i]))
            {
                return false;
            }
            i++;
        }
        return true;
    }

    private static String leftTrim(String s)
    {
        StringBuilder sb = new StringBuilder();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            if (sb.length() == 0 && Character.isWhitespace(chars[i]))
            {
                continue;
            }
            else
            {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    private static StringBuffer rightTrim(StringBuffer sb)
    {
        int len = sb.length();
        if (len > 0)
        {
            int index = sb.length() - 1;
            while (index >= 0 && Character.isWhitespace(sb.charAt(index)))
            {
                sb.deleteCharAt(index);
                index--;
            }
        }
        return sb;
    }

    /**
     * Walks the DOM nodes and find all TextNodes and appends them to a list
     *
     * @param node     - the Node to start from
     * @param textList the list of text nodes
     */
    private static void walkDOMAndGatherText(Node node, List textList)
    {
        walkDOMAndGatherImpl(node, textList, false);
    }

    private static void walkDOMAndGatherTextNodes(Node node, List textList)
    {
        walkDOMAndGatherImpl(node, textList, true);

    }

    private static void walkDOMAndGatherImpl(Node node, List textList, boolean justNodes)
    {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE)
            {
                Text textNode = (Text) child;
                if (justNodes)
                {
                    textList.add(textNode);
                }
                else
                {
                    String nodeText = textNode.getData();
                    textList.add(nodeText);
                }
            }
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                walkDOMAndGatherImpl(child, textList, justNodes);
            }
        }
    }

    /**
     * This will return an "canonical" version of the HTML that makes up the given {@link org.w3c.dom.Node}, exclusing itself.
     * <p/>
     * HTML tags will be converted to lower case if they are not already.
     *
     * @param node the Node in play
     * @return a text representation of the innards of the Node
     * @throws RuntimeException if the parsing is unsuccessful
     */
    public static String getInnerHTML(Node node)
    {
        StringBuilder sb = new StringBuilder();
        NodeList nodeList = node.getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++)
        {
            Node child = nodeList.item(i);
            sb.append(getHTML(child));
        }
        return sb.toString();
    }

    /**
     * This will return an "canonical" version of the HTML that makes up the given {@link org.w3c.dom.Element}, including itself.
     * <p/>
     * HTML tags will be converted to lower case if they are not already.  This is a suitabe default in JIRA even if the
     * object model of browsers and HttpUnit use uppercase for tags.  It matches the JSP code we use in lower case.
     *
     * @param node the Node in play
     * @return a text representation of the innards of the Element
     * @throws RuntimeException if the parsing is unsuccessful
     */
    public static String getHTML(Node node)
    {
        StringBuffer sb = new StringBuffer();
        useDOM(node, sb);
        //useSAX(node, sb);
        return sb.toString();
    }

    /**
     * Change this if we want to ALWAYS convert to lower case on output of DOM trees
     */
    private final static boolean convertTagsToLowerCaseOnOutput = false;

    private static void useDOM(Node node, StringBuffer sb)
    {
        new DomNodeOutputter(node, sb, convertTagsToLowerCaseOnOutput);
    }

    static void useSAX(Node node, StringBuffer sb)
    {
        try
        {
            DOMSource source = new DOMSource(node);
            SaxTagOutputter tagOutputter = new SaxTagOutputter(sb, convertTagsToLowerCaseOnOutput);
            SAXResult result = new SAXResult(tagOutputter);
            result.setLexicalHandler(tagOutputter);

            Transformer xformer;
            xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            xformer.setOutputProperty(OutputKeys.INDENT, "no");
            xformer.setOutputProperty(OutputKeys.METHOD, "html");
            xformer.transform(source, result);
        }
        catch (TransformerConfigurationException e1)
        {
            throw new RuntimeException(e1);
        }
        catch (TransformerException e1)
        {
            throw new RuntimeException(e1);
        }
    }


    /**
     * This will copy the {@link Node}' s from srcNode into a new {@link org.w3c.dom.DocumentFragment}
     *
     * @param srcNode                the source element to copy from
     * @param convertTagsToLowerCase whether to copy the element tags to lower case
     * @return the copied Node
     * @throws IllegalArgumentException if the srcNode is not a {@link org.w3c.dom.Document} or {@link org.w3c.dom.Element}
     */
    public static Node copyDOM(Node srcNode, boolean convertTagsToLowerCase)
    {
        return new DomNodeCopier(srcNode, convertTagsToLowerCase).getCopiedNode();
    }

    /**
     * This returns a {@link org.w3c.dom.Node} implementation that has better toString() attached to it.  Its useful inside a debugger
     * so you can easily see what content a node has.  Each {@link org.w3c.dom.Node} that is linked to from this node
     * will also get the better toString() methods applied to it.
     * <p/>
     * You can call this function safely on already wrapped Node objects since it can detect if they have already been wrapped
     *
     * @param node the Node to make into a better one
     * @return a proxied implementation of {@link org.w3c.dom.Node} that has the new functionality
     */
    public static Node betterNode(Node node)
    {
        return DomBetterNode.betterNode(node);
    }

}
