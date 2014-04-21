package com.atlassian.jira.web.util;

import com.atlassian.jira.util.IOUtil;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A test harness to be used to measure precise mime sniffing behaviour of internet explorer. Keep this code for
 * measuring IE7 when we drop support for IE6 and we can change our {@link Ie6MimeSniffer}
 * to emulate that behaviour. IE 8 may fix this but it is still beta at the time of writing so it's not really relevant
 * to our install base.
 *
 * @since v3.13
 */
public class IeMimeSniffingServletHarness extends HttpServlet
{
    private static final Logger log = Logger.getLogger(IeMimeSniffingServletHarness.class);



    private static final String[] TAGS = new String[] {
            "a", "abbr", "acronym", "address", "applet", "area", "audioscope", "b", "base", "basefont", "bdo", "bgsound", "big", "blackface", "blink", "blockquote", "body", "bq", "br", "button", "caption", "center", "cite", "code", "col", "colgroup", "comment", "custom", "dd", "del", "dfn", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "fn", "font", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "i", "iframe", "ilayer", "img", "input", "ins", "isindex", "keygen", "kbd", "label", "layer", "legend", "li", "limittext", "link", "listing", "map", "marquee", "menu", "meta", "multicol", "nobr", "noembed", "noframes", "noscript", "nosmartquotes", "object", "ol", "optgroup", "option", "p", "param", "plaintext", "pre", "q", "rt", "ruby", "s", "samp", "script", "select", "server", "shadow", "sidebar", "small", "spacer", "span", "strike", "strong", "style", "sub", "sup", "table", "tbody", "td", "textarea", "tfoot", "th", "thead", "title", "tr", "tt", "u", "ul", "var", "wbr", "xml", "xmp", "!DOCTYPE", "!--"
    };

    private static final String[] TYPES = new String[] {
            "text/plain",
            "text/html",
            "text/css",
            "text/javascript",
            "image/jpeg",
            "image/gif",
            "image/png",
            "application/x-download",
            "application/octet-stream",
            "madeup/shit",
    };


    static final String[] EXPLOIT_FILES = new String[] {
            "1179826281.jpg",
            "1179826282.png",
            "test.png",
            "xsshack.gif",
            "html.html",
            "xsshack.jpg",
            "xsshack.png",
            "xsshack2.gif",
            "xsshack3.gif",
            "xssoj7.png"
    };

    public void init() throws ServletException
    {
        super.init();
        log.info("init");

    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String cd = request.getParameter("cd");
        if (cd != null)
        {
            response.setHeader("Content-Disposition", cd); // WARNING NOT FOR PRODUCTION USE: HEADER INJECTION PRONE
            log.info("setting Content-Disposition to " + cd);
        }

        String type = request.getParameter("type");
        if (type == null)
        {
            type = "text/plain";
        }
        response.setContentType(type);
        log.info("type: " + type);

        String itemNum = request.getParameter("item");
        int item = 0;
        if (itemNum != null)
        {
            try
            {
                item = Integer.parseInt(itemNum);
                if (item >= EXPLOIT_FILES.length)
                {
                    item = 0;
                }
            }
            catch (NumberFormatException e)
            {
                log.error(e);
            }
            returnContent(response, item);
        }
        else if (request.getParameter("gen") != null)
        {
            String generateTag = request.getParameter("gen");
            // use this param to generate a tag in a byte stream
            generateExploitTagResponse(response, generateTag);
        }
        else
        {
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            writer.println("<html><head><title>Mime Sniffer Test Harness</title></head><body>");
            writer.println("<h3>Mime Sniffer Test Harness</h3>");
            for (int i = 0; i < EXPLOIT_FILES.length; i++)
            {
                writer.println("<p>");
                writer.println(EXPLOIT_FILES[i]);
                for (int j = 0; j < TYPES.length; j++)
                {
                    String mimetype = TYPES[j];
                    writer.println(" <a href=\"?item=" + i + "&type=" + mimetype + "\">" + mimetype + "</a> ");
                }
                writer.println("</p>");
            }
            writer.println("<hr>");
            writer.println("<h3>checking tags using iframes</h3>");
            writer.println("<table border=\"1\">");
            for (int i = 0; i < TAGS.length; i++)
            {
                String tag = TAGS[i];
                String url = "?gen=" + response.encodeURL(tag) + "&type=" + response.encodeURL(type);
                writer.println("<tr><td><a href=\"" + url + "\">" + TextUtils.htmlEncode(tag) + "</a></td>");
                writer.println("<td><iframe src=\"" + url + "\" width=\"400\" height=\"30\" marginwidth=\"0\" marginheight=\"0\" scrolling=\"no\"></iframe></td></tr>");
                writer.flush();
            }
            writer.println("</table>");
            writer.println("</body></html>");
        }

    }

    private void generateExploitTagResponse(HttpServletResponse response, String generateTag)
            throws IOException
    {
        OutputStream out = response.getOutputStream();
        String chars = "<" + generateTag + "&copy;";
        try
        {
            int prefill = Ie6MimeSniffer.MAX_BYTES_TO_SNIFF - chars.length();
            for (int i = 1; i <= prefill; i++)
            {
                out.write(".".getBytes("ASCII"));
            }
            out.write(chars.getBytes("ASCII"));
            out.flush();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void returnContent(HttpServletResponse httpServletResponse, int i) throws IOException
    {
        log.info("returning content item number " + i);
        FileInputStream fis = new FileInputStream("src/etc/test/com/atlassian/jira/web/util/" + EXPLOIT_FILES[i]);
        IOUtil.copy(fis, httpServletResponse.getOutputStream());
        httpServletResponse.getOutputStream().flush();
    }

}
