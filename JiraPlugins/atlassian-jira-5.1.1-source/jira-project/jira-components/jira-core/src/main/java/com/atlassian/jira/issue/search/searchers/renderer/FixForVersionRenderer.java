package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import webwork.action.Action;

import java.util.Map;

/**
 * The renderer for the Affected Version searcher.
 *
 * @since v4.0
 */
public class FixForVersionRenderer extends AbstractVersionRenderer
{
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public FixForVersionRenderer(ProjectManager projectManager, VersionManager versionManager,
            FieldVisibilityManager fieldVisibilityManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine, String searcherNameKey)
    {
        super(SystemSearchConstants.forFixForVersion(), searcherNameKey, projectManager, versionManager,
                velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, true);
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    public String getEditHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("extraOption", EasyMap.build("value", getI18n(searcher).getText("common.filters.noversion"), "key", VersionManager.NO_VERSIONS));
        velocityParams.put("selectedValues", fieldValuesHolder.get(SystemSearchConstants.forFixForVersion().getUrlParameter()));
        velocityParams.put("selectListOptions", getSelectListOptions(searcher, searchContext));
        return renderEditTemplate("project-constants-searcher-edit.vm", velocityParams);
    }

    public String getViewHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("selectedObjects", getSelectedObjects(fieldValuesHolder, new VersionLabelFunction(searcher, true)));
        velocityParams.put("baseurl", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
        return renderViewTemplate("project-constants-searcher-view.vm", velocityParams);
    }
}
