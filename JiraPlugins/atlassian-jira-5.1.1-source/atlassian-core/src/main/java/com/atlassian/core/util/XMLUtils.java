/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 20/02/2002
 * Time: 16:48:36
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLUtils
{
    /**
     * Our default policy is to replace ummappable characters with the replacement char
     */
    private static final ReplacePolicy DEFAULT_POLICY = new ReplacePolicy();

    /**
     * With a given parent XML Element, find the text contents of the child element with supplied name.
     */
    public static String getContainedText(Node parent, String childTagName)
    {
        try
        {
            Node tag = ((Element) parent).getElementsByTagName(childTagName).item(0);
            return ((Text) tag.getFirstChild()).getData();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Given one element, get a single named child element of it.
     */
    public static Element getSingleChildElement(Element el, String name)
    {
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName()))
            {
                return (Element) node;
            }
        }

        return null;
    }

    /**
     * Get an attribute of a given element, with a default value if the attribute is not present or blank.
     */
    public static String getAttributeWithDefault(Element element, String attributeName, String defaultValue)
    {
        String group = element.getAttribute(attributeName);

        if (group == null || "".equals(group.trim()))
        {
            group = defaultValue;
        }

        return group;
    }

    /**
     * Escapes a string so it may be returned as text content or attribute value. Non printable characters are escaped
     * using character references. Where the format specifies a deault entity reference, that reference is used (e.g.
     * <tt>&amp;lt;</tt>).
     *
     * @param source the string to escape or "" for null.
     * @param policy how to handle invalid XML characters
     * @since 3.19 / 3.10.1
     */
    public static String escape(final String source, final TransformPolicy policy)
    {
        if (source == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder(source.length() + 30);  // lets allocate a StringBuilder that is roughly the same length
        for (int i = 0; i < source.length(); ++i)
        {
            transform(sb, source.charAt(i), policy);
        }
        return sb.toString();
    }

    /**
     * Escape an XML string using a default policy of replacing invalid XML characters.
     */
    public static String escape(final String source)
    {
        return escape(source, DEFAULT_POLICY);
    }


    public static String escapeForCdata(String source)
    {
        if (source == null)
        {
            return null;
        }
        final StringBuilder sb = new StringBuilder();

        int index;
        int oldIndex = 0;
        while ((index = source.indexOf("]]>", oldIndex)) > -1)
        {
            final String str = source.substring(oldIndex, index);
            transformCData(sb, str, DEFAULT_POLICY);

            oldIndex = index + 3;
            sb.append("]]]]><![CDATA[>");
        }

        final String rest = source.substring(oldIndex);
        transformCData(sb, rest, DEFAULT_POLICY);

        return sb.toString();
    }

    /**
     * Identifies the last printable character in the Unicode range that is supported by the encoding used with this
     * serializer. For 8-bit encodings this will be either 0x7E or 0xFF. For 16-bit encodings this will be 0xFFFF.
     * Characters that are not printable will be escaped using character references.
     */
    static private int _lastPrintable = 0x7E;

    /**
     * Encode special XML characters into the equivalent character references. These five are defined by default for all
     * XML documents. Converts '<', '>', '"'. and '\'' to "lt", "gt", "quot", or "apos".
     */
    static private String getEntityRef(char ch)
    {
        switch (ch)
        {
            case '<':
                return "lt";
            case '>':
                return "gt";
            case '"':
                return "quot";
            case '\'':
                return "apos";
            case '&':
                return "amp";
        }

        return null;
    }

    /**
     * If there is a suitable entity reference for this character, return it. The list of available entity references is
     * almost but not identical between XML and HTML. This uses a default transformation policy of replacing invalid XML
     * characters.
     */
    public static String escape(final char ch)
    {
        return escape(ch, DEFAULT_POLICY);
    }

    /**
     * Escape XML characters with a user specified transformation policy for invalid characters.
     *
     * @since 3.19 / 3.10.1
     */
    public static String escape(final char ch, final TransformPolicy policy)
    {
        final StringBuilder sb = new StringBuilder();
        transform(sb, ch, policy);
        return sb.toString();
    }

    /**
     * Append escaped version of character to the end of the StringBuilder
     */
    private static void transform(final StringBuilder sb, final char ch, final TransformPolicy policy)
    {
        if (!validXml(ch))
        {
            sb.append(policy.handle(ch));
        }
        else
        {
            String charRef = getEntityRef(ch);
            if (charRef != null)
            {
                sb.append("&").append(charRef).append(";");
            }
            else if ((ch >= ' ' && ch <= _lastPrintable && ch != 0xF7) ||
                    ch == '\n' || ch == '\r' || ch == '\t')
            {
                // If the character is not printable, print as character reference.
                // Non printables are below ASCII space but not tab or line
                // terminator, ASCII delete, or above a certain Unicode threshold.
                sb.append(ch);
            }
            else
            {
                sb.append("&#").append(Integer.toString(ch)).append(";");
            }
        }
    }

    /**
     * Append escaped version of CData "character data" to the end of the StringBuilder
     */
    private static void transformCData(final StringBuilder sb, final String cdata, final TransformPolicy policy)
    {
        for (int i = 0; i < cdata.toCharArray().length; i++)
        {
            char ch = cdata.toCharArray()[i];
            if (!validXml(ch))
            {
                sb.append(policy.handle(ch));
            }
            else
            {
                sb.append(ch);
            }
        }
    }
    
    /**
     * @since 3.19 / 3.10.1
     */
    // http://www.w3.org/TR/REC-xml/#charsets
    public static boolean validXml(final char ch)
    {
        return ((ch == 0x9) ||
                (ch == 0xA) ||
                (ch == 0xD) ||
                ((ch >= 0x20) && (ch <= 0xD7FF)) ||
                ((ch >= 0xE000) && (ch <= 0xFFFD)) ||
                ((ch >= 0x10000) && (ch <= 0x10FFFF)));
    }

    /**
     * @since 3.19 / 3.10.1
     */
    public interface TransformPolicy
    {
        String handle(final char input);
    }

    // Replace bad XML input with a fixed character
    public static class ReplacePolicy implements TransformPolicy
    {
        public String handle(final char input)
        {
            // the unicode REPLACEMENT CHARACTER: ?
            return "\uFFFD";
        }
    }

}
