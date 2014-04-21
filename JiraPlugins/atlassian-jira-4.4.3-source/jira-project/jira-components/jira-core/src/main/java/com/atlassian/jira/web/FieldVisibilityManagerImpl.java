package com.atlassian.jira.web;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

/**
 * This is just the FieldVisiblityBean renamed and getting its dependencies injected via PICO
 * constructor injection
 * @since v4.0
 */
public class FieldVisibilityManagerImpl implements FieldVisibilityManager
{
    private static final Logger log = Logger.getLogger(FieldVisibilityManagerImpl.class);

    private final FieldManager fieldManager;
    private final ProjectManager projectManager;

    public FieldVisibilityManagerImpl(final FieldManager fieldManager, final ProjectManager projectManager)
    {
        this.fieldManager = fieldManager;
        this.projectManager = projectManager;
    }

    public boolean isFieldHidden(User remoteUser, String id)
    {
        return fieldManager.isFieldHidden(remoteUser, id);
    }

    public final boolean isFieldHidden(com.opensymphony.user.User remoteUser, String id)
    {
        return fieldManager.isFieldHidden(remoteUser, id);
    }

    public boolean isFieldHidden(String fieldId, GenericValue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        else if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("The entity must be an issue.");
        }

        return isFieldHidden(issue.getLong("project"), fieldId, issue.getString("type"));
    }

    public boolean isFieldHidden(String fieldId, Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        final Project project = issue.getProjectObject();
        final IssueType issueType = issue.getIssueTypeObject();
        if (issueType == null || project == null)
        {
            if (issueType == null)
            {
                log.warn("Issue with id '" + issue.getId() + "' and key '" + issue.getKey() + "' has a null issue type, returning true for isFieldHidden check.");
            }
            if (project == null)
            {
                log.warn("Issue with id '" + issue.getId() + "' and key '" + issue.getKey() + "' has a null project, returning true for isFieldHidden check.");
            }
            return true;
        }
        return isFieldHidden(project.getId(), fieldId, issueType.getId());
    }


    public boolean isCustomFieldHidden(Long projectId, Long customFieldId, String issueTypeId)
    {
        return isFieldHidden(projectId, FieldManager.CUSTOM_FIELD_PREFIX + customFieldId, issueTypeId);
    }

    public boolean isFieldHidden(Long projectId, String fieldId, Long issueTypeId)
    {
        return isFieldHidden(projectId, fieldId, issueTypeId.toString());
    }

    public boolean isFieldHidden(Long projectId, String fieldId, String issueTypeId)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("projectId cannot be null.");
        }

        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        Project project = projectManager.getProjectObj(projectId);

        if (TextUtils.stringSet(issueTypeId))
        {
            final FieldLayoutManager layoutManager = fieldManager.getFieldLayoutManager();
            if (ALL_ISSUE_TYPES.equals(issueTypeId))
            {
                List<String> issueTypes;
                SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
                if (subTaskManager.isSubTasksEnabled())
                {
                    issueTypes = constantsManager.expandIssueTypeIds(EasyList.build(ConstantsManager.ALL_ISSUE_TYPES));
                }
                else
                {
                    issueTypes = constantsManager.expandIssueTypeIds(EasyList.build(ConstantsManager.ALL_STANDARD_ISSUE_TYPES));
                }

                for (String issueType : issueTypes)
                {
                    FieldLayout fieldLayout = layoutManager.getFieldLayout(project, issueType);

                    if (fieldLayout == null || fieldLayout.isFieldHidden(fieldId))
                    {
                        return true;
                    }
                }
                // Field is not hidden in any field layout scheme associated with this project
                return false;
            }
            // Check if field is hidden in layout associated with specific project and issue type
            else
            {
                // Retrieve the field layout associated with specified project and issue type
                FieldLayout fieldLayout = layoutManager.getFieldLayout(project, issueTypeId);

                // Check if the field is present in the list
                return fieldLayout == null || fieldLayout.isFieldHidden(fieldId);
            }
        }
        log.warn("Unable to determine field visibility with project with id '" + projectId + "', issue type with id '" + issueTypeId + "' and field with id '" + fieldId + "'.");
        return true;
    }

    public boolean isFieldHiddenInAllSchemes(Long projectId, String fieldId, List<String> issueTypes)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("projectId cannot be null");
        }

        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        Project project = projectManager.getProjectObj(projectId);
        final FieldLayoutManager layoutManager = fieldManager.getFieldLayoutManager();

        if (issueTypes == null || issueTypes.isEmpty())
        {
            // Scenario 1 - Project specified only
            SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
            if (subTaskManager.isSubTasksEnabled())
            {
                issueTypes = constantsManager.expandIssueTypeIds(EasyList.build(ConstantsManager.ALL_ISSUE_TYPES));
            }
            else
            {
                issueTypes = constantsManager.expandIssueTypeIds(EasyList.build(ConstantsManager.ALL_STANDARD_ISSUE_TYPES));
            }
            for (String issueType : issueTypes)
            {
                FieldLayout fieldLayout = layoutManager.getFieldLayout(project, issueType);

                if (fieldLayout != null && !fieldLayout.isFieldHidden(fieldId))
                {
                    // Field is visible in at least one scheme association
                    return false;
                }
            }

            // Field is hidden in all associated schemes
            return true;
        }
        else
        {
            // Scenario 2 - project and issue type(s) specified
            for (String issueType : issueTypes)
            {
                FieldLayout fieldLayout = layoutManager.getFieldLayout(project, issueType);

                if (fieldLayout != null && !fieldLayout.isFieldHidden(fieldId))
                {
                    // Field is visible in at least one scheme association
                    return false;
                }
            }

            // Field is hidden in all associated schemes
            return true;
        }
    }

    public boolean isFieldHiddenInAllSchemes(Long projectId, String fieldId)
    {
        return isFieldHiddenInAllSchemes(projectId, fieldId, Collections.<String>emptyList());
    }

    public boolean isFieldHiddenInAllSchemes(String fieldId, SearchContext context, User user)
    {
        if (context.isForAnyProjects())
        {
            // Sees if it's hidden in all of the browseable schemes for the user
            return isFieldHidden(user, fieldId);
        }
        else
        {
            // Loop through the projects
            List<Long> projectIds = context.getProjectIds();
            for (final Long projectId : projectIds)
            {

                // Checks if the project exists & is visible to the user
                if (projectManager.getProjectObj(projectId) != null)
                {
                    boolean hidden = isFieldHiddenInAllSchemes(projectId, fieldId, context.getIssueTypeIds());
                    if (!hidden)
                    {
                        return false;
                    }
                }
                else
                {
                    log.warn("Unable to find project with id " + projectId);
                }
            }
            return true;
        }
    }

    public final boolean isFieldHiddenInAllSchemes(String fieldId, SearchContext context, com.opensymphony.user.User user)
    {
        return isFieldHiddenInAllSchemes(fieldId, context, (User) user);
    }

}
