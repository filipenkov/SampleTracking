/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user.provider;


/**
 * The CredentialsProvider is a UserProvider specifically used for storing
 * details for authenticating Users. All Entities referred to are of type
 * User, and all Entity.Accessor objects can be safely cast to User.Accessor
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.3 $
 *
 * @see com.opensymphony.user.provider.UserProvider
 * @see com.opensymphony.user.User
 */
@Deprecated
public interface CredentialsProvider extends UserProvider {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
    * Check password supplied matches that of User.
    */
    boolean authenticate(String name, String password);

    /**
    * Change password of user.
    */
    boolean changePassword(String name, String password);
}
