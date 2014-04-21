package com.atlassian.jira.util;

import com.atlassian.jira.local.ListeningTestCase;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@code com.atlassian.jira.util.UrlValidator}.
 */
public class TestUrlValidator extends ListeningTestCase
{
    @Test
    public void testValidUrls()
    {
        final ImmutableList<String> urls = ImmutableList.of(
                "http://jira.example.com",
                "http://jira.example.com",
                "https://jira.example.com",
                "http://localhost:8090/jira",
                "http://example.com:8090/jira",
                "http://jira.me",
                "http://jira.mytestsite.loc:8080",
                "http://localhost.localdomain",
                "http://localhost.abc.colo/jirax",
                "http://localhost.local"
        );
        for (String url : urls)
        {
            assertTrue("Valid URL '" + url + "' was evaluated as invalid", UrlValidator.isValid(url));
        }
    }

    @Test
    public void testInternationalizedDomainName()
    {
        assertTrue(UrlValidator.isValid("http://\u00E8x\u00E6mpl\u0113.com"));
    }

    @Test
    public void testInvalidCharacters()
    {
        final ImmutableList<String> urls = ImmutableList.of(
                "http://exa mple.com:8090/jira",
                "http://example.com:8090/^jira",
                "http://example.com:8090:80/jira",
                "http://example..com",
                "http://example.com.:80"
                );
        for (String url : urls)
        {
            assertFalse("Invalid URL '" + url + "' was evaluated as valid", UrlValidator.isValid(url));
        }

    }

    @Test
    public void testInvalidTld()
    {
        // we accept it because customer BTF may use many strange TLDs.
        assertTrue(UrlValidator.isValid("http://example.invalid.tld"));
    }

    @Test
    public void testInvalidPort()
    {
        assertFalse(UrlValidator.isValid("http://example.com:not_a_port/jira"));
    }

    @Test
    public void testInvalidScheme()
    {
        assertFalse(UrlValidator.isValid("ssh://example.com:8090/jira"));
        assertFalse(UrlValidator.isValid("file://example.com:8090/jira"));
    }

    @Test
    public void testNoScheme()
    {
        assertFalse(UrlValidator.isValid("example.com:8090/jira"));
    }
}
