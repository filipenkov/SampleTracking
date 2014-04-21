package com.atlassian.plugins.rest.common.expand.entity;

import com.atlassian.plugins.rest.common.expand.parameter.Indexes;

import java.util.List;

public interface ListWrapperCallback<T>
{
    List<T> getItems(Indexes indexes);

}
