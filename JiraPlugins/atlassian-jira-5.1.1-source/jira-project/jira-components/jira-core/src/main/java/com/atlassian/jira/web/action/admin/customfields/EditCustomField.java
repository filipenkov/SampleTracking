package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.Action;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class EditCustomField extends JiraWebActionSupport
{
    private Long id;
    private String name;
    private String description;
    private String searcher;

    private final CustomFieldManager customFieldManager;
    private final CustomFieldService customFieldService;
    private final ReindexMessageManager reindexMessageManager;

    public EditCustomField(CustomFieldService customFieldService, CustomFieldManager customFieldManager, final ReindexMessageManager reindexMessageManager)
    {
        this.customFieldService = customFieldService;
        this.customFieldManager = customFieldManager;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
    }

    public String doDefault() throws Exception
    {
        setName(getCustomField().getName());
        setDescription(getCustomField().getDescription());
        final CustomFieldSearcher currentSearcher = getCurrentSearcher();
        setSearcher(currentSearcher != null ? currentSearcher.getDescriptor().getCompleteKey() : null);
        return Action.INPUT;
    }

    protected void doValidation()
    {
        customFieldService.validateUpdate(getJiraServiceContext(), id, name, description, searcher);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        CustomField updatedField = getCustomField();
        updatedField.setName(getName());
        updatedField.setDescription(getDescription());
        final CustomFieldSearcher oldSearcher = updatedField.getCustomFieldSearcher();
        if (ObjectUtils.isValueSelected(searcher))
        {
            final CustomFieldSearcher newSearcher = customFieldManager.getCustomFieldSearcher(searcher);
            updatedField.setCustomFieldSearcher(newSearcher);

            // if searcher has changed, then we need to push a reindex message
            if (oldSearcher == null || !oldSearcher.getDescriptor().getCompleteKey().equals(newSearcher.getDescriptor().getCompleteKey()))
            {
                reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.custom.fields");
            }
        }
        else
        {
            updatedField.setCustomFieldSearcher(null);
        }
        customFieldManager.updateCustomField(updatedField);

        return getRedirect("ViewCustomFields.jspa");
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long l)
    {
        id = l;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setSearcher(String searcher)
    {
        this.searcher = searcher;
    }

    public String getSearcher()
    {
        return searcher;
    }

    public List getSearchers()
    {
        return customFieldManager.getCustomFieldSearchers((customFieldManager.getCustomFieldObject(getId()).getCustomFieldType()));
    }

    public CustomFieldSearcher getCurrentSearcher()
    {
        return getCustomField().getCustomFieldSearcher();
    }

    public CustomField getCustomField()
    {
        return customFieldManager.getCustomFieldObject(getId());
    }
}