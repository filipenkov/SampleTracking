package com.atlassian.gadgets.dashboard.internal.diagnostics;

public class UrlPortMismatchException extends DiagnosticsException
{
    private final int expectedPort;
    private final int detectedPort;

    public UrlPortMismatchException(int expectedPort, int detectedPort)
    {
        super(String.format("Detected URL port, '%s', does not match expected port, '%s'", detectedPort, expectedPort));
        this.expectedPort = expectedPort;
        this.detectedPort = detectedPort;
    }

    public int getExpectedPort()
    {
        return expectedPort;
    }

    public int getDetectedPort()
    {
        return detectedPort;
    }
}
