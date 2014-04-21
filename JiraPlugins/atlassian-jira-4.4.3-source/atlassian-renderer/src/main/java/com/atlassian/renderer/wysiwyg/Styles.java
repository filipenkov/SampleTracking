package com.atlassian.renderer.wysiwyg;


import org.w3c.dom.Node;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 9/06/2005
 * Time: 16:52:56
 * To change this template use File | Settings | File Templates.
 */
public class Styles
{
    public final static String ITALIC = "font-style: italic";
    public final static String BOLD = "font-weight: bold";
    public final static String STRIKETHROUGH = "text-decoration: line-through";
    public final static String UNDERLINE = "text-decoration: underline";
    public static final String SUBSCRIPT = "baseline-shift: sub"; // always uses <sub> tags, this constant's actual value doesn't matter
    public static final String SUPERSCRIPT = "baseline-shift: sup"; // always uses <sup> tags, this constant's actual value doesn't matter
    public static final String CITE = "style-citation"; // always uses <cite> tags, this constant's actual value doesn't matter

    private Set styles = Collections.EMPTY_SET;

    private String getColour()
    {
        return colour;
    }

    private String colour = null;

    public Set getStyles()
    {
        return styles;
    }


    public Styles(Node node, Styles current)
    {
        styles = new HashSet();
        styles.addAll(current.getStyles());
        colour = current.getColour();
        if (node.getAttributes() != null)
        {
            Node styleNode = node.getAttributes().getNamedItem("style");
            if (styleNode != null)
            {
                String[] newStyles = styleNode.getNodeValue().split(";");
                for (int i = 0; i < newStyles.length; ++i)
                {
                    if (newStyles[i].trim().toLowerCase().startsWith("color:"))
                    {
                        setColour(newStyles[i].trim());
                    }
                    else
                    {
                        styles.add(newStyles[i].trim());
                    }
                }
            }
            Node colorNode = node.getAttributes().getNamedItem("color");
            if (colorNode != null)
            {
                colour = colorNode.getNodeValue().toLowerCase();
            }
        }
    }

    /**
     * Take a string of the form "color: rgb(51, 153, 102);" and set colour to #RRGGBB
     */
    private void setColour(String s)
    {
        s = s.toLowerCase();
        if (s.startsWith("color: rgb("))
        {
            String numbers = s.substring("color: rgb(".length(), s.length() - 1);
            String[] number = numbers.split(",");
            if (number.length != 3)
            {
                throw new RuntimeException("Bad color style format:'" + s + "'");
            }
            try
            {
                colour = "#" + hexDigits(number[0]) + hexDigits(number[1]) + hexDigits(number[2]);
            }
            catch (NumberFormatException nfe)
            {
                throw new RuntimeException("Bad color style format:'" + s + "'", nfe);
            }
        }
        else
        {
            if (s.equals("color:"))
            {
                colour = null;
            }
            else
            {
                colour = s.substring("color: ".length());
            }
        }
    }

    private String hexDigits(String number)
    {
        String s = Integer.toHexString(Integer.parseInt(number.trim())).toLowerCase();
        if (s.length() == 1)
        {
            s = "0" + s;
        }
        return s;
    }

    public Styles(String style, Styles current)
    {
        styles = new HashSet();
        if (current != null)
        {
            styles.addAll(current.getStyles());
            colour = current.getColour();
        }
        styles.add(style);
    }

    public Styles()
    {

    }

    public String decorateText(String text)
    {
        // Make the styles a list (rather than a StringBuffer), so we can support syntax with multiple characters
        // CONF-4889
        List prefix = new ArrayList();
        if (styles.contains(BOLD))
        {
            prefix.add("*");
        }
        if (styles.contains(ITALIC))
        {
            prefix.add("_");
        }
        if (styles.contains(STRIKETHROUGH))
        {
            prefix.add("-");
        }
        if (styles.contains(UNDERLINE))
        {
            prefix.add("+");
        }
        if (styles.contains(SUBSCRIPT))
        {
            prefix.add("~");
        }
        if (styles.contains(SUPERSCRIPT))
        {
            prefix.add("^");
        }
        if (styles.contains(CITE))
        {
            prefix.add("??");
        }
        StringBuffer s = new StringBuffer();
        if (colour != null)
        {
            s.append("{color:").append(colour).append("}");
        }
        StringBuffer prefixsb = new StringBuffer();
        StringBuffer suffixsb = new StringBuffer();
        for (int i = 0; i < prefix.size(); ++i)
        {
            prefixsb.append('{').append(prefix.get(i)).append('}');
            suffixsb.append('{').append(prefix.get(prefix.size() - 1 - i)).append('}');
        }
        s.append(prefixsb).append(text).append(suffixsb);
        if (colour != null)
        {
            s.append("{color}");
        }
        return s.toString();
    }
}
