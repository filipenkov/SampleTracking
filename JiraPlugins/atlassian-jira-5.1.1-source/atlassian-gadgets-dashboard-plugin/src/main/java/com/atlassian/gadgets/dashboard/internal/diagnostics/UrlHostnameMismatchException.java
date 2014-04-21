package com.atlassian.gadgets.dashboard.internal.diagnostics;

public class UrlHostnameMismatchException extends DiagnosticsException
{
    private final String expectedHostname;
    private final String detectedHostname;

    public UrlHostnameMismatchException(String expectedHostname, String detectedHostname)
    {
        super(String.format("Detected URL hostname, '%s', does not match expected hostname, '%s'",
                            detectedHostname,
                            expectedHostname));
        this.expectedHostname = expectedHostname;
        this.detectedHostname = detectedHostname;
    }

    public String getExpectedHostname()
    {
        return expectedHostname;
    }

    public String getDetectedHostname()
    {
        return detectedHostname;
    }
}
