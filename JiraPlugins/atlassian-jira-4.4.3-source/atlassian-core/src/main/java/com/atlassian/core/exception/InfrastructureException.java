/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd.
 * All rights reserved.
 */
package com.atlassian.core.exception;


/**
 * @author    Ara Abrahamian (ara_e_w@yahoo.com)
 * @created   May 15, 2003
 * @version   Revision: 1.1.1.1 $
 */
public class InfrastructureException extends RuntimeException
{
    //~ Constructors ---------------------------------------------------------------------------------------------------

    public InfrastructureException(Throwable cause)
    {
        super(cause);
    }

    public InfrastructureException(String msg)
    {
        super(msg);
    }

    public InfrastructureException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
