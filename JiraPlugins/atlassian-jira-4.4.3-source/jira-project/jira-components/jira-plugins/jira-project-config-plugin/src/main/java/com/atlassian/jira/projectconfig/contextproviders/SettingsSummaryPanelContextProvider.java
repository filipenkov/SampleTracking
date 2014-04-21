package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraProjectEntityType;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.jira.bc.project.projectoperation.ProjectOperationManager;
import com.atlassian.jira.plugin.projectoperation.PluggableProjectOperation;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.plugin.PluginParseException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Provides velocity context for the "settings" summary panel. This is basically the shit panel that holds everything
 * that does not fit elsewhere.
 *
 * @since v4.4
 */
public class SettingsSummaryPanelContextProvider implements CacheableContextProvider
{
    private static final Logger log = Logger.getLogger(SettingsSummaryPanelContextProvider.class);

    private static final String CONTEXT_PLUGIN_HTML = "pluginsHtml";
    private static final String CONTEXT_REPOS = "repos";
    private static final String CONTEXT_ERROR = "error";

    private final ProjectOperationManager projectOperationManager;
    private final ContextProviderUtils utils;
    private final JiraAuthenticationContext context;
    private final RepositoryManager repositoryManager;
    private final ApplicationLinkService applicationLinkService;
    private final InternalHostApplication hostApplication;

    public SettingsSummaryPanelContextProvider(ProjectOperationManager projectOperationManager,
            ContextProviderUtils utils, JiraAuthenticationContext context, RepositoryManager repositoryManager,
            ApplicationLinkService applicationLinkService, InternalHostApplication hostApplication)
    {
        this.projectOperationManager = projectOperationManager;
        this.utils = utils;
        this.context = context;
        this.repositoryManager = repositoryManager;
        this.applicationLinkService = applicationLinkService;
        this.hostApplication = hostApplication;
    }

    public void init(Map<String, String> configParams) throws PluginParseException
    {
        //nothing to do here.
    }

    public Map<String, Object> getContextMap(Map<String, Object> passedContext)
    {
        final Project project = utils.getProject();
        final List<PluggableProjectOperation> projectOperations =
                projectOperationManager.getVisibleProjectOperations(project, context.getLoggedInUser());

        final MapBuilder<String, Object> contextBuilder = MapBuilder.newBuilder(passedContext);

        final List<String> render = new ArrayList<String>(projectOperations.size());
        for (PluggableProjectOperation projectOperation : projectOperations)
        {
            render.add(projectOperation.getHtml(project, context.getUser()));
        }
        contextBuilder.add(CONTEXT_PLUGIN_HTML, render);

        try
        {
            List<Repository> collection = new ArrayList<Repository>(repositoryManager.getRepositoriesForProject(project.getGenericValue()));
            final Comparator<String> stringComparator = utils.getStringComparator();
            Collections.sort(collection, new Comparator<Repository>()
            {
                public int compare(Repository o1, Repository o2)
                {
                    return stringComparator.compare(o1.getName(), o2.getName());
                }
            });

            contextBuilder.add(CONTEXT_REPOS, collection);
        }
        catch (GenericEntityException e)
        {
            log.error("An error occured while getting repositories from the database.", e);
            contextBuilder.add(CONTEXT_ERROR, true);
        }

        contextBuilder.add("showAppLinks", showAppLinks(project));

        return contextBuilder.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    private boolean showAppLinks(Project project)
    {
        if (applicationLinkService.getApplicationLinks().iterator().hasNext())
        {
            final EntityReference entityReference = hostApplication.toEntityReference(project.getKey(), JiraProjectEntityType.class);
            return hostApplication.canManageEntityLinksFor(entityReference);
        }
        return false;
    }
}
