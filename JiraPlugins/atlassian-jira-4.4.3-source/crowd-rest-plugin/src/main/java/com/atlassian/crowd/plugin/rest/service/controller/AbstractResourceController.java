package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.*;
import com.atlassian.crowd.model.application.*;

/**
 * Abstract resource controller.
 *
 * @since v2.1
 */
public abstract class AbstractResourceController
{
    protected ApplicationService applicationService;
    protected ApplicationManager applicationManager;

    public AbstractResourceController(final ApplicationService applicationService, final ApplicationManager applicationManager)
    {
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
    }

    protected Application getApplication(final String applicationName)
    {
        try
        {
            return applicationManager.findByName(applicationName);
        }
        catch (ApplicationNotFoundException e)
        {
            // application is expected to be found since the application name and password combination should have been
            // validated already
            throw new IllegalStateException("Application " + applicationName + " should exist after the application has been authenticated.", e);
        }
    }
}
