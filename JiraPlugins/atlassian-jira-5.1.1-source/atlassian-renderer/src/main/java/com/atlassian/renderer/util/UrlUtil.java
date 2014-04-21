package com.atlassian.renderer.util;

import com.opensymphony.util.TextUtils;
import com.atlassian.renderer.v2.components.HtmlEscaper;
import org.radeox.util.Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class UrlUtil
{
    public static final List URL_PROTOCOLS = Collections.unmodifiableList(Arrays.asList(new String[]
        {"http://", "https://", "ftp://", "ftps://", "mailto:", "nntp://", "news://", "irc://", "file:"}));

    /**
     * Maximum length of a URL schema, e.g. "https://"
     */
    protected static final int URL_SCHEMA_LENGTH = 8;

    /**
     * Will return a new string with ampersands replaced with '&amp;amp;'.
     *
     * @return the safe(r) string
     * @deprecated since 3.12 use {@link HtmlEscaper#escapeAmpersands(String,boolean)} passing 'true' to
     *             preserveExistingEntities.
     */
    public static String escapeSpecialCharacters(String url)
    {
        if (url == null)
        {
            return null;
        }

        return HtmlEscaper.escapeAmpersands(url, true);
    }

    public static boolean startsWithUrl(String str)
    {
        if (!TextUtils.stringSet(str))
        {
            return false;
        }

        return getUrlIndex(str) == 0;
    }

    public static boolean containsUrl(String str)
    {
        if (!TextUtils.stringSet(str))
        {
            return false;
        }

        return getUrlIndex(str) != -1;
    }

    public static int getUrlIndex(String str)
    {
        if (!TextUtils.stringSet(str))
        {
            return -1;
        }

        String str_lower = str.toLowerCase();
        Iterator it = URL_PROTOCOLS.iterator();

        while (it.hasNext())
        {
            String protocol = (String) it.next();
            int index = str_lower.indexOf(protocol);

            if (index != -1 && (index == 0 || !Character.isLetterOrDigit(str_lower.charAt(index - 1))))
            {
                return index;
            }
        }

        return -1;
    }

    public static String escapeUrlFirstCharacter(String linkBody)
    {
        int i = getUrlIndex(linkBody);
        if (i == 0)
        {
            StringBuffer buf = new StringBuffer(linkBody);
            char c = buf.charAt(i);

            buf.deleteCharAt(i);
            buf.insert(i, Encoder.toEntity(c));
            linkBody = buf.toString();
        }
        return linkBody;
    }

    /**
     * Fixes the URLs used in content imported from external sources to have a base URL that points to that external
     * source. For example, where a JIRA portlet uses a URL relative to JIRA, the JIRA base URL should be prepended so
     * Confluence users see the correct URL.
     * <p/>
     * Links are fixed in href and src attributes in the HTML. Different types of links are treated differently
     * (assuming a baseUrl of "http://www.example.com/path/to/file.html"): <ul> <li>Where a link is remote (e.g.
     * "http://www.atlassian.com"), it is left intact.</li> <li>Where a link is local and absolute (e.g. "/foo/bar"),
     * the hostname component of the baseUrl is prepended: "http://www.example.com/foo/bar"</li> <li>Where a link is
     * local and relative (e.g. "foo/bar?opt=baz"), the host and path components of the baseUrl are prepended, with any
     * file component removed: "http://www.example.com/path/to/foo/bar?opt=baz"</li> </ul>
     *
     * @param html the HTML text which will have its links corrected to match the base URL of the remote server.
     * @param baseUrl the URL which local links will be made relative to. Typically this is the base URL of the remote
     * content.
     * @return HTML text modified so all links point to the remote content.
     */
    public static String correctBaseUrls(String html, String baseUrl)
    {
        if (html.length() < 10)
        {
            return html;
        }

        StringBuffer result = new StringBuffer(html.length());

        int idx = 0;
        while (true)
        {
            String matchText = "";
            int matchIdx = html.length() + 1; // initialise beyond the end of the string

            String[] linkText = linksToFix();
            for (int i = 0; i < linkText.length; i++)
            {
                int testIdx = html.indexOf(linkText[i], idx);
                if (testIdx >= 0 && testIdx < matchIdx)
                {
                    matchText = linkText[i];
                    matchIdx = testIdx;
                }
                // don't exit early -- we need to find the match closest to the start of the string!
            }

            if (matchIdx > html.length()) // no match found
            {
                result.append(html.substring(idx));
                break;
            }

            matchIdx += matchText.length();
            result.append(html.substring(idx, matchIdx));

            String linkStart = html.substring(matchIdx, Math.min(matchIdx + URL_SCHEMA_LENGTH, html.length()));

            if (isLocalUrl(linkStart))
            {
                if (linkStart.startsWith("/"))
                    result.append(getServerUrl(baseUrl));
                else
                    result.append(getUrlPath(baseUrl)).append("/");
            }

            idx = matchIdx;
        }

        return result.toString();
    }

    /**
     * @param url should not be null
     * @return false if the link starts with a valid protocol (e.g. http://), otherwise true.
     */
    private static boolean isLocalUrl(String url)
    {
        String[] validProtocols = new String[]{"http://", "https://", "mailto:", "ftp://"};
        for (int i = 0; i < validProtocols.length; i++)
        {
            String validProtocol = validProtocols[i];
            if (url.startsWith(validProtocol)) return false;
        }
        return true;
    }

    /**
     * Returns the baseUrl, first removing the query parameters, then removing everything upto and including the last
     * slash.
     *
     * @param baseUrl should not be null, http://www.example.com/foo/bar?quux
     * @return the directory with a trailing slash if one was passed, e.g. http://www.example.com/foo
     */
    private static String getUrlPath(String baseUrl)
    {
        String result = baseUrl;

        // strip query parameters
        if (result.indexOf('?') > 0)
            result = result.substring(0, result.indexOf('?'));

        // strip everything after last slash, excluding slashes in the URL schema
        int lastSlash = result.lastIndexOf('/');
        if (lastSlash >= URL_SCHEMA_LENGTH)
            result = result.substring(0, lastSlash);

        return result;
    }

    /**
     * Returns the baseUrl with everything after the first slash removed (excluding slashes in the URL schema, e.g.
     * "http://").
     *
     * @param baseUrl should not be null, e.g. http://www.example.com/foo/bar?quux
     * @return the absolute server URL with a trailing slash if one was passed, e.g. http://www.example.com/
     */
    private static String getServerUrl(String baseUrl)
    {
        String result = baseUrl;

        // strip everything after first slash, excluding slashes in the URL schema
        int firstSlash = result.indexOf('/', URL_SCHEMA_LENGTH);
        if (firstSlash >= 0)
            result = result.substring(0, firstSlash);

        return result;
    }

    private static String[] linksToFix()
    {
        return new String[]{
            " href=\"",
            " href='",
            " src=\"",
            " src='"
        };
    }

    /**
     * @param request The current request
     * @param name The name of the parameter to add/replace
     * @param value The value of the parameter to add/replace
     * @deprecated since 3.12 this is not used. Builds a URL with a new parameter, replacing any existing parameters with that name
     *             but maintaining all other parameters.
     *             <p/>
     *             For example /foo.html?name=fred&key=FLINT
     */
    public static String buildNewRelativeUrl(HttpServletRequest request, String name, String value)
    {
        StringBuffer url = new StringBuffer(request.getContextPath());
        url.append(request.getServletPath());
        if (request.getPathInfo() != null)
        {
            url.append(request.getPathInfo());
        }
        url.append("?");
        Map params = request.getParameterMap();

        boolean paramAppended = false;
        for (Iterator iterator = params.keySet().iterator(); iterator.hasNext();)
        {
            String paramName = (String) iterator.next();
            if (name.equals(paramName))
            {
                appendParam(url, name, value);
                paramAppended = true;
            }
            else
            {
                appendParam(url, paramName, ((String[]) params.get(paramName))[0]);
            }
            if (iterator.hasNext())
            {
                url.append('&');
            }
        }

        if (!paramAppended)
        {
            url.append("&");
            appendParam(url, name, value);
        }

        return url.toString();
    }

    /**
     * The method above which calls this is deprecated. Don't use this method.
     *
     * @deprecated since 3.12
     */
    private static void appendParam(StringBuffer url, String paramName, String value)
    {
        url.append(urlEncode(paramName)).append("=").append(urlEncode(value));
    }

    /**
     * The method above which calls this is deprecated. Don't use this method.
     * It doesn't correctly retrieve the encoding from the application.
     *
     * @deprecated since 3.12
     */
    private static String urlEncode(String url)
    {
        if (url == null)
        {
            return null;
        }

        try
        {
            return URLEncoder.encode(url, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return url;
        }
    }
}
