/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user.provider;

import com.opensymphony.module.propertyset.PropertySet;


/**
 * The ProfileProvider is a UserProvider specifically used for storing
 * details about a User's profile. All Entities referred to are of type
 * User, and all Entity.Accessor objects can be safely cast to User.Accessor
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.1.1.1 $
 *
 * @see com.opensymphony.user.provider.UserProvider
 * @see com.opensymphony.module.propertyset.PropertySet
 */
@Deprecated
public interface ProfileProvider extends UserProvider {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
    * Retrieve profile for User with given name.
    */
    PropertySet getPropertySet(String name);
}
