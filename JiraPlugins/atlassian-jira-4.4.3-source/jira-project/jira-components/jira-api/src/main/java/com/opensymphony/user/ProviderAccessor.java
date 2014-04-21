/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user;

import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;

import java.io.Serializable;

@Deprecated
public interface ProviderAccessor extends Serializable {
    //~ Methods ////////////////////////////////////////////////////////////////

    public UserManager getUserManager();

    /**
     * Return appropriate AccessProvider for entity.
     */
    AccessProvider getAccessProvider(String name);

    /**
     * Return appropriate CredentialsProvider for entity.
     */
    CredentialsProvider getCredentialsProvider(String name);

    /**
     * Return appropriate ProfileProvider for entity.
     */
    ProfileProvider getProfileProvider(String name);
}
