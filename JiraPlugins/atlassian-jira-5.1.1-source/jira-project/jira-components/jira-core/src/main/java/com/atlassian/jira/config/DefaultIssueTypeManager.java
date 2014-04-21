package com.atlassian.jira.config;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.config.IssueTypeCreatedEvent;
import com.atlassian.jira.event.config.IssueTypeDeletedEvent;
import com.atlassian.jira.event.config.IssueTypeUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public class DefaultIssueTypeManager extends AbstractIssueConstantsManager<IssueType> implements IssueTypeManager
{
    private final TranslationManager translationManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectManager projectManager;
    private final WorkflowManager workflowManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final CustomFieldManager customFieldManager;
    private EventPublisher eventPublisher;

    public DefaultIssueTypeManager(ConstantsManager constantsManager, OfBizDelegator ofBizDelegator, IssueIndexManager issueIndexManager, TranslationManager translationManager, JiraAuthenticationContext jiraAuthenticationContext, ProjectManager projectManager, WorkflowManager workflowManager, FieldLayoutManager fieldLayoutManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, IssueTypeSchemeManager issueTypeSchemeManager, WorkflowSchemeManager workflowSchemeManager, FieldConfigSchemeManager fieldConfigSchemeManager, CustomFieldManager customFieldManager, EventPublisher eventPublisher)
    {
        super(constantsManager, ofBizDelegator, issueIndexManager);
        this.translationManager = translationManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectManager = projectManager;
        this.workflowManager = workflowManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.customFieldManager = customFieldManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public synchronized IssueType createIssueType(String name, String description, String iconUrl)
    {
        return createIssueTypeAndAddToDefaultScheme(name, description, iconUrl, null);
    }

    @Override
    public synchronized IssueType createSubTaskIssueType(String name, String description, String iconUrl)
    {
        return createIssueTypeAndAddToDefaultScheme(name, description, iconUrl, SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE);
    }

    private IssueType createIssueTypeAndAddToDefaultScheme(String name, String description, String iconUrl, String style)
    {
        Assertions.notBlank("name", name);
        Assertions.notNull("iconUrl", iconUrl);
        for (IssueType it : getIssueTypes())
        {
            if (name.trim().equalsIgnoreCase(it.getName()))
            {
                throw new IllegalStateException("An issue type with the name '" + name + "' exists already.");
            }
        }
        try
        {
            String issueStyleType = StringUtils.trimToNull(style);
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("name", name);
            fields.put("description", description);
            fields.put("iconurl", iconUrl);
            fields.put("style", issueStyleType);
            fields.put("id", getNextStringId());
            GenericValue issueTypeGV = createConstant(fields);
            // Add to default scheme
            issueTypeSchemeManager.addOptionToDefault(issueTypeGV.getString("id"));
            IssueType issueType = new IssueTypeImpl(issueTypeGV, translationManager, jiraAuthenticationContext);
            eventPublisher.publish(new IssueTypeCreatedEvent(issueType, issueStyleType));
            return issueType;
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException("Failed to create issue type with name '" + name + "'", ex);
        }
        finally
        {
            clearCaches();
        }
    }

    @Override
    protected void clearCaches()
    {
        constantsManager.refreshIssueTypes();
    }

    @Override
    public void editIssueType(IssueType issueType, String name, String description, String iconUrl)
    {
        Assertions.notNull("issueType", issueType);
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        for (IssueType it : getIssueTypes())
        {
            if (name.equalsIgnoreCase(it.getName()) && !issueType.getId().equals(issueType.getId()))
            {
                throw new IllegalStateException("Cannot rename issue type. An issue type with the name '" + name + "' exists already.");
            }
        }
        issueType.setName(name);
        issueType.setDescription(description);
        issueType.setIconUrl(iconUrl);
        try
        {
            issueType.getGenericValue().store();
            eventPublisher.publish(new IssueTypeUpdatedEvent(issueType, null));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to update issue type '" + name + "'", e);
        }
        finally
        {
            clearCaches();
        }
    }

    @Override
    public Collection<IssueType> getIssueTypes()
    {
        return constantsManager.getAllIssueTypeObjects();
    }

    @Override
    public void removeIssueType(String id, String newIssueTypeId)
    {
        Assertions.notBlank("id", id);
        IssueType issueType = constantsManager.getIssueTypeObject(id);
        if (issueType == null)
        {
            throw new IllegalArgumentException("An issue type with id '" + id + "' does not exist.");
        }
        if (StringUtils.isNotBlank(newIssueTypeId))
        {
            IssueType newIT = constantsManager.getIssueTypeObject(newIssueTypeId);
            if (newIT == null)
            {
                throw new IllegalArgumentException("An issue type with id '" + newIssueTypeId + "' does not exist.");
            }
        }
        try
        {
            boolean issuesWithThisTypeExist = !getMatchingIssues(issueType).isEmpty();
            if (issuesWithThisTypeExist && getAvailableIssueTypes(issueType).isEmpty())
            {
                throw new IllegalStateException("Cannot remove issue type with id '" + id + "', because there is no alternative issue type available.");
            }

            // - delete all workflow scheme entries associated with this issue type
            // - delete all field layout scheme entries associated with this issue type
            Collection workflowSchemes = workflowSchemeManager.getSchemes();
            for (Iterator iterator = workflowSchemes.iterator(); iterator.hasNext(); )
            {
                GenericValue workflowScheme = (GenericValue) iterator.next();
                Collection entities = workflowSchemeManager.getEntities(workflowScheme);
                for (Iterator iterator1 = entities.iterator(); iterator1.hasNext(); )
                {
                    GenericValue entity = (GenericValue) iterator1.next();
                    if (issueType.getId().equals(entity.getString("issuetype")))
                    {
                        workflowSchemeManager.deleteEntity(entity.getLong("id"));
                    }
                }
            }

            // Go through all field layout schemes and remove the entry of this issue type if one exists
            for (Iterator iterator = fieldLayoutManager.getFieldLayoutSchemes().iterator(); iterator.hasNext(); )
            {
                FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) iterator.next();
                if (fieldLayoutScheme.containsEntity(issueType.getId()))
                {
                    fieldLayoutScheme.removeEntity(issueType.getId());
                }
            }

            // Go through all issue type screen schemes and remove the entry for this issue type if one exists
            for (Iterator iterator = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().iterator(); iterator.hasNext(); )
            {
                IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) iterator.next();
                if (issueTypeScreenScheme.containsEntity(issueType.getId()))
                {
                    issueTypeScreenScheme.removeEntity(issueType.getId());
                }
            }

            //JRA-10461: Safely remove the issueType association to field config schemes.  Need to refresh
            // the customfieldManager after this.
            fieldConfigSchemeManager.removeInvalidFieldConfigSchemesForIssueType(issueType);
            customFieldManager.refresh();

            issueTypeSchemeManager.removeOptionFromAllSchemes(issueType.getId());

            removeConstant("type", issueType, newIssueTypeId);
            eventPublisher.publish(new IssueTypeDeletedEvent(issueType, null));
        }
        catch (Exception ex)
        {
            throw new DataAccessException("Failed to remove issueType with id '" + id + "'", ex);
        }
    }

    @Override
    public IssueType getIssueType(String id)
    {
        Assertions.notBlank("id", id);
        return constantsManager.getIssueTypeObject(id);
    }

    @Override
    public Collection<IssueType> getAvailableIssueTypes(IssueType issueType)
    {
        /**
         * For Enterprise - need to determine if:
         * <p/>
         * 1: Issue Type is associated with one workflow and field layout scheme pair within the system.
         * 2: any other issue types associated with the same workflow and field layout scheme pair exist.
         * <p/>
         * If these requirements are not satisfied - the issues associated with this type can not be moved due to conflicts
         * in status and field layouts will occur.
         * <p/>
         * Handles subtask types also.
         * <p/>
         * Returns a collection of suitable alternative issue types to which matching issues can be moved to.
         */
        return Lists.newArrayList(Iterables.transform(getAvailableIssueTypesGenericValues(issueType), new Function<GenericValue, IssueType>()
        {
            @Override
            public IssueType apply(@Nullable GenericValue from)
            {
                return new IssueTypeImpl(from, translationManager, jiraAuthenticationContext);
            }
        }));
    }

    @Override
    protected String getIssueConstantField()
    {
        return ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE;
    }

    @Override
    protected List<IssueType> getAllValues()
    {
        return Lists.newArrayList(constantsManager.getRegularIssueTypeObjects());
    }

    private Collection<GenericValue> getAvailableIssueTypesGenericValues(IssueType issueType)
    {
        Collection projects = projectManager.getProjects();
        Collection availableIssueTypes = new HashSet();
        try
        {
            // Check workflow and field layout scheme associations of issue type to be deleted
            CheckIssueTypeAssociationsResult checkIssueTypeAssociationsResult = checkIssueTypeAssociations(issueType, projects);
            if (!checkIssueTypeAssociationsResult.valid)
            {
                return Collections.EMPTY_LIST;
            }
            else
            {
                Collection issueTypes;
                // Determine if this is a sub task issue type
                GenericValue issueTypeGV = constantsManager.getIssueType(issueType.getId());
                if (constantsManager.getSubTaskIssueTypes().contains(issueTypeGV))
                {
                    issueTypes = constantsManager.getSubTaskIssueTypes();
                }
                else
                {
                    issueTypes = constantsManager.getIssueTypes();
                }

                // Check workflow and field layout scheme associations of other issue types
                availableIssueTypes = getAlternativeTypes(issueType, issueTypes, projects, checkIssueTypeAssociationsResult.workflow, checkIssueTypeAssociationsResult.fieldScreenScheme, checkIssueTypeAssociationsResult.fieldLayoutId);
            }
            return availableIssueTypes;
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException("Failed to read list of available issue types.", ex);
        }
    }

    private CheckIssueTypeAssociationsResult checkIssueTypeAssociations(IssueType issueType, Collection projects)
            throws WorkflowException, GenericEntityException
    {
        // Ensure issue type to be deleted is associated with same workflow throughout JIRA
        JiraWorkflow workflow = null;
        FieldScreenScheme fieldScreenScheme = null;
        Long fieldLayoutId = null;
        for (Iterator iterator = projects.iterator(); iterator.hasNext(); )
        {
            GenericValue projectGV = (GenericValue) iterator.next();

            if (workflow == null)
            {
                workflow = workflowManager.getWorkflow(projectGV.getLong("id"), issueType.getId());
            }
            else if (!workflow.equals(workflowManager.getWorkflow(projectGV.getLong("id"), issueType.getId())))
            {
                // Different workflow detected - cannot delete issue type
                return new CheckIssueTypeAssociationsResult(false, workflow, fieldScreenScheme, fieldLayoutId);
            }
        }

        int i = 0;
        // Ensure issue type to be deleted is associated with the same field layout scheme throughout JIRA
        for (Iterator iterator = fieldLayoutManager.getFieldLayoutSchemes().iterator(); iterator.hasNext(); )
        {
            FieldLayoutScheme layoutScheme = (FieldLayoutScheme) iterator.next();
            // Only care about this scheme if it is associated with at least one project
            if (!layoutScheme.getProjects().isEmpty())
            {
                if (i == 0)
                {
                    // If this is the first time through the loop simply record the field layout id used
                    fieldLayoutId = layoutScheme.getFieldLayoutId(issueType.getId());
                    // Increment counter to ensure we start comaprison on the next iteration
                    i++;
                }
                else
                {
                    // Ensure the field layout id is the same as the one we looked up during the first iteration
                    if (!ObjectUtils.equalsNullSafe(fieldLayoutId, layoutScheme.getFieldLayoutId(issueType.getId())))
                    {
                        // If it's not then cannot remove issue type
                        return new CheckIssueTypeAssociationsResult(false, workflow, fieldScreenScheme, fieldLayoutId);
                    }
                }
            }
        }

        i = 0;
        // Ensure issue type is using the field screen scheme
        for (Iterator iterator = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().iterator(); iterator.hasNext(); )
        {
            IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) iterator.next();
            // Only care about this scheme if it is associated with at least one project
            if (!issueTypeScreenScheme.getProjects().isEmpty())
            {
                IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueType.getId());
                if (issueTypeScreenSchemeEntity == null)
                {
                    issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(null);
                }

                if (i == 0)
                {
                    // Record the id used
                    fieldScreenScheme = issueTypeScreenSchemeEntity.getFieldScreenScheme();
                    // Increment counter so that the comparison is done next iteration
                    i++;
                }
                else
                {
                    if (!ObjectUtils.equalsNullSafe(fieldScreenScheme, issueTypeScreenSchemeEntity.getFieldScreenScheme()))
                    {
                        return new CheckIssueTypeAssociationsResult(false, workflow, fieldScreenScheme, fieldLayoutId);
                    }
                }
            }
        }

        return new CheckIssueTypeAssociationsResult(true, workflow, fieldScreenScheme, fieldLayoutId);
    }

    private class CheckIssueTypeAssociationsResult
    {
        private CheckIssueTypeAssociationsResult(boolean valid, JiraWorkflow workflow, FieldScreenScheme fieldScreenScheme, Long fieldLayoutId)
        {
            this.valid = valid;
            this.workflow = workflow;
            this.fieldScreenScheme = fieldScreenScheme;
            this.fieldLayoutId = fieldLayoutId;
        }

        public boolean valid = false;
        public JiraWorkflow workflow = null;
        public FieldScreenScheme fieldScreenScheme;
        public Long fieldLayoutId;
    }

    // Return a collection of suitable alternative issue types (each will use the same workflow and field layout scheme and field screen scheme as the issue type to be deleted)
    private Collection getAlternativeTypes(IssueType issueType, Collection issueTypes, Collection projects, JiraWorkflow jiraWorkflow, FieldScreenScheme fieldScreenScheme, Long fieldLayoutId)
            throws WorkflowException
    {
        Collection availableIssueTypes = new HashSet();

        outer:
        for (Iterator issueTypeIterator = issueTypes.iterator(); issueTypeIterator.hasNext(); )
        {
            GenericValue issueTypeGV = (GenericValue) issueTypeIterator.next();
            if (!issueType.getId().equals(issueTypeGV.getString("id")))
            {
                // Ensure possible issue type to move to is used throughout JIRA with the same workflow.
                for (Iterator iterator1 = projects.iterator(); iterator1.hasNext(); )
                {
                    GenericValue projectGV = (GenericValue) iterator1.next();
                    // Compare workflow with workflow of issue type to be deleted
                    if (!jiraWorkflow.equals(workflowManager.getWorkflow(projectGV.getLong("id"), issueTypeGV.getString("id"))))
                    {
                        // If the workflows do not equal cannot change to this issue type so move onto the next issue type
                        continue outer;
                    }
                }
                // Issue Type associated with same workflow - add as possible candidate
                availableIssueTypes.add(issueTypeGV);
            }
        }

        // Go through the list of issue types with the same workflow and check that they use the same field layout
        for (Iterator iterator = availableIssueTypes.iterator(); iterator.hasNext(); )
        {
            GenericValue issueTypeGV = (GenericValue) iterator.next();
            for (Iterator it = fieldLayoutManager.getFieldLayoutSchemes().iterator(); it.hasNext(); )
            {
                FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) it.next();
                // Only care about this scheme if it is associated with at least one project
                if (!fieldLayoutScheme.getProjects().isEmpty())
                {
                    Long flid = fieldLayoutScheme.getFieldLayoutId(issueTypeGV.getString("id"));
                    if (!ObjectUtils.equalsNullSafe(fieldLayoutId, flid))
                    {
                        // Remove the issue type as a possibel issue type to move to
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        // Go through the list of issue types and find the ones that use the same field screen layout
        for (Iterator iterator = availableIssueTypes.iterator(); iterator.hasNext(); )
        {
            GenericValue issueTypeGV = (GenericValue) iterator.next();
            for (Iterator it = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().iterator(); it.hasNext(); )
            {
                IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) it.next();
                // Only care about this scheme if it is associated with at least one project
                if (!issueTypeScreenScheme.getProjects().isEmpty())
                {
                    IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueTypeGV.getString("id"));
                    if (issueTypeScreenSchemeEntity == null)
                    {
                        issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(null);
                    }
                    if (!ObjectUtils.equalsNullSafe(fieldScreenScheme, issueTypeScreenSchemeEntity.getFieldScreenScheme()))
                    {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return availableIssueTypes;
    }

}
