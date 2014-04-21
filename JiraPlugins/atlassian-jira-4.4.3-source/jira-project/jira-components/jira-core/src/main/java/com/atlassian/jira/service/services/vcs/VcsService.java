/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.vcs;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.vcs.RepositoryManager;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

public class VcsService extends AbstractService
{
    private static final Logger log = org.apache.log4j.Logger.getLogger(VcsService.class);
    private final RepositoryManager repositoryManager;

    public VcsService(RepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }

    public VcsService()
    {
        this(ComponentManager.getInstance().getRepositoryManager());
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("VCSService", "services/com/atlassian/jira/service/services/vcs/vcsservice.xml", null);
    }

    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);
    }

    public void run()
    {
        if (Boolean.getBoolean(SystemPropertyKeys.DISABLE_VCS_POLLING_SYSTEM_PROPERTY))
        {
            log.info("Version control polling (VcsService) disabled by '"+ SystemPropertyKeys.DISABLE_VCS_POLLING_SYSTEM_PROPERTY+"' property.");
            return;
        }
        log.debug("VcsService service running...");
        try
        {
            // Update all the repositories
            repositoryManager.updateRepositories();
        }
        catch (GenericEntityException e)
        {
            log.error("Error occurred while running VcsService.", e);
        }
        catch (OutOfMemoryError e)
        {
            log.error("OutOfMemoryError while updating the repositories. Start the app server with more memory (see '-Xmx' parameter of the 'java' command.)", e);
        }

        log.debug("VcsService service finished.");
    }

    public boolean isUnique()
    {
        // Only one of these should be around - this service updates all the repositories that exist in the system
        return true;
    }

    public boolean isInternal()
    {
        // This service should not be deletable from the UI.
        return true;
    }
}
