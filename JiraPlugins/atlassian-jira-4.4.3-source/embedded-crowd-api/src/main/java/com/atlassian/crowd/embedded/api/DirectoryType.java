package com.atlassian.crowd.embedded.api;

public enum DirectoryType
{
    UNKNOWN,
    INTERNAL,
    /**
     * LDAP directories
     */
    CONNECTOR,
    CUSTOM,
    DELEGATING
            {
                @Override
                public boolean isExportOfNonLocalGroupsRequired()
                {
                    return false;
                }

            },
    /**
     * Remote crowd directory
     */
    CROWD;

    public boolean isExportOfNonLocalGroupsRequired()
    {
        return true;
    }

}
