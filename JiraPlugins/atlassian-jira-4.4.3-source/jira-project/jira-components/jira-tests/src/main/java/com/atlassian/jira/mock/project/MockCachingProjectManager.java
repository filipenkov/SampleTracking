/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.project;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.component.DefaultProjectComponentManager;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.project.CachingProjectManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;

import java.util.Collection;

public class MockCachingProjectManager extends CachingProjectManager
{
    public MockCachingProjectManager(ProjectManager decoratedProjectManager)
    {
        super(decoratedProjectManager, (ProjectComponentManager) ComponentManager.getComponentInstanceOfType(DefaultProjectComponentManager.class), (ProjectFactory) ComponentManager.getComponentInstanceOfType(ProjectFactory.class), null, null, null);
    }

    public Collection noNull(Collection col)
    {
        return super.noNull(col);
    }
}
