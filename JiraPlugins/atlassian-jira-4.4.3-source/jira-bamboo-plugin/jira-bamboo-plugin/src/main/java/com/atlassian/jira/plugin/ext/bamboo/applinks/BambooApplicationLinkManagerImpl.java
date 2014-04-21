package com.atlassian.jira.plugin.ext.bamboo.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.api.event.ApplicationLinksIDChangedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BambooApplicationLinkManagerImpl implements BambooApplicationLinkManager, InitializingBean, DisposableBean
{
    private static final Logger log = Logger.getLogger(BambooApplicationLinkManagerImpl.class);
    static final String JBAM_ASSOCIATIONS = "jbam-associations";

    private final ApplicationLinkService applicationLinkService;
    private final EventPublisher eventPublisher;

    private final Map<String, ApplicationId> projectApplicationLinks;

    public BambooApplicationLinkManagerImpl(ApplicationLinkService applicationLinkService, EventPublisher eventPublisher)
    {
        this.applicationLinkService = applicationLinkService;
        this.eventPublisher = eventPublisher;
        this.projectApplicationLinks = new HashMap<String, ApplicationId>();
    }

    public boolean hasApplicationLinks()
    {
        return getApplicationLinkCount() > 0;
    }

    public Iterable<ApplicationLink> getApplicationLinks()
    {
        return applicationLinkService.getApplicationLinks(BambooApplicationType.class);
    }

    public int getApplicationLinkCount()
    {
        return Iterables.size(getApplicationLinks());
    }

    public ApplicationLink getApplicationLink(String projectKey)
    {
        if (projectApplicationLinks.containsKey(projectKey))
        {
            ApplicationId appId = projectApplicationLinks.get(projectKey);

            try
            {
                return applicationLinkService.getApplicationLink(appId);
            }
            catch (TypeNotInstalledException e)
            {
                log.warn("Application link cannot be found for project '" + projectKey + "' and applicationId '" + appId + "'.");
            }
        }

        return getDefaultApplicationLink();
    }

    public ApplicationLink getBambooApplicationLink(String appId)
    {
        try
        {
            ApplicationLink applicationLink = applicationLinkService.getApplicationLink(new ApplicationId(appId));
            if (!(BambooApplicationType.class.isAssignableFrom(applicationLink.getType().getClass())))
            {
                //only return Bamboo application links
                return null;
            }

            return applicationLink;
        }
        catch(TypeNotInstalledException e)
        {
            //application link doesn't exist. return null.
            return null;
        }

    }

    public Iterable<String> getProjects(String appId)
    {
        ApplicationLink applink = lookupApplicationLink(new ApplicationId(appId));
        final Object projectKeys = applink.getProperty(JBAM_ASSOCIATIONS);
        if (projectKeys == null)
        {
            return ImmutableList.of();
        }
        else
        {
            return (List<String>) projectKeys;
        }
    }

    public boolean hasAssociatedProjects(String appId)
    {
        return !Iterables.isEmpty(getProjects(appId));
    }

    public boolean hasAssociatedApplicationLink(String projectKey)
    {
        return projectApplicationLinks.containsKey(projectKey);
    }

    public boolean isAssociated(String projectKey, ApplicationId applicationId)
    {
        return projectApplicationLinks.containsKey(projectKey) && projectApplicationLinks.get(projectKey).equals(applicationId);
    }

    public void associate(String projectKey, ApplicationId appId)
    {
        ApplicationLink applink = lookupApplicationLink(appId);
        final Object projectKeys = applink.getProperty(JBAM_ASSOCIATIONS);
        if (projectKeys == null)
        {
            applink.putProperty(JBAM_ASSOCIATIONS, ImmutableList.of(projectKey));
        }
        else
        {
            List<String> projectKeysList = (List<String>) projectKeys;
            applink.putProperty(JBAM_ASSOCIATIONS, ImmutableList.builder().addAll(projectKeysList).add(projectKey).build());
        }

        projectApplicationLinks.put(projectKey, appId);

        log.info("Associated project '" + projectKey + "' with application link '" + appId + "'.");
    }

    public void unassociateAll(ApplicationId appId)
    {
        ApplicationLink applink = lookupApplicationLink(appId);
        Object projectKeys = applink.getProperty(JBAM_ASSOCIATIONS);
        if (projectKeys != null)
        {
            List<String> projectKeysList = (List<String>) projectKeys;
            for (String projectKey : projectKeysList)
            {
                projectApplicationLinks.remove(projectKey);
            }

            applink.removeProperty(JBAM_ASSOCIATIONS);
        }

        log.info("Unassociated all projects with application link '" + appId + "'.");
    }

    @EventListener
    public void onEvent(ApplicationLinksIDChangedEvent event)
    {
        ApplicationId oldId = event.getOldApplicationId();
        ApplicationId newId = event.getApplicationId();
        Object associations = event.getApplicationLink().getProperty(JBAM_ASSOCIATIONS);

        //if we have any applinks set up for these project, update the references
        if (associations != null)
        {
            List<String> projectKeys = (List<String>) associations;
            for (String projectKey : projectKeys)
            {
                log.info("Updating application link for '" + projectKey + "' from '" + oldId + "' to '" + newId + "'.");
                projectApplicationLinks.remove(projectKey);
                projectApplicationLinks.put(projectKey, newId);
            }
        }
    }

    private ApplicationLink lookupApplicationLink(ApplicationId appId)
    {
        try
        {
            return applicationLinkService.getApplicationLink(appId);
        }
        catch (TypeNotInstalledException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Called upon startup.
     */
    public void afterPropertiesSet() throws Exception
    {
        //build the initial projectKey to applicationId map
        for (ApplicationLink applink : getApplicationLinks())
        {
            Object projectKeys = applink.getProperty(JBAM_ASSOCIATIONS);
            if (projectKeys != null)
            {
                List<String> projectKeyList = (List<String>) projectKeys;
                for (String projectKey : projectKeyList)
                {
                    projectApplicationLinks.put(projectKey, applink.getId());
                }
            }
        }

        eventPublisher.register(this);
    }

    /**
     * Called upon shutdown.
     */
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    private ApplicationLink getDefaultApplicationLink()
    {
        return applicationLinkService.getPrimaryApplicationLink(BambooApplicationType.class);
    }
}
