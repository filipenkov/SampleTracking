/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user;


/**
 * Thrown when User/Group details are updated that cannot be changed by
 * underlying provider.
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.1.1.1 $
 */
@Deprecated
public class ImmutableException extends Exception {
    //~ Constructors ///////////////////////////////////////////////////////////

    public ImmutableException() {
    }

    public ImmutableException(String msg) {
        super(msg);
    }
}
