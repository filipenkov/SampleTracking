package com.atlassian.gadgets.dashboard.internal.impl;

import java.net.URI;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardTab;
import com.atlassian.gadgets.dashboard.internal.Tab;

public class TabImpl implements Tab
{
    private final DashboardTab tab;
    private final boolean writable;

    public TabImpl(DashboardTab tab, boolean writable)
    {
        this.tab = tab;
        this.writable = writable;
    }

    public DashboardId getDashboardId()
    {
        return tab.getDashboardId();
    }

    public String getTitle()
    {
        return tab.getTitle();
    }

    public URI getTabUri()
    {
        return tab.getTabUri();
    }

    public boolean isWritable()
    {
        return writable;
    }
}
