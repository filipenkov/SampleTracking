/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@WebSudoRequired
public class DeleteIssueType extends AbstractDeleteConstant
{
    private final FieldLayoutManager fieldLayoutManager;
    private final ProjectManager projectManager;
    private final WorkflowManager workflowManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final CustomFieldManager customFieldManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;

    // Suitable alternative issue types that matching issues can be moved to - once the issue type is deleted
    private Collection availableIssueTypes;

    // Details on issue type to be deleted - used by methods to determine suitable alternative issue types
    boolean defaultLayoutScheme = false;
    JiraWorkflow workflow = null;
    private FieldScreenScheme fieldScreenScheme;
    private Long fieldLayoutId;

    public DeleteIssueType(FieldLayoutManager fieldLayoutManager, ProjectManager projectManager, WorkflowManager workflowManager, WorkflowSchemeManager workflowSchemeManager,
                           IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, CustomFieldManager customFieldManager, IssueTypeSchemeManager issueTypeSchemeManager,
                           FieldConfigSchemeManager fieldConfigSchemeManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.projectManager = projectManager;
        this.workflowManager = workflowManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.customFieldManager = customFieldManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
    }

    protected String getConstantEntityName()
    {
        return "IssueType";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.issuetype.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "type";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getIssueType(id);
    }

    protected IssueType getIssueTypeObject()
    {
        return getConstantsManager().getIssueTypeObject(id);
    }

    protected String getRedirectPage()
    {
        return "ViewIssueTypes.jspa";
    }

    protected Collection getConstants()
    {
        return getConstantsManager().getIssueTypes();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshIssueTypes();
        ManagerFactory.getFieldManager().refresh();
    }

    protected void doValidation()
    {
        try
        {
            if (getMatchingIssues().isEmpty())
            {
                if (getConstant() == null)
                {
                    addErrorMessage(getText("admin.errors.no.constant.found", getNiceConstantName(), id));
                }
            }
            else
            {
                // Validate issue type is associated with one workflow and field layout scheme and that
                // suitable alternative issue types exist
                if (getAvailableIssueTypes().isEmpty())
                {
                    addErrorMessage(getText("admin.errors.issuetypes.no.alternative"));
                }
                super.doValidation();
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred: " + e, e);
            addErrorMessage(getText("admin.errors.error.occurred") + " " + e);
        }
    }

    protected String doExecute() throws Exception
    {
        // - delete all workflow scheme entries associated with this issue type
        // - delete all field layout scheme entries associated with this issue type
        Collection workflowSchemes = workflowSchemeManager.getSchemes();
        for (Iterator iterator = workflowSchemes.iterator(); iterator.hasNext();)
        {
            GenericValue workflowScheme = (GenericValue) iterator.next();
            Collection entities = workflowSchemeManager.getEntities(workflowScheme);
            for (Iterator iterator1 = entities.iterator(); iterator1.hasNext();)
            {
                GenericValue entity = (GenericValue) iterator1.next();
                if (getId().equals(entity.getString("issuetype")))
                {
                    workflowSchemeManager.deleteEntity(entity.getLong("id"));
                }
            }
        }

        // Go through all field layout schemes and remove the entry of this issue type if one exists
        for (Iterator iterator = fieldLayoutManager.getFieldLayoutSchemes().iterator(); iterator.hasNext();)
        {
            FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) iterator.next();
            if (fieldLayoutScheme.containsEntity(getId()))
            {
                fieldLayoutScheme.removeEntity(getId());
            }
        }

        // Go through all issue type screen schemes and remove the entry for this issue type if one exists
        for (Iterator iterator = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().iterator(); iterator.hasNext();)
        {
            IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) iterator.next();
            if (issueTypeScreenScheme.containsEntity(getId()))
            {
                issueTypeScreenScheme.removeEntity(getId());
            }
        }

        //JRA-10461: Safely remove the issueType association to field config schemes.  Need to refresh
        // the customfieldManager after this.
        fieldConfigSchemeManager.removeInvalidFieldConfigSchemesForIssueType(getIssueTypeObject());
        customFieldManager.refresh();

        issueTypeSchemeManager.removeOptionFromAllSchemes(getId());

