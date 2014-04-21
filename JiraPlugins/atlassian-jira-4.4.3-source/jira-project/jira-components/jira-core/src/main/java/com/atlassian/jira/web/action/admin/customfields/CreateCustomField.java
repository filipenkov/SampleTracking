/*
 * Atlassian Source Code Template.
 * User: mike
 * Created: 26/09/2002 16:21:17
 * Time: 4:32:34 PM
 */
package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.web.action.JiraWizardActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class CreateCustomField extends JiraWizardActionSupport
{

    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String FIELD_TYPE_PREFIX = "com.atlassian.jira.plugin.system.customfieldtypes:";

    // ------------------------------------------------------------------------------------------------- Type Properties
    private String fieldName;
    private String description;
    private String fieldType;
    private String searcher;

    private boolean global = true;
    private boolean basicMode = true;

    private Long[] projectCategories;
    private Long[] projects = new Long[0];
    private String[] issuetypes = {"-1"};

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ProjectManager projectManager;
    private SubTaskManager subTaskManager;
    private final CustomFieldManager customFieldManager;
    private final ConstantsManager constantsManager;
    private final CustomFieldValidator customFieldValidator;
    private final JiraContextTreeManager treeManager;
    private final ReindexMessageManager reindexMessageManager;
    private final CustomFieldContextConfigHelper customFieldContextConfigHelper;


    // ---------------------------------------------------------------------------------------------------- Constructors
    public CreateCustomField(JiraContextTreeManager treeManager, CustomFieldValidator customFieldValidator, ConstantsManager constantsManager, CustomFieldManager customFieldManager, ProjectManager projectManager, SubTaskManager subTaskManager, final ReindexMessageManager reindexMessageManager, final CustomFieldContextConfigHelper customFieldContextConfigHelper)
    {
        this.treeManager = treeManager;
        this.customFieldValidator = customFieldValidator;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.projectManager = projectManager;
        this.subTaskManager = subTaskManager;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.customFieldContextConfigHelper = notNull("customFieldContextConfigHelper", customFieldContextConfigHelper);
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        String url;
        if (!isFinishClicked() && isNextClicked())
        {
            url =  doCustomFieldType();
        }
        else if(isFinishClicked())
        {
            url =  doAddDetails();
        }
        else
        {
            url =  INPUT;
        }

        if (!invalidInput())
        {
            super.doExecute();
        }

        return url;
    }

    public String doCustomFieldType() throws Exception
    {
        addErrorCollection(customFieldValidator.validateType(getFieldType()));

        if (invalidInput())
        {
            return "input";
        }

        return "details";
    }

    public String doAddDetails() throws Exception
    {
        // Check that the field type is REALLY valid.
        // This was supposed to be checked in the previous step, but doesn't seem to stop you going forward.
        addErrorCollection(customFieldValidator.validateType(getFieldType()));
        addErrorCollection(customFieldValidator.validateDetails(fieldName, fieldType, searcher));

        if (!isGlobal() && (projects == null || projects.length == 0))
        {
            addError("projects", getText("admin.errors.must.select.project.for.non.global.contexts"));
        }


        if (invalidInput())
        {
            return "details";
        }

        CustomFieldSearcher cfs;
        if (ObjectUtils.isValueSelected(searcher))
        {
            cfs = customFieldManager.getCustomFieldSearcher(searcher);
        }
        else
        {
            cfs = null;
        }

        // Add the contexts
        List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(isGlobal(),
                                                                getProjectCategories(),
                                                                getProjects(),
                                                                treeManager);


        // Add the issue types
        List<GenericValue> returnIssueTypes = CustomFieldUtils.buildIssueTypes(constantsManager, getIssuetypes());

        CustomField customField = customFieldManager.createCustomField(fieldName, description, getCustomFieldType(), cfs, contexts, returnIssueTypes);

        // if the resultant context contains issues, then we must also add a reindex message
        if (customFieldContextConfigHelper.doesAddingContextToCustomFieldAffectIssues(getRemoteUser(), customField, contexts, returnIssueTypes, true))
        {
            reindexMessageManager.pushMessage(getRemoteUser(), "admin.notifications.task.custom.fields");
        }

        return getRedirect("AssociateFieldToScreens!default.jspa?fieldId="+customField.getId()+"&returnUrl=ViewCustomFields.jspa");
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    public Collection getFieldTypes()
    {
        return customFieldManager.getCustomFieldTypes();
    }


    public List getSearchers()
    {
        return customFieldManager.getCustomFieldSearchers(customFieldManager.getCustomFieldType(getFieldType()));
    }

    public CustomFieldType getCustomFieldType()
    {
        return customFieldManager.getCustomFieldType(getFieldType());
    }

    public Collection getAllProjects() throws Exception
    {
        return projectManager.getProjects();
    }

    public Collection getAllProjectCategories() throws Exception
    {
        return projectManager.getProjectCategories();
    }

    public Collection getAllIssueTypes() throws Exception
    {
        if (subTaskManager.isSubTasksEnabled())
        {
            return constantsManager.getAllIssueTypeObjects();
        }
        else
        {
            return constantsManager.getRegularIssueTypeObjects();
        }
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    /**
     * Returns true if the current field type is not null and valid.
     * This is used as a safety precaution against XSS. See JRA-21173.
     *
     * @return true if the current field type is not null and valid.
     */
    public boolean isFieldTypeValid()
    {
        boolean valid = customFieldValidator.isValidType(getFieldType());
        return valid;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public String getSearcher()
    {
        // If the searcher is null, then pre-pick the first one.
        if (StringUtils.isEmpty(searcher) && StringUtils.isNotEmpty(fieldType) )
        {
            List searchers = getSearchers();
            if (searchers != null && !searchers.isEmpty())
                searcher = ((CustomFieldSearcher)searchers.iterator().next()).getDescriptor().getCompleteKey();
        }

        return searcher;
    }

    public void setSearcher(String searcher)
    {
        this.searcher = searcher;
    }


    public Long[] getProjects()
    {
        return projects;
    }

    public void setProjects(Long[] projects)
    {
        this.projects = projects;
    }

    public String[] getIssuetypes()
    {
        return issuetypes;
    }

    public void setIssuetypes(String[] issuetypes)
    {
        this.issuetypes = issuetypes;
    }

    public Map getGlobalContextOption()
    {
        return CustomFieldContextManagementBean.getGlobalContextOption();
    }

    public boolean isGlobal()
    {
        return global;
    }

    public void setGlobal(boolean global)
    {
        this.global = global;
    }

    public boolean isBasicMode()
    {
        return basicMode;
    }

    public void setBasicMode(boolean basicMode)
    {
        this.basicMode = basicMode;
    }

    public Long[] getProjectCategories()
    {
        return projectCategories;
    }

    public void setProjectCategories(Long[] projectCategories)
    {
        this.projectCategories = projectCategories;
    }

    public int getTotalSteps()
    {
        return 2;
    }
}

