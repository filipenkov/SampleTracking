package com.atlassian.jira.dev.backdoor;

import com.atlassian.core.filters.AbstractHttpFilter;
import org.apache.log4j.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Slows down each request to expose race conditions in browser-based asynchronous integration tests.
 *
 * @since v5.0
 */
public class SlowMotionFilter extends AbstractHttpFilter
{
    public static final String PROPERTY_KEY_ATLASSIAN_SLOMO = "atlassian.slomo";
    
    private static final long DEFAULT_DELAY = 2000;

    private static final Logger log = Logger.getLogger(SlowMotionFilter.class);
    private static final String HEADER_SLOMO = "X-Atlassian-Slomo";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        // do nothing if the property is missing
        String prop = System.getProperty(PROPERTY_KEY_ATLASSIAN_SLOMO);
        if (prop != null && !prop.equals("") && !prop.equals("0"))
        {
            long delay = DEFAULT_DELAY;
            try
            {
                delay = Math.max(Long.parseLong(prop), 0);
            }
            catch (NumberFormatException ignore)
            {
                log.debug(String.format("Slowing everything down to %s because I couldn't parse %s as a number", DEFAULT_DELAY, prop));
                // fall back to default delay..
            }
            try
            {
                response.setHeader(HEADER_SLOMO, Long.toString(delay));
                Thread.sleep(delay);
            }
            catch (InterruptedException ignore)
            {
            }
        }

        filterChain.doFilter(request, response);
    }
}
