package com.atlassian.trackback;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.net.SocketTimeoutException;

/**
 * Some simple utility methods used in the Trackback component.
 */
public class TrackbackUtils
{
    private static Log log;

    private static int REGEX_OPTIONS;
    private static Pattern RDF_OUTER_PATTERN;
    private static Pattern RDF_INNER_PATTERN;
    private static Pattern DC_IDENTIFIER_PATTERN;
    private static Pattern TRACKBACK_PING_PATTERN;
    private static Pattern HREF_PATTERN;

    static
    {
        try
        {
            log = LogFactory.getLog(TrackbackUtils.class);
            REGEX_OPTIONS = Perl5Compiler.SINGLELINE_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK;
            RDF_OUTER_PATTERN = new Perl5Compiler().compile("(<rdf:RDF.*?</rdf:RDF>).*?", REGEX_OPTIONS);
            RDF_INNER_PATTERN = new Perl5Compiler().compile("(<rdf:Description.*?/>)", REGEX_OPTIONS);
            DC_IDENTIFIER_PATTERN = new Perl5Compiler().compile("dc:identifier=\"(.*?)\"");
            TRACKBACK_PING_PATTERN = new Perl5Compiler().compile("trackback:ping=\"(.*?)\"");
            HREF_PATTERN = new Perl5Compiler().compile("<\\s*a .*?href\\s*=\\s*\"(http([^\"]+).*?)\"\\s*>", REGEX_OPTIONS);
        }
        catch (MalformedPatternException e)
        {
            final String message = "Error while initializing TrackbackUtils";
            log.error(message, e);
            throw new RuntimeException(message);
        }
    }

    protected static final String MIME_TYPE_HTML = "text/html";
    protected static final String MIME_TYPE_XML = "text/xml";
    protected static final String MIME_CORRECT_TYPE_XML = "application/rss+xml;";
    protected static final String MIME_TYPE_XHTML = "application/xhtml+xml";
    private static final int MAX_RESULT_LENGTH = 100 * 1024;
    protected static final int HTTPCLIENT_SOCKET_TIMEOUT = 1000 * 30;

