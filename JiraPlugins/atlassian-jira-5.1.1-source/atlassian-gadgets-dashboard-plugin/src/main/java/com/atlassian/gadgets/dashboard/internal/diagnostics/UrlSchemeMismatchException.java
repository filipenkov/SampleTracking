package com.atlassian.gadgets.dashboard.internal.diagnostics;

public class UrlSchemeMismatchException extends DiagnosticsException
{
    private final String expectedScheme;
    private final String detectedScheme;

    public UrlSchemeMismatchException(String expectedScheme, String detectedScheme)
    {
        super(String.format("Detected URL scheme, '%s', does not match expected scheme '%s'",
                            detectedScheme,
                            expectedScheme));
        this.expectedScheme = expectedScheme;
        this.detectedScheme = detectedScheme;
    }

    public String getExpectedScheme()
    {
        return expectedScheme;
    }

    public String getDetectedScheme()
    {
        return detectedScheme;
    }
}
