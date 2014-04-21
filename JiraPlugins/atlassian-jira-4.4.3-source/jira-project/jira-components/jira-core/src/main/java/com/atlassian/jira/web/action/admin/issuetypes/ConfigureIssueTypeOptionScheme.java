package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@WebSudoRequired
public class ConfigureIssueTypeOptionScheme extends ConfigureOptionScheme
{
    private final ComponentFactory componentFactory;
    private List<Project> associatedProjects;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ConfigureIssueTypeOptionScheme(final FieldConfigSchemeManager configSchemeManager,
            final IssueTypeSchemeManager issueTypeSchemeManager, final FieldManager fieldManager,
            final OptionSetManager optionSetManager, final IssueTypeManageableOption manageableOptionType,
            final BulkMoveOperation bulkMoveOperation, final SearchProvider searchProvider,
            final ConstantsManager constantsManager, final IssueManager issueManager, final ComponentFactory factory)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType, bulkMoveOperation, searchProvider,
            constantsManager, issueManager);
        componentFactory = factory;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods

    @Override
    protected void doValidation()
    {
        super.doValidation();

        if (CollectionUtils.exists(issueTypeSchemeManager.getAllSchemes(), new FieldConfigPredicate(getSchemeId(), getName())))
        {
            addError("name", getText("admin.errors.issuetypes.duplicate.name"));
        }

        if ((getSelectedOptions() != null) && (getSelectedOptions().length > 0))
        {
            boolean hasNormalIssueType = false;
            for (int i = 0; i < getSelectedOptions().length; i++)
            {
                final String id = getSelectedOptions()[i];
                final IssueType issueType = constantsManager.getIssueTypeObject(id);
                if (!issueType.isSubTask())
                {
                    hasNormalIssueType = true;
                    break;
                }
            }

            if (!hasNormalIssueType)
            {
                addErrorMessage(getText("admin.errors.issuetypes.must.select.standard.issue.type"));
            }
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        // Find all possibly affected issues.
        final List associatedProjects = getConfigScheme().getAssociatedProjects();
        if ((associatedProjects != null) && !associatedProjects.isEmpty())
        {
            final List projectIds = GenericValueUtils.transformToLongIdsList(associatedProjects);
            final Collection obseleteOptions = CollectionUtils.subtract(getOriginalOptions(), getNewOptions());
            if ((obseleteOptions != null) && !obseleteOptions.isEmpty())
            {
                final List obseleteOptionIds = new ArrayList(obseleteOptions.size());
                for (final Iterator iterator = obseleteOptions.iterator(); iterator.hasNext();)
                {
                    final Option option = (Option) iterator.next();
                    obseleteOptionIds.add(option.getId());
                }

                final Query query = getQuery(projectIds, obseleteOptionIds);
                final SearchResults searchResults = searchProvider.search(query, getRemoteUser(), PagerFilter.getUnlimitedFilter());
                final List affectedIssues = searchResults.getIssues();
                if ((affectedIssues != null) && !affectedIssues.isEmpty())
                {
                    // Prepare for Update
                    configScheme = new FieldConfigScheme.Builder(getConfigScheme()).setName(getName()).setDescription(getDescription()).toFieldConfigScheme();
                    final List optionIds = new ArrayList(Arrays.asList(getSelectedOptions()));

                    return migrateIssues(this, affectedIssues, optionIds);
                }
            }
        }

        return super.doExecute();
    }

    // -------------------------------------------------------------------------------------------------- View Helpers
    @Override
    public String getIconurl()
    {
        final String iconurl = super.getIconurl();

        if (StringUtils.isBlank(iconurl))
        {
            return ViewIssueTypes.NEW_ISSUE_TYPE_DEFAULT_ICON;
        }
        else
        {
            return iconurl;
        }
    }

    public List<Project> getUsedIn()
    {
        if (associatedProjects == null)
        {
            ProjectIssueTypeSchemeHelper helper = componentFactory.createObject(ProjectIssueTypeSchemeHelper.class);
            associatedProjects = helper.getProjectsUsingScheme(getConfigScheme());
        }
        return associatedProjects;
    }

    /**
     * Whether or not you're allowed to add or remove an option to the current list
     * @return boolean for
     */
    @Override
    public boolean isAllowEditOptions()
    {
        return !issueTypeSchemeManager.getDefaultIssueTypeScheme().getId().equals(getSchemeId());
    }
}