package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@WebSudoRequired
public class ConfigureFieldScreenScheme extends AbstractFieldScreenSchemeItemAction
{
    private List addableIssueOperations;
    private final ProjectIssueTypeScreenSchemeHelper helper;
    private List<Project> projects;

    public ConfigureFieldScreenScheme(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager,
            final ProjectIssueTypeScreenSchemeHelper helper)
    {
        super(fieldScreenSchemeManager, fieldScreenManager);
        this.helper = helper;
    }

    protected void doValidation()
    {
        validateId();
    }

    public String doExecute() throws Exception
    {
        return getResult();
    }

    @RequiresXsrfCheck
    public String doAddFieldScreenSchemeItem()
    {
        validateIssueOperationId();

        validateFieldScreenId();

        if (!invalidInput())
        {
            FieldScreenSchemeItem fieldScreenSchemeItem = new FieldScreenSchemeItemImpl(getFieldScreenSchemeManager(), getFieldScreenManager());
            if (getIssueOperationId() != null)
                fieldScreenSchemeItem.setIssueOperation(IssueOperations.getIssueOperation(getIssueOperationId()));
            else
                fieldScreenSchemeItem.setIssueOperation(null);

            fieldScreenSchemeItem.setFieldScreen(getFieldScreenManager().getFieldScreen(getFieldScreenId()));
            getFieldScreenScheme().addFieldScreenSchemeItem(fieldScreenSchemeItem);
            return redirectToView();
        }

        return getResult();
    }

    // This is here to overcome the annoying webwork hack to lookup the stack if null is returned!
    public Long getIssueOperaionId(ScreenableIssueOperation issueOperation)
    {
        return issueOperation.getId();
    }

    public Collection getAddableIssueOperations()
    {
        if (addableIssueOperations == null)
        {
            addableIssueOperations = new LinkedList();
            addableIssueOperations.add(new DefaultIssueOperation());
            addableIssueOperations.addAll(IssueOperations.getIssueOperations());

            for (Iterator iterator = getFieldScreenScheme().getFieldScreenSchemeItems().iterator(); iterator.hasNext();)
            {
                FieldScreenSchemeItem fieldScreenSchemeItem = (FieldScreenSchemeItem) iterator.next();
                IssueOperation issueOperation = fieldScreenSchemeItem.getIssueOperation();
                if (issueOperation != null)
                {
                    addableIssueOperations.remove(issueOperation);
                }
                else
                {
                    addableIssueOperations.remove(new DefaultIssueOperation());
                }
            }
        }

        return addableIssueOperations;
    }

    @RequiresXsrfCheck
    public String doDeleteFieldScreenSchemeItem()
    {
        validateIssueOperationId();

        if (!invalidInput())
        {
            getFieldScreenScheme().removeFieldScreenSchemeItem(getIssueOperation());
            return redirectToView();
        }

        return getResult();
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final FieldScreenScheme fieldScreenScheme = getFieldScreenScheme();
            projects = helper.getProjectsForFieldScreenScheme(fieldScreenScheme);
        }
        return projects;
    }


    private static class DefaultIssueOperation implements ScreenableIssueOperation
    {
        private String nameKey = "admin.common.words.default";
        private String description = "";

        public Long getId()
        {
            return null;
        }

        public String getNameKey()
        {
            return nameKey;
        }

        public String getDescriptionKey()
        {
            return description;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof IssueOperation)) return false;

            final ScreenableIssueOperation issueOperation = (ScreenableIssueOperation) o;

            if (description != null ? !description.equals(issueOperation.getDescriptionKey()) : issueOperation.getDescriptionKey() != null) return false;
            if (issueOperation.getId() != null) return false;
            if (nameKey != null ? !nameKey.equals(issueOperation.getNameKey()) : issueOperation.getNameKey() != null) return false;

            return true;
        }

        public int hashCode()
        {
            int result = 0;
            result = 29 * result + (nameKey != null ? nameKey.hashCode() : 0);
            result = 29 * result + (description != null ? description.hashCode() : 0);
            return result;
        }
    }
}
