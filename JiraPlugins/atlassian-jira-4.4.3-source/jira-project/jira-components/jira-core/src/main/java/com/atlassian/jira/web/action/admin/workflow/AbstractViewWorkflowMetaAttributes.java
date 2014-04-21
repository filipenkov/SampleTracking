package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Iterator;
import java.util.Map;

public abstract class AbstractViewWorkflowMetaAttributes extends JiraWebActionSupport
{
    private final JiraWorkflow workflow;
    private final WorkflowService workflowService;

    private Map metaAtrributes;
    private String attributeKey;
    private String attributeValue;

    protected AbstractViewWorkflowMetaAttributes(JiraWorkflow workflow, WorkflowService workflowService)
    {
        this.workflow = workflow;
        this.workflowService = workflowService;
    }

    protected void initializeAttributes()
    {
        metaAtrributes = new ListOrderedMap();

        // Remove reserved meta attributes from the map - as they should not be shown
        for (Iterator iterator = getEntityMetaAttributes().entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            final String key = (String) entry.getKey();
            if (!isReservedKey(key))
            {
                metaAtrributes.put(key, entry.getValue());
            }
        }
    }

    protected abstract Map getEntityMetaAttributes();

    protected boolean isReservedKey(final String key)
    {
        if (key == null) return false;
        for (int i = 0; i < JiraWorkflow.JIRA_META_ATTRIBUTE_ALLOWED_LIST.length; i++)
        {
            // Check if our meta attribute starts with one of the allowed prefixes, eg. 'jira.permission'
            String allowedPrefix = JiraWorkflow.JIRA_META_ATTRIBUTE_ALLOWED_LIST[i];
            if (key.equals(allowedPrefix) || key.startsWith(allowedPrefix))
            {
                return false;
            }
        }
        return key.startsWith(JiraWorkflow.JIRA_META_ATTRIBUTE_KEY_PREFIX);
    }

    public Map getMetaAttributes()
    {
        return metaAtrributes;
    }

    @RequiresXsrfCheck
    public String doAddMetaAttribute() throws Exception
    {
        if (!TextUtils.stringSet(attributeKey))
        {
            addError("attributeKey", getText("admin.errors.workflows.attribute.key.must.be.set"));
        }
        else if (isReservedKey(attributeKey))
        {
            addError("attributeKey", getText("admin.errors.workflows.attribute.key.has.reserved.prefix","'" + JiraWorkflow.JIRA_META_ATTRIBUTE_KEY_PREFIX + "'"));
        }
        else if(getEntityMetaAttributes().containsKey(attributeKey))
        {
            addError("attributeKey", getText("admin.errors.workflows.attribute.key.exists","'" + attributeKey + "'"));
        }
        // This is needed due to http://jira.opensymphony.com/browse/WF-476.  We should consider removing this
        // if we upgrade osworkflow to a version that fixes WF-476.
        WorkflowUtil.checkInvalidCharacters(attributeKey, "attributeKey", this);
        WorkflowUtil.checkInvalidCharacters(attributeValue, "attributeValue", this);

        if (invalidInput())
        {
            return INPUT;
        }

        getEntityMetaAttributes().put(attributeKey, attributeValue);
        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getViewRidirect();
    }

    protected abstract String getViewRidirect() throws Exception;

    @RequiresXsrfCheck
    public String doRemoveMetaAttribute() throws Exception
    {
        if (!TextUtils.stringSet(attributeKey))
        {
            addErrorMessage(getText("admin.errors.workflows.attribute.key.must.be.set"));
        }
        else if (!getEntityMetaAttributes().containsKey(attributeKey))
        {
            addErrorMessage(getText("admin.errors.workflows.attribute.key.does.not.exist"));
        }
        else if (isReservedKey(attributeKey))
        {
            addErrorMessage(getText("admin.errors.workflows.cannot.remove.reserved.attribute"));
        }

        if (invalidInput())
            return INPUT;

        getEntityMetaAttributes().remove(attributeKey);
        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getViewRidirect();
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }

    public String getAttributeKey()
    {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey)
    {
        this.attributeKey = attributeKey;
    }

    public String getAttributeValue()
    {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue)
    {
        this.attributeValue = attributeValue;
    }
    
    public String getWorkflowDisplayName()
    {
        return WorkflowUtil.getWorkflowDisplayName(workflow);
    }

    public abstract String getRemoveAttributeUrl(String key);
}
