package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.option.ComponentOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * A search renderer for the component searcher
 *
 * @since v4.0
 */
public class ComponentSearchRenderer extends AbstractProjectConstantsRenderer implements SearchRenderer
{
    private static final Logger log = Logger.getLogger(ComponentSearchRenderer.class);
    private final SimpleFieldSearchConstantsWithEmpty searchConstants;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ProjectComponentManager componentManager;
    private final ProjectManager projectManager;
    private final ComponentResolver componentResolver;


    public ComponentSearchRenderer(SimpleFieldSearchConstantsWithEmpty searchConstants,
            VelocityRequestContextFactory velocityRequestContextFactory, FieldVisibilityManager fieldVisibilityManager,
            ApplicationProperties applicationProperties, VelocityManager velocityManager,
            String searcherNameKey, ProjectComponentManager componentManager, ProjectManager projectManager,
            ComponentResolver componentResolver)
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, fieldVisibilityManager, searchConstants, searcherNameKey);
        this.searchConstants = searchConstants;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.componentManager = componentManager;
        this.projectManager = projectManager;
        this.componentResolver = componentResolver;
    }

    public String getEditHtml(final User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("selectedValues", fieldValuesHolder.get(searchConstants.getSearcherId()));
        velocityParams.put("selectListOptions", getSelectListOptions(searcher, searchContext));
        return renderEditTemplate("project-constants-searcher-edit.vm", velocityParams);
    }

    public String getViewHtml(final User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("selectedObjects", getSelectedObjects(fieldValuesHolder, new ComponentLabelFunction(searcher)));
        velocityParams.put("baseurl", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
        return renderViewTemplate("project-constants-searcher-view.vm", velocityParams);
    }

    @Override
    protected Map<String, Object> getVelocityParams(final User searcher, SearchContext searchContext, FieldLayoutItem fieldLayoutItem, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        final Map<String, Object> velocityParams = super.getVelocityParams(searcher, searchContext, fieldLayoutItem, fieldValuesHolder, displayParameters, action);
        velocityParams.put("extraOption", EasyMap.build("value", getI18n(searcher).getText("common.filters.nocomponent"), "key", "-1"));
        return velocityParams;
    }

    protected List<Option> getSelectListOptions(final User searcher, SearchContext searchContext)
    {
        if (searchContext.isSingleProjectContext())
        {
            Long projectId = searchContext.getProjectIds().iterator().next();
            if (projectManager.getProjectObj(projectId) != null)
            {
                return CollectionUtil.transform(componentManager.findAllForProject(projectId), ComponentOption.FUNCTION);
            }
            else
            {
                log.warn("Project for search context " + searchContext + " is invalid");
            }
        }
        return Collections.emptyList();
    }

    protected class ComponentLabelFunction implements Function<String, AbstractProjectConstantsRenderer.GenericProjectConstantsLabel>
    {
        private final User searcher;

        public ComponentLabelFunction(User searcher)
        {
            this.searcher = searcher;
        }

        public GenericProjectConstantsLabel get(final String input)
        {
            if (input.equals(searchConstants.getEmptySelectFlag()))
            {
                return new GenericProjectConstantsLabel(getI18n(searcher).getText("navigator.hidden.search.request.summary.no.components"));
            }
            else
            {
                Long id = new Long(input);
                ProjectComponent component = componentResolver.get(id);
                if (component != null)
                {
                    Project project = projectManager.getProjectObj(component.getProjectId());
                    return new GenericProjectConstantsLabel(component.getName(), "/browse/" + project.getKey() + "/component/" + component.getId());
                }
                else
                {
                    log.warn("Unknown " + searchConstants.getSearcherId() + " selected. Value: " + id);
                    return null;
                }
            }

        }
    }
}
