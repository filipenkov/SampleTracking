package com.atlassian.streams.internal.feed;

import java.net.URL;

import com.atlassian.streams.api.FeedContentSanitizer;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a way to sanitize input strings to ensure that it is safe to output that content in a Feed, without risk of
 * XSS attacks.
 * 
 * @author pkaeding
 * @see antisamy-policy.xml
 */
public class FeedContentSanitizerImpl implements FeedContentSanitizer
{

    private static final Logger log = LoggerFactory.getLogger(FeedContentSanitizerImpl.class);
    public static final String ANTISAMY_POLICY_FILE = "antisamy-policy.xml";

    private URL policyUrl;
    private AntiSamy as;
    private Policy policy;

    public FeedContentSanitizerImpl()
    {
        policyUrl = getClass().getClassLoader().getResource(ANTISAMY_POLICY_FILE);
        as = new AntiSamy();
        try
        {
            policy = Policy.getInstance(policyUrl);
        }
        catch (PolicyException e)
        {
            log.error("Error loading AntiSamy policy file", e);
        }
    }

    /* (non-Javadoc)
     * @see com.atlassian.streams.internal.feed.FeedContentSanitizer#sanitize(java.lang.String)
     */
    @Override
    public String sanitize(String taintedInput)
    {
        CleanResults cr;
        try
        {
            cr = as.scan(taintedInput, policy);
        }
        catch (PolicyException e)
        {
            log.error("Error loading AntiSamy policy file", e);
            // If we can't sanitize the input, we will just swallow the whole thing, and
            // return an empty string, rather than display tainted content
            return "";
        }
        catch (ScanException e)
        {
            log.error("Error scanning input with AntiSamy", e);
            return "";
        }
        return cr.getCleanHTML();
    }
}
