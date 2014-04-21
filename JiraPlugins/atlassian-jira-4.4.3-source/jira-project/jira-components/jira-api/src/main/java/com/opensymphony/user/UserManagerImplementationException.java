/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user;


/**
 * RuntimeException thrown by UserManager/User/Group signifying that
 * an error has occurred with the underlying provider (such as a network
 * problem).
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.1.1.1 $
 */
@Deprecated
public class UserManagerImplementationException extends RuntimeException
{
    //~ Instance fields ////////////////////////////////////////////////////////

    private Throwable cause;

    //~ Constructors ///////////////////////////////////////////////////////////

    public UserManagerImplementationException()
    {
    }

    public UserManagerImplementationException(String s)
    {
        super(s);
    }

    public UserManagerImplementationException(Throwable cause)
    {
        this.cause = cause;
    }

    public UserManagerImplementationException(String s, Throwable cause)
    {
        super(s);
        this.cause = cause;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public Throwable getCause()
    {
        return cause;
    }
}
