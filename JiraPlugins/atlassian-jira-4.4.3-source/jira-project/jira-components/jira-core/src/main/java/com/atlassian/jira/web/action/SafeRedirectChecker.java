package com.atlassian.jira.web.action;

import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Contains methods that check whether a particular redirect is "safe" or not.
 *
 * @since v4.3
 */
public class SafeRedirectChecker
{
    private final Logger log = LoggerFactory.getLogger(SafeRedirectChecker.class);
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    /**
     * Creates a new SafeRedirectChecker
     *
     * @param velocityRequestContextFactory a VelocityRequestContextFactory
     */
    public SafeRedirectChecker(VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    /**
     * Returns a boolean indicating whether redirecting to the given URI is allowed or not. This method returns false if
     * the <code>redirectUri</code> is an absolute URI and it points to a domain that is not this JIRA instance's
     * domain, and true otherwise.
     *
     * @param redirectUri a String containing a URI
     * @return a boolean indicating whether redirecting to the given URI should be allowed or not
     */
    public boolean canRedirectTo(String redirectUri)
    {
        if (redirectUri == null)
        {
            return true;
        }

        try
        {
            URI uri = new URI(redirectUri);
            return uri.getScheme() == null || redirectUri.startsWith(getCanonicalBaseURL());
        }
        catch (URISyntaxException e)
        {
            log.debug("Failed to parse URI '{}', redirect will not be allowed", redirectUri);
            return false;
        }
    }

    /**
     * Returns the canonical base URL for JIRA.
     *
     * @return a String containing the canonical base URL
     */
    protected String getCanonicalBaseURL()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }
}
