package com.atlassian.crowd.model.event;

import com.atlassian.crowd.embedded.api.Directory;

public interface OperationEvent
{
    Operation getOperation();

    Directory getDirectory();
}
