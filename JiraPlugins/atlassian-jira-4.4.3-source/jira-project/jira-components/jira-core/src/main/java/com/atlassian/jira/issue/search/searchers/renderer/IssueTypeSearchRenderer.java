package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.AbstractOption;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSet;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.query.Query;
import com.atlassian.util.profiling.UtilTimerStack;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A search renderer for the project system field searcher.
 *
 * @since v4.0
 */
public class IssueTypeSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private final ConstantsManager constantsManager;
    private final PermissionManager permissionManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final OptionSetManager optionSetManager;
    private final SubTaskManager subTaskManager;

    public IssueTypeSearchRenderer(String searcherNameKey, ConstantsManager constantsManager, PermissionManager permissionManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityManager velocityManager, IssueTypeSchemeManager issueTypeSchemeManager,
            OptionSetManager optionSetManager, SubTaskManager subTaskManager)
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, SystemSearchConstants.forIssueType(), searcherNameKey);
        this.constantsManager = constantsManager;
        this.permissionManager = permissionManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.optionSetManager = optionSetManager;
        this.subTaskManager = subTaskManager;
    }

    public String getEditHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        Map velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);

        final List issueTypes = (List) fieldValuesHolder.get(DocumentConstants.ISSUE_TYPE);
        velocityParams.put("selectedIssueTypes", issueTypes != null ? issueTypes : Collections.EMPTY_LIST);

        Map projectToConfig = new ListOrderedMap();
        Set releventConfigs = new HashSet();
        Collection allProjects = getVisibleProjects(searcher);
        for (Iterator iterator = allProjects.iterator(); iterator.hasNext();)
        {
            GenericValue project = (GenericValue) iterator.next();
            FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(project);
            FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();

            releventConfigs.add(relevantConfig);
            projectToConfig.put(project.getLong("id"), relevantConfig.getId());
        }

        final Map<Option, String> optionsToCss = new HashMap<Option, String>();
        final Collection<Option> visibleIssueTypes = getVisibleIssueTypes(searcher, releventConfigs);
        for (Option visibleIssueType : visibleIssueTypes)
        {
            if (visibleIssueType.getImagePath() != null)
            {
                optionsToCss.put(visibleIssueType,  getCssClassForOption(visibleIssueType));                
            }
        }

        velocityParams.put("visibleIssueTypeStyles", optionsToCss);
        velocityParams.put("projectToConfig", projectToConfig);
        velocityParams.put("visibleIssueTypes", visibleIssueTypes);

        return renderEditTemplate("issuetype-searcher" + EDIT_TEMPLATE_SUFFIX, velocityParams);
    }

    public boolean isShown(final User searcher, final SearchContext searchContext)
    {
        return true;
    }

    public String getViewHtml(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        Map velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);

        final Collection unconvertedIssueTypeIds = new ArrayList((Collection) fieldValuesHolder.get(DocumentConstants.ISSUE_TYPE));
        final List<String> types = new ArrayList<String>();
        if (unconvertedIssueTypeIds != null)
        {
            if (unconvertedIssueTypeIds.contains(ConstantsManager.ALL_STANDARD_ISSUE_TYPES))
            {
                types.add(getI18n(searcher).getText("common.filters.standardissuetypes"));
                unconvertedIssueTypeIds.remove(ConstantsManager.ALL_STANDARD_ISSUE_TYPES);
            }

            if (unconvertedIssueTypeIds.contains(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES))
            {
                types.add(getI18n(searcher).getText("common.filters.subtaskissuetypes"));
                unconvertedIssueTypeIds.remove(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES);
            }

            if (!unconvertedIssueTypeIds.isEmpty())
            {
                final List<IssueConstant> issueTypes = constantsManager.convertToConstantObjects("IssueType", unconvertedIssueTypeIds);
                for (IssueConstant issueType : issueTypes)
                {
                    types.add(issueType.getNameTranslation());
                }
            }

            velocityParams.put("selectedIssueTypes", types);
        }

        return renderViewTemplate("issuetype-searcher" + VIEW_TEMPLATE_SUFFIX, velocityParams);
    }

    public boolean isRelevantForQuery(final User searcher, final Query query)
    {
        return isRelevantForQuery(SystemSearchConstants.forIssueType().getJqlClauseNames(), query);
    }

    private Collection getVisibleProjects(final User searcher)
    {
        return permissionManager.getProjects(Permissions.BROWSE, searcher);
    }

    private String getCssClassForOption(Option option)
    {
        UtilTimerStack.push("IssueTypeSearchRenderer.getCssClassForOption");
        try
        {
            Collection allRelatedSchemes = issueTypeSchemeManager.getAllRelatedSchemes(option.getId());

            StringBuffer cssClass = new StringBuffer();
            for (Iterator iterator = allRelatedSchemes.iterator(); iterator.hasNext();)
            {
                FieldConfigScheme configScheme = (FieldConfigScheme) iterator.next();
                FieldConfig config = configScheme.getOneAndOnlyConfig();
                cssClass.append(config.getId()).append(" ");
            }
            return cssClass.toString();
        }
        finally
        {
            UtilTimerStack.pop("IssueTypeSearchRenderer.getCssClassForOption");
        }
    }

    private Collection<Option> getVisibleIssueTypes(final User searcher, Set releventConfigs)
    {
        Set<Option> allOptions = new HashSet<Option>();
        for (Iterator iterator = releventConfigs.iterator(); iterator.hasNext();)
        {
            FieldConfig config = (FieldConfig) iterator.next();
            OptionSet optionsForConfig = optionSetManager.getOptionsForConfig(config);
            allOptions.addAll(optionsForConfig.getOptions());
        }
        // Because we are dealing with IssueType we expect to actually have a List of IssueConstantOption which implements Comparable...
        List<AbstractOption> optionsList = new ArrayList(allOptions);
        Collections.sort(optionsList);

        // Determine if issue types are turned on or off
        if (subTaskManager.isSubTasksEnabled())
        {
            List<Option> issueTypes = new LinkedList<Option>();
            I18nHelper i18nHelper = getI18n(searcher);
            issueTypes.add(new TextOption(ConstantsManager.ALL_STANDARD_ISSUE_TYPES, i18nHelper.getText("common.filters.standardissuetypes"), "sectionHeaderOption"));
            issueTypes.addAll(CollectionUtils.select(optionsList, IssueConstantOption.STANDARD_OPTIONS_PREDICATE));

            // Only add sub-tasks if there are any
            Collection<Option> subTaskOptions = CollectionUtils.select(optionsList, IssueConstantOption.SUB_TASK_OPTIONS_PREDICATE);
            if (subTaskOptions != null && !subTaskOptions.isEmpty())
            {
                issueTypes.add(new TextOption(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES, i18nHelper.getText("common.filters.subtaskissuetypes"), "sectionHeaderOption"));
                issueTypes.addAll(subTaskOptions);
            }

            return issueTypes;
        }
        else
        {
            // Sub-Tasks are turned off - return only non-sub-task issue types
            return CollectionUtils.select(optionsList, IssueConstantOption.STANDARD_OPTIONS_PREDICATE);
        }
    }
}
