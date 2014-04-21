/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.provider.AccessProvider;
import com.opensymphony.user.provider.CredentialsProvider;
import com.opensymphony.user.provider.ProfileProvider;

import java.io.Serializable;


/**
 * Superclass for User and Group.
 *
 * <p>Methods common to both User and Group are defined here.</p>
 *
 * <p>When an entity is modified the store() method has to be called to force the
 * provider to persist changes. This is a convenience for Providers and they may choose
 * to write data before then.</p>
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.2 $
 */
@Deprecated
public abstract class Entity implements Serializable {
    //~ Instance fields ////////////////////////////////////////////////////////

    /**
     * Name of entity (unique).
     */
    protected String name;

    /**
     * Whether this entity is mutable (i.e. can be modifed).
     */
    protected boolean mutable = true;
    private final ProviderAccessor providerAccessor;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     * Constructor to be called by User or Group. Should pass across name of Entity
     * and UserManager.Accessor for priveleged access to the UserManager.
     */
    protected Entity(String name, ProviderAccessor providerAccessor) {
        this.name = name;
        this.providerAccessor = providerAccessor;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Retrieve pluggable CredentialsProvider for this entity.
     */
    public CredentialsProvider getCredentialsProvider() {
        return providerAccessor.getCredentialsProvider(name);
    }

    /**
     * Name (unique identifier) of entity.
     * This cannot be changed once the entity has been created.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve pluggable ProfileProvider for this entity.
     */
    public ProfileProvider getProfileProvider() {
        return providerAccessor.getProfileProvider(name);
    }

    /**
     * Extra properties associated with entity. This is managed by ProfileProvider.
     */
    public PropertySet getPropertySet() {
        ProfileProvider profileProvider = getProfileProvider();

        if (!profileProvider.handles(name)) {
            profileProvider.create(name);
        }

        return profileProvider.getPropertySet(name);
    }

    /**
     * Remove this entity from existence.
     * If a provider does not allow removal, ImmutableException shall be thrown.
     */
    public abstract void remove() throws ImmutableException;

    /**
     * Retrieve pluggable AccessProvider for this entity.
     */
    public AccessProvider getAccessProvider() {
        return providerAccessor.getAccessProvider(name);
    }

    /**
     * Determine if entity is mutable. If entity is read-only, false is returned.
     */
    public boolean isMutable() {
        return mutable;
    }

    /**
     * Retrieve underlying UserManager that this User is handled by.
     */
    public UserManager getUserManager() {
        return providerAccessor.getUserManager();
    }

    /**
     * Compare name.
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj.getClass().equals(this.getClass()))) {
            return false;
        } else {
            return name.equals(((Entity) obj).getName());
        }
    }

    /**
     * Hashcode of name.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Force update to underlying data-stores.
     * This allows providers that do not update persistent data on the fly to store changes.
     * If any of the providers are immutable and fields that cannot be updated have changed,
     * ImmutableException shall be thrown.
     */
    public void store() throws ImmutableException {
        if (!mutable) {
            throw new ImmutableException();
        }
    }

    /**
      * String representation returns name.
      */
    public String toString() {
        return name;
    }
}
