package com.atlassian.gadgets.dashboard.internal.diagnostics;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DiagnosticsException extends Exception
{
    public DiagnosticsException(String message)
    {
        super(message);
    }

    public DiagnosticsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public String getStackTraceAsString()
    {
        StringWriter writer = new StringWriter();
        this.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
