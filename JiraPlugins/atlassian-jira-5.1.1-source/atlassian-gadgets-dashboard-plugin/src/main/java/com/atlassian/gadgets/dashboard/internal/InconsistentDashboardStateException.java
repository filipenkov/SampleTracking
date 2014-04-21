package com.atlassian.gadgets.dashboard.internal;

public class InconsistentDashboardStateException extends RuntimeException
{
    public InconsistentDashboardStateException(String message)
    {
        super(message);
    }
}
