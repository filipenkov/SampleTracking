package com.atlassian.config.wizard;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 14:22:10
 * To change this template use File | Settings | File Templates.
 */
public class StepNotFoundException extends Exception
{
    public StepNotFoundException(String message)
    {
        super(message);
    }

    public StepNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
