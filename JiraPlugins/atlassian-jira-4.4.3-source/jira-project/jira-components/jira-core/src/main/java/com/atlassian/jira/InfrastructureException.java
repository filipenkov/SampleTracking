/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Apr 14, 2004
 * Time: 6:01:11 PM
 */
package com.atlassian.jira;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class InfrastructureException extends NestableRuntimeException
{
    public InfrastructureException()
    {
    }

    public InfrastructureException(String s)
    {
        super(s);
    }

    public InfrastructureException(Throwable throwable)
    {
        super(throwable);
    }

    public InfrastructureException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}