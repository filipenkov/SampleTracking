/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user;


/**
 * Signifies an entity cannot be created because
 * another entity already exists with the same name and type.
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.1.1.1 $
 */
@Deprecated
public class DuplicateEntityException extends Exception {
    //~ Constructors ///////////////////////////////////////////////////////////

    public DuplicateEntityException() {
    }

    public DuplicateEntityException(String msg) {
        super(msg);
    }
}