    /**
     * Given a chunk of content and a hyperlink to look for, find the relevant trackback url.
     *
     * @param remoteContent The HTML content of a remote page possibly containing a trackback autodiscovery block
     * @param hyperlink     The link to search for if there is more than one autodiscovery block
     * @return The trackback ping URL, or null if no URL is found.
     */
    public static String getTrackbackUrl(String remoteContent, String hyperlink)
    {
        // Look for the Auto Trackback RDF in the HTML
        final PatternMatcher matcher = new Perl5Matcher();
        final PatternMatcherInput input = new PatternMatcherInput(remoteContent);

        while (matcher.contains(input, RDF_OUTER_PATTERN))
        {
            if (log.isDebugEnabled()) log.debug("Found outer RDF text in hyperlink");

            final MatchResult result = matcher.getMatch();
            for (int i = 0; i < result.groups(); i++)
            {
                String outerRdfText = result.group(i);

                // Look for the inner RDF description

                PatternMatcher rdfInnerMatcher = new Perl5Matcher();
                final PatternMatcherInput outerRdfTextInput = new PatternMatcherInput(outerRdfText);
                while (rdfInnerMatcher.contains(outerRdfTextInput, RDF_INNER_PATTERN))
                {
                    log.debug("Found inner RDF text in hyperlink");

                    final MatchResult rdfOuterRdfTextResult = rdfInnerMatcher.getMatch();
                    for (int j = 0; j < rdfOuterRdfTextResult.groups(); j++)
                    {
                        String innerRdfText = rdfOuterRdfTextResult.group(j);

                        // Look for a dc:identifier attribute which matches the current hyperlink
                        PatternMatcher dcIdentifierMatcher = new Perl5Matcher();
                        final PatternMatcherInput dcIdentifierInput = new PatternMatcherInput(innerRdfText);
                        if (dcIdentifierMatcher.contains(dcIdentifierInput, DC_IDENTIFIER_PATTERN))
                        {
                            final MatchResult dcIdentifierResult = dcIdentifierMatcher.getMatch();
                            String dcIdentifier = dcIdentifierResult.group(1);

                            // If we find a match, return the URL
                            if (dcIdentifier.equals(hyperlink))
                            {
                                log.debug("Matched dc:identifier to hyperlink");
                                PatternMatcher trackbackPingMatcher = new Perl5Matcher();
                                final PatternMatcherInput pingInput = new PatternMatcherInput(innerRdfText);
                                if (trackbackPingMatcher.contains(pingInput, TRACKBACK_PING_PATTERN))
                                {
                                    final MatchResult pingResult = trackbackPingMatcher.getMatch();
                                    String trackbackUrl = pingResult.group(1);
                                    log.debug("Got trackback URL " + trackbackUrl);
                                    return trackbackUrl;
                                }
                            }
                            else
                            {
                                log.debug("dc:identifier mismatch; expected '" + hyperlink + "', found '" + dcIdentifier + "', ignoring");
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get the content of a remote URL
     *
     * @return Text content, or null if an invalid HTTP response / content type is received
     * @throws RuntimeException      For various other HTTP errors
     */
    public static String getUrlContent(String hyperlink) throws IOException
    {
        try
        {
            HttpClient client = new HttpClient();
            // JRA-7589 - set timeout to abort connections that will not return
            client.setTimeout(HTTPCLIENT_SOCKET_TIMEOUT);
            HttpMethod method = new GetMethod(hyperlink);
            method.addRequestHeader("Accepts", MIME_TYPE_HTML + ", " + MIME_TYPE_XML + "," + MIME_TYPE_XHTML);
            int statusCode = -1;
            try
            {
                statusCode = client.executeMethod(method);
            }
            catch (HttpException e)
            {
                log.info("Problem retrieving content for '"+hyperlink+": "+e.getMessage());
                log.debug(e.getMessage(), e);
                return null;
            }
            catch (SocketTimeoutException e)
            {
                log.info("Timeout exceeeded - unable to parse content from '"+hyperlink+": "+ e.getMessage());
                log.debug(e.getMessage(), e);
                return null;
            }

            if (statusCode == 200 && isValidResponseContentType(method))
            {
                InputStream is = method.getResponseBodyAsStream();
                if (is == null)
                    return null;

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String s = null;

                StringBuffer result = new StringBuffer();

                while ((s = reader.readLine()) != null && result.length() < MAX_RESULT_LENGTH)
                {
                    result.append(s);
                }

                is.close();
                method.releaseConnection();
                return result.toString();
            }
            else
            {
                return null;
            }
        }
        catch (IllegalStateException e)
        {
            log.info("IllegalStateException whilst retrieving URL " + hyperlink + ". Exception " + e.getMessage(), e);
            return null;
        }
    }

    private static boolean isValidResponseContentType(HttpMethod method)
    {
        Header contentType = method.getResponseHeader("Content-type");
        String headerValue = contentType.getValue();

        if (headerValue.startsWith(MIME_TYPE_HTML) ||
                headerValue.startsWith(MIME_TYPE_XML) ||
                headerValue.startsWith(MIME_CORRECT_TYPE_XML) ||
                headerValue.startsWith(MIME_TYPE_XHTML))
            return true;
        else
            return false;
    }

    /**
     * Find a list of hyperlinks within a piece of HTML content
     *
     * @return List of String hyperlinks
     */
    public static List getHttpLinks(String html)
    {
        List result = new ArrayList();

        PatternMatcher hrefMatcher = new Perl5Matcher();
        final PatternMatcherInput input = new PatternMatcherInput(html);
        while (hrefMatcher.contains(input, HREF_PATTERN))
        {
            final MatchResult match = hrefMatcher.getMatch();
            if (match.groups() == 3)
            {
                String hyperlink = match.group(1);
                log.debug("Found hyperlink: " + hyperlink);
                result.add(hyperlink);
            }
        }

        return result;
    }

}
