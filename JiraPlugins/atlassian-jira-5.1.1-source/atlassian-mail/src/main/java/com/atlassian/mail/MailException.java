package com.atlassian.mail;

import org.apache.commons.lang.exception.NestableException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 9, 2002
 * Time: 2:24:33 PM
 * To change this template use Options | File Templates.
 */
public class MailException extends NestableException
{
    public MailException()
    {
    }

    public MailException(String s)
    {
        super(s);
    }

    public MailException(Throwable throwable)
    {
        super(throwable);
    }

    public MailException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
