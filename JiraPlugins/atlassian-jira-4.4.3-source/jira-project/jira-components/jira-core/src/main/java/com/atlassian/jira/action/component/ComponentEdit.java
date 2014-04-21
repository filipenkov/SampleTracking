package com.atlassian.jira.action.component;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.project.AbstractProjectEntityEdit;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.ofbiz.core.entity.GenericEntityException;

/**
 * This class updates the component details
 */
public class ComponentEdit extends AbstractProjectEntityEdit
{
    private String lead;

    /**
     * Update the name of the component and store. Flush all affected issues from the cache so that the new name is
     * picked up
     *
     * @return String to indicate result of action
     */
    protected String doExecute()
    {

        ProjectComponentService projectComponentService = ComponentManager.getComponentInstanceOfType(ProjectComponentService.class);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MutableProjectComponent mutableProjectComponent = MutableProjectComponent.copy(projectComponentService.find(getRemoteUser(), errorCollection, getEntity().getLong("id")));
        mutableProjectComponent.setName(getName());
        mutableProjectComponent.setDescription(getDescription());
        mutableProjectComponent.setLead(getLead());
        projectComponentService.update(getRemoteUser(), errorCollection, mutableProjectComponent);

        // Add any errors that occurred
        addErrorMessages(errorCollection.getErrorMessages());
        addErrors(errorCollection.getErrors());

        try
        {
            //Flush the affected issues from the cache so that the new component will appear
            flushAffectedIssues(IssueRelationConstants.COMPONENT, getEntity());
        }
        catch (GenericEntityException e)
        {
            log.error("cache flush failed for component", e);
        }

        return getResult();
    }

    public String getLead()
    {
        return lead;
    }

    public void setLead(String lead)
    {
        this.lead = lead;
    }
}
