package com.atlassian.gadgets.dashboard.internal.diagnostics;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.base.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diagnostics
{
    private static final Logger log = LoggerFactory.getLogger(Diagnostics.class);

    private final ApplicationProperties application;

    public Diagnostics(ApplicationProperties application)
    {
        this.application = application;
    }

    public void check(URI clientDetectedUri)
        throws URISyntaxException, UrlSchemeMismatchException, UrlHostnameMismatchException, UrlPortMismatchException
    {
        log.info("DIAGNOSTICS: Running Dashboard Diagnostics");
        URI baseUri = new URI(application.getBaseUrl());
        int baseUriPort = getPort(baseUri);
        int clientDetectedUriPort = getPort(clientDetectedUri);
        checkExpectedScheme(clientDetectedUri.getScheme(), baseUri.getScheme());
        checkExpectedHostname(clientDetectedUri.getHost(), baseUri.getHost());
        checkExpectedPort(clientDetectedUriPort, baseUriPort);
        log.info("DIAGNOSTICS: All OK");
    }

    private int getPort(URI uri)
    {
        if (uri.getPort() == -1)
        {
            if (uri.getScheme().equals("http"))
            {
                return 80;
            }
            else if (uri.getScheme().equals("https"))
            {
                return 443;
            }
        }
        return uri.getPort();
    }

    private void checkExpectedScheme(String expectedScheme, String detectedScheme) throws UrlSchemeMismatchException
    {
        log.info("SCHEME: Checking that the detected URL scheme, '{}', matches the expected scheme, '{}'",
                 detectedScheme, expectedScheme);
        if (!Objects.equal(expectedScheme, detectedScheme))
        {
            throw new UrlSchemeMismatchException(expectedScheme, detectedScheme);
        }
        log.info("SCHEME: OK ({})", detectedScheme);
    }

    private void checkExpectedHostname(String expectedHostname, String detectedHostname)
        throws UrlHostnameMismatchException
    {
        log.info("HOST: Checking that the detected hostname, '{}', matches the expected hostname, '{}'",
                 detectedHostname, expectedHostname);
        if (!Objects.equal(expectedHostname, detectedHostname))
        {
            throw new UrlHostnameMismatchException(expectedHostname, detectedHostname);
        }
        log.info("HOST: OK ({})", detectedHostname);
    }

    private void checkExpectedPort(int expectedPort, int detectedPort) throws UrlPortMismatchException
    {
        log.info("PORT: Checking that the detected port, '{}', matches the expected port, '{}'",
                 detectedPort, expectedPort);
        if (expectedPort != detectedPort)
        {
            throw new UrlPortMismatchException(expectedPort, detectedPort);
        }
        log.info("PORT: OK ({})", detectedPort);
    }
}
