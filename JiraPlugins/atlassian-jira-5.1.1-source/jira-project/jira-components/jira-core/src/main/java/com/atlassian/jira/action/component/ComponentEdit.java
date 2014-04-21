package com.atlassian.jira.action.component;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.project.AbstractProjectEntityEdit;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * This class updates the component details
 *
 * @deprecated Use {@link com.atlassian.jira.bc.project.component.ProjectComponentService#update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.util.ErrorCollection, com.atlassian.jira.bc.project.component.MutableProjectComponent)}
 *              or {@link com.atlassian.jira.bc.project.component.ProjectComponentManager#update(com.atlassian.jira.bc.project.component.MutableProjectComponent)} instead. Since v5.0.
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
        final MutableProjectComponent mutableProjectComponent = MutableProjectComponent.copy(projectComponentService.find(getLoggedInUser(), errorCollection, getEntity().getLong("id")));
        mutableProjectComponent.setName(getName());
        mutableProjectComponent.setDescription(getDescription());
        mutableProjectComponent.setLead(getLead());
        projectComponentService.update(getLoggedInUser(), errorCollection, mutableProjectComponent);

        // Add any errors that occurred
        addErrorMessages(errorCollection.getErrorMessages());
        addErrors(errorCollection.getErrors());

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
