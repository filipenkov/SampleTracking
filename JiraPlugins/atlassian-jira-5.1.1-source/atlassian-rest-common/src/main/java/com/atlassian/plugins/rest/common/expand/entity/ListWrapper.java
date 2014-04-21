package com.atlassian.plugins.rest.common.expand.entity;

public interface ListWrapper<T>
{
    ListWrapperCallback<T> getCallback();
}
