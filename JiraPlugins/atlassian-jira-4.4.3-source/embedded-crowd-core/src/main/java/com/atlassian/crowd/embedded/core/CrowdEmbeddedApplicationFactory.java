package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.model.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrowdEmbeddedApplicationFactory implements ApplicationFactory
{
    private static final Logger log = LoggerFactory.getLogger(CrowdEmbeddedApplicationFactory.class);
    private static final String APPLICATION_NAME = "crowd-embedded";

    private final ApplicationDAO applicationDao;

    public CrowdEmbeddedApplicationFactory(final ApplicationDAO applicationDao)
    {
        this.applicationDao = applicationDao;

    }
    public Application getApplication()
    {
        try
        {
            final Application application = applicationDao.findByName(APPLICATION_NAME);
            return application;
        }
        catch (ApplicationNotFoundException e)
        {
            // This should only happen during initial system setup.
            // The embedding application should always provide an application once fully set up.
            log.debug("Crowd application : " + APPLICATION_NAME + " not found.");
            return null;
        }
    }

}