        return super.doExecute();
    }

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
    public Collection getAvailableIssueTypes() throws GenericEntityException, WorkflowException
    {
        if (availableIssueTypes == null)
        {
            Collection projects = projectManager.getProjects();
            availableIssueTypes = new HashSet();

            // Check workflow and field layout scheme associations of issue type to be deleted
            if (!checkIssueTypeAssociations(projects))
            {
                return Collections.EMPTY_LIST;
            }
            else
            {
                Collection issueTypes;
                // Determine if this is a sub task issue type
                GenericValue issueType = getConstantsManager().getIssueType(getId());
                if (getConstantsManager().getSubTaskIssueTypes().contains(issueType))
                {
                    issueTypes = getConstantsManager().getSubTaskIssueTypes();
                }
                else
                {
                    issueTypes = getConstantsManager().getIssueTypes();
                }

                // Check workflow and field layout scheme associations of other issue types
                availableIssueTypes = getAlternativeTypes(issueTypes, projects);
            }
        }
        
        return availableIssueTypes;
    }

    private Map mapIdToType(Collection issueTypeGVs)
    {
        Map map = new HashMap();
        for (Iterator it = issueTypeGVs.iterator(); it.hasNext();)
        {
            GenericValue gv = (GenericValue) it.next();
            map.put(gv.getString("id"), gv);
        }
        return map;
    }

    // Determine if issue type to be deleted is associated with one workflow and field layout and field screen scheme within the system
    private boolean checkIssueTypeAssociations(Collection projects) throws WorkflowException, GenericEntityException
    {
        // Ensure issue type to be deleted is associated with same workflow throughout JIRA
        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            GenericValue projectGV = (GenericValue) iterator.next();

            if (workflow == null)
            {
                workflow = workflowManager.getWorkflow(projectGV.getLong("id"), getId());
            }
            else if (!workflow.equals(workflowManager.getWorkflow(projectGV.getLong("id"), getId())))
            {
                // Different workflow detected - cannot delete issue type
                return false;
            }
        }

        fieldLayoutId = null;
        int i = 0;
        // Ensure issue type to be deleted is associated with the same field layout scheme throughout JIRA
        for (Iterator iterator = fieldLayoutManager.getFieldLayoutSchemes().iterator(); iterator.hasNext();)
        {
            FieldLayoutScheme layoutScheme = (FieldLayoutScheme) iterator.next();
            // Only care about this scheme if it is associated with at least one project
            if (!layoutScheme.getProjects().isEmpty())
            {
                if (i == 0)
                {
                    // If this is the first time through the loop simply record the field layout id used
                    fieldLayoutId = layoutScheme.getFieldLayoutId(getId());
                    // Increment counter to ensure we start comaprison on the next iteration
                    i++;
                }
                else
                {
                    // Ensure the field layout id is the same as the one we looked up during the first iteration
                    if (!ObjectUtils.equalsNullSafe(fieldLayoutId, layoutScheme.getFieldLayoutId(getId())))
                    {
                        // If it's not then cannot remove issue type
                        return false;
                    }
                }
            }
        }

        i = 0;
        // Ensure issue type is using the field screen scheme
        for (Iterator iterator = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().iterator(); iterator.hasNext();)
        {
            IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) iterator.next();
            // Only care about this scheme if it is associated with at least one project
            if (!issueTypeScreenScheme.getProjects().isEmpty())
            {
                IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(getId());
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
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // Return a collection of suitable alternative issue types (each will use the same workflow and field layout scheme and field screen scheme as the issue type to be deleted)
    private Collection getAlternativeTypes(Collection issueTypes, Collection projects)
            throws WorkflowException
    {
        Collection availableIssueTypes = new HashSet();

        outer:
        for (Iterator issueTypeIterator = issueTypes.iterator(); issueTypeIterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) issueTypeIterator.next();
            if (!getId().equals(issueType.getString("id")))
            {
                // Ensure possible issue type to move to is used throughout JIRA with the same workflow.
                for (Iterator iterator1 = projects.iterator(); iterator1.hasNext();)
                {
                    GenericValue projectGV = (GenericValue) iterator1.next();
                    // Compare workflow with workflow of issue type to be deleted
                    if (!workflow.equals(workflowManager.getWorkflow(projectGV.getLong("id"), issueType.getString("id"))))
                    {
                        // If the workflows do not equal cannot change to this issue type so move onto the next issue type
                        continue outer;
                    }
                }
                // Issue Type associated with same workflow - add as possible candidate
                availableIssueTypes.add(issueType);
            }
        }

        // Go through the list of issue types with the same workflow and check that they use the same field layout
        for (Iterator iterator = availableIssueTypes.iterator(); iterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) iterator.next();
            for (Iterator it = fieldLayoutManager.getFieldLayoutSchemes().iterator(); it.hasNext();)
            {
                FieldLayoutScheme fieldLayoutScheme = (FieldLayoutScheme) it.next();
                // Only care about this scheme if it is associated with at least one project
                if (!fieldLayoutScheme.getProjects().isEmpty())
                {
                    Long flid = fieldLayoutScheme.getFieldLayoutId(issueType.getString("id"));
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
        for (Iterator iterator = availableIssueTypes.iterator(); iterator.hasNext();)
        {
            GenericValue issueType = (GenericValue) iterator.next();
            for (Iterator it = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes().iterator(); it.hasNext();)
            {
                IssueTypeScreenScheme issueTypeScreenScheme = (IssueTypeScreenScheme) it.next();
                // Only care about this scheme if it is associated with at least one project
                if (!issueTypeScreenScheme.getProjects().isEmpty())
                {
                    IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueType.getString("id"));
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
