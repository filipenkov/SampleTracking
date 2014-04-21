package com.atlassian.upm.license.internal.host;

import com.atlassian.upm.license.internal.HostApplicationDescriptor;

public class RefappApplicationDescriptor implements HostApplicationDescriptor
{
    @Override
    public int getActiveUserCount()
    {
        return 1;
    }
}
