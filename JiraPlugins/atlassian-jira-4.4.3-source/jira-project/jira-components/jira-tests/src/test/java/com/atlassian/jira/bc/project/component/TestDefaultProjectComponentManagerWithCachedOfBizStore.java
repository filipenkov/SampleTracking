package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

public class TestDefaultProjectComponentManagerWithCachedOfBizStore
        extends TestDefaultProjectComponentManagerWithOfBizStore
{

    protected ProjectComponentStore createStore(MockOfBizDelegator ofBizDelegator)
    {
        return new CachingProjectComponentStore(new OfBizProjectComponentStore(ofBizDelegator), null);
    }
}
