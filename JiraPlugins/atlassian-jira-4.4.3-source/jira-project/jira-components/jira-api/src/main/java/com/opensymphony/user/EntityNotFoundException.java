/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user;


/**
 * Thrown when User/Group is looked up by name that does not exist.
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.1.1.1 $
 */
@Deprecated
public class EntityNotFoundException extends Exception {
    //~ Constructors ///////////////////////////////////////////////////////////

    public EntityNotFoundException() {
    }

    public EntityNotFoundException(String msg) {
        super(msg);
    }
}
