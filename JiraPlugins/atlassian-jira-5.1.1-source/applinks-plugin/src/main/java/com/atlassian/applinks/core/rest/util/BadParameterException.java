package com.atlassian.applinks.core.rest.util;

public class BadParameterException extends RuntimeException
{
    public BadParameterException(final String parameter)
    {
        super(parameter + " is a required parameter");
    }
}
