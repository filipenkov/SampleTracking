package com.atlassian.crowd.embedded.api;

import com.atlassian.crowd.model.application.Application;

/**
 * <p>Necessary evil as Crowd's {@link com.atlassian.crowd.model.application.Application} is a concrete class.</p>
 * <p>An given instance of this factory should always return the same instance of {@code Application}, so "caching" the retrieved
 * application is fine, if not encouraged.</p>
 */
public interface ApplicationFactory
{
    Application getApplication();
}