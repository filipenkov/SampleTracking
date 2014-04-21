package com.atlassian.crowd.directory.hybrid;

import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.exception.*;

/**
 * Manages internal group creation and mutation.
 * <p/>
 * An internal group can be either:
 * <ul>
 * <li>a shadow group, or</li>
 * <li>a local group</li>
 * </ul>
 *
 * Subclasses specialise the handling of each type.
 *
 * Common code is manifested in this class.
 */
public abstract class InternalGroupHandler
{    
    public static final String SHADOW_ATTRIBUTE_KEY = InternalGroupHandler.class.getName() + ".shadow";

    private final InternalRemoteDirectory internalDirectory;
    private final boolean localGroupsEnabled;

    protected InternalGroupHandler(InternalRemoteDirectory internalDirectory)
    {
        this.internalDirectory = internalDirectory;
        this.localGroupsEnabled = Boolean.parseBoolean(internalDirectory.getValue(LDAPPropertiesMapper.LOCAL_GROUPS));
    }

    protected InternalRemoteDirectory getInternalDirectory()
    {
        return internalDirectory;
    }

    /**
     * @return <code>true</code> if local groups are enabled.
     */
    public boolean isLocalGroupsEnabled()
    {
        return localGroupsEnabled;
    }
}
