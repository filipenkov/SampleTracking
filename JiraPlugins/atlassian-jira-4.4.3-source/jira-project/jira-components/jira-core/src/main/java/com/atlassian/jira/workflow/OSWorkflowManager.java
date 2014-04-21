package com.atlassian.jira.workflow;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.DraftWorkflowCreatedEvent;
import com.atlassian.jira.event.DraftWorkflowDeletedEvent;
import com.atlassian.jira.event.DraftWorkflowPublishedEvent;
import com.atlassian.jira.event.WorkflowCopiedEvent;
import com.atlassian.jira.event.WorkflowDeletedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.TransactionRuntimeException;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.InvalidEntryStateException;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.InvalidRoleException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.basic.BasicWorkflow;
import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.config.DefaultConfiguration;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.SimpleStep;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OSWorkflowManager implements WorkflowManager
{
    private static final Logger log = Logger.getLogger(OSWorkflowManager.class);

    private volatile Configuration configuration;
    private final DraftWorkflowStore draftWorkflowStore;
    private final EventPublisher eventPublisher;

    // This constructor is used by (very old) unit tests.
    public OSWorkflowManager(DraftWorkflowStore draftWorkflowStore, EventPublisher eventPublisher)
    {
        resetConfiguration();
        this.draftWorkflowStore = draftWorkflowStore;
        this.eventPublisher = eventPublisher;
    }

    public OSWorkflowManager(Configuration configuration, DraftWorkflowStore draftWorkflowStore, EventPublisher eventPublisher)
    {
        setConfiguration(configuration);
        this.draftWorkflowStore = draftWorkflowStore;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Retrieve all of the workflows in the system
     *
     * @return A collection of JiraWorkflow objects.
     */
    public Collection<JiraWorkflow> getWorkflows()
    {
        List<JiraWorkflow> workflows = new ArrayList<JiraWorkflow>();

        try
        {
            String[] workflowNames = getConfiguration().getWorkflowNames();

            for (String workflowName : workflowNames)
            {
                workflows.add(getWorkflow(workflowName));
            }
        }
        catch (FactoryException e)
        {
            log.error("Could not get workflow names: " + e, e);
        }

        Collections.sort(workflows);
        return workflows;
    }

    @Override
    public List<JiraWorkflow> getWorkflowsIncludingDrafts()
    {
        List<JiraWorkflow> ret = new ArrayList<JiraWorkflow>();
        for (JiraWorkflow jiraWorkflow : getWorkflows())
        {
            ret.add(jiraWorkflow);
            JiraWorkflow draftWorkflow = getDraftWorkflow(jiraWorkflow.getName());
            if (draftWorkflow != null)
            {
                ret.add(draftWorkflow);
            }
        }
        return ret;
    }

    /**
     * This method returns the (unique) name of the workflow which should be used for the provided projectId and
     * issueType
     *
     * @return the name of the workflow that should be used for the issue
     */
    protected String getWorkflowName(Long projectId, String issueType)
    {
        // first we need to get the workflow scheme for this project
        Project project = ManagerFactory.getProjectManager().getProjectObj(projectId);
        return ManagerFactory.getWorkflowSchemeManager().getWorkflowName(project, issueType);
    }

    public Collection<JiraWorkflow> getActiveWorkflows() throws WorkflowException
    {
        return getSchemeActiveWorkflows();
    }

    public boolean isActive(JiraWorkflow workflow) throws WorkflowException
    {
        return getSchemeActiveWorkflows().contains(workflow);
    }

    // Check for a system or XML based workflow - can not be edited
    public boolean isSystemWorkflow(JiraWorkflow workflow)
    {
        return !getConfiguration().isModifiable(workflow.getName());
    }

    private Collection<JiraWorkflow> getSchemeActiveWorkflows() throws WorkflowException
    {
        try
        {
            Collection<String> names = ManagerFactory.getWorkflowSchemeManager().getActiveWorkflowNames();
            Set<JiraWorkflow> workflows = new HashSet<JiraWorkflow>();
            for (String name : names)
            {
                workflows.add(getWorkflow(name));
            }
            return workflows;
        }
        catch (GenericEntityException e)
        {
            throw new WorkflowException(e);
        }
    }

    public JiraWorkflow getWorkflow(String name)
    {
        try
        {
            WorkflowDescriptor workflowDescriptor = getConfiguration().getWorkflow(name);
            //TODO: We should check here if the returned workflowDescriptor is non null.
            if (JiraWorkflow.DEFAULT_WORKFLOW_NAME.equals(name))
            {
                return new DefaultJiraWorkflow(workflowDescriptor, this);
            }
            else
            {
                return new ConfigurableJiraWorkflow(name, workflowDescriptor, this);
            }
        }
        catch (FactoryException e)
        {
            log.error("Could not get workflow called: " + name + ": " + e, e);
            return null;
        }
    }

    public JiraWorkflow getWorkflowClone(String name)
    {
        try
        {
            WorkflowDescriptor workflowDescriptor = getConfiguration().getWorkflow(name);
            if (JiraWorkflow.DEFAULT_WORKFLOW_NAME.equals(name))
            {
                return new DefaultJiraWorkflow(workflowDescriptor, this);
            }
            WorkflowDescriptor mutableDescriptor = cloneDescriptor(workflowDescriptor);
            return new ConfigurableJiraWorkflow(name, mutableDescriptor, this);
        }
        catch (FactoryException e)
        {
            log.error("Could not get workflow called: " + name + ": " + e, e);
            return null;
        }
    }

    //package level protected for testing.
    WorkflowDescriptor cloneDescriptor(WorkflowDescriptor workflowDescriptor)
            throws FactoryException
    {
        return WorkflowUtil.convertXMLtoWorkflowDescriptor(WorkflowUtil.convertDescriptorToXML(workflowDescriptor));
    }

    public JiraWorkflow getDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        final JiraWorkflow parentWorkflow = getWorkflow(parentWorkflowName);
        if (parentWorkflow == null)
        {
            throw new IllegalArgumentException("Draft workflow could not be retrieved, since the parent workflow with name '" +
                    parentWorkflowName + "' does not exist.");
        }
        return draftWorkflowStore.getDraftWorkflow(parentWorkflowName);
    }

    public JiraWorkflow createDraftWorkflow(String username, String parentWorkflowName)
    {
        if (username == null)
        {
            throw new IllegalArgumentException("You can not create a draft workflow with a null username.");
        }
        final JiraWorkflow parentWorkflow = getWorkflow(parentWorkflowName);
        if (parentWorkflow == null)
        {
            throw new IllegalArgumentException("You can not create a draft workflow from a parent that does not exist.");
        }
        if (!parentWorkflow.isActive())
        {
            throw new IllegalStateException("You can not create a draft workflow from a parent workflow that is not active.");
        }

        JiraWorkflow draftWorkflow = draftWorkflowStore.createDraftWorkflow(username, parentWorkflow);
        eventPublisher.publish(new DraftWorkflowCreatedEvent(draftWorkflow));

        return draftWorkflow;
    }

    public boolean deleteDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        if (StringUtils.isBlank(parentWorkflowName))
        {
            throw new IllegalArgumentException("Can not delete a draft workflow for a parent workflow name of null.");
        }

        JiraWorkflow draftWorkflow = getDraftWorkflow(parentWorkflowName);
        boolean deleted = draftWorkflowStore.deleteDraftWorkflow(parentWorkflowName);

        if (deleted)
        {
            eventPublisher.publish(new DraftWorkflowDeletedEvent(draftWorkflow));
        }

        return deleted;
    }

    public boolean workflowExists(String name) throws WorkflowException
    {
        // This is more efficient than the parent method as it does not create workflow objects
        // but simply looks at workflow names

        if (name == null)
        {
            throw new IllegalArgumentException("Name must not be null.");
        }

        // Cannot get access to a Map that stores the workflows - so need to loop over the
        // name array.
        try
        {
            for (int i = 0; i < getConfiguration().getWorkflowNames().length; i++)
            {
                String workflowName = getConfiguration().getWorkflowNames()[i];
                if (name.equals(workflowName))
                {
                    return true;
                }
            }
        }
        catch (FactoryException e)
        {
            throw new WorkflowException(e);
        }

        return false;
    }

    public JiraWorkflow getWorkflow(Issue issue) throws WorkflowException
    {
        GenericValue project = issue.getProject();
        if (project == null)
        {
            throw new IllegalArgumentException("Project for issue with id '" + issue.getId() + "' is null.");
        }

        GenericValue issueType = issue.getIssueType();
        if (issueType == null)
        {
            throw new IllegalArgumentException("Issue Type for issue with id '" + issue.getId() + "' is null.");
        }

        return getWorkflow(project.getLong("id"), issueType.getString("id"));
    }

    public JiraWorkflow getWorkflow(Long projectId, String issueTypeId) throws WorkflowException
    {
        return getWorkflow(getWorkflowName(projectId, issueTypeId));
    }

    public JiraWorkflow getWorkflowFromScheme(GenericValue scheme, String issueTypeId)
    {
        return getWorkflow(ManagerFactory.getWorkflowSchemeManager().getWorkflowName(scheme, issueTypeId));
    }

    public Collection<JiraWorkflow> getWorkflowsFromScheme(GenericValue scheme) throws WorkflowException
    {
        if (scheme != null)
        {
            // now, check if we have a workflow configured for this issue type
            try
            {
                Collection<GenericValue> schemeEntities = ManagerFactory.getWorkflowSchemeManager().getEntities(scheme);
                if (schemeEntities != null)
                {
                    List<JiraWorkflow> result = new ArrayList<JiraWorkflow>(schemeEntities.size());
                    for (GenericValue schemeEntity : schemeEntities)
                    {
                        result.add(getWorkflow(schemeEntity.getString("workflow")));
                    }
                    return result;
                }
            }
            catch (GenericEntityException e)
            {
                throw new WorkflowException(e);
            }
        }

        // Always return the default if nothing else is found
        return CollectionBuilder.newBuilder(getWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME)).asMutableList();
    }

    public void copyAndDeleteDraftWorkflows(com.opensymphony.user.User user, Set<JiraWorkflow> workflows)
    {
        copyAndDeleteDraftWorkflows((User) user, workflows);
    }

    public void copyAndDeleteDraftWorkflows(User user, Set<JiraWorkflow> workflows)
    {
        if (workflows == null || workflows.isEmpty())
        {
            return;
        }
        for (final JiraWorkflow workflow : workflows)
        {
            final String parentWorkflowName = workflow.getName();
            final JiraWorkflow draftWorkflow = getDraftWorkflow(parentWorkflowName);

            //We should only create a copy and delete the draft, if the parentworkflow
            //is not active, and the draft actually exists.  For a workflow that's still
            //active, we want to keep the draft around.
            if (!workflow.isActive() && draftWorkflow != null)
            {
                String username = user == null ? null : user.getName();

                copyWorkflow(username,
                        getClonedWorkflowName(parentWorkflowName),
                        draftWorkflow.getDescription() + " " +
                                getI18nBean(user).getText("admin.workflows.manager.draft.auto.generated", parentWorkflowName),
                        draftWorkflow);
                deleteDraftWorkflow(parentWorkflowName);
            }
        }
    }

    public void createWorkflow(String username, JiraWorkflow workflow) throws WorkflowException
    {
        // Store the last edit author and updated date in the workflow descriptor
        addAuditInfo(username, workflow);

        saveWorkflowWithoutAudit(workflow);
    }

    public void createWorkflow(com.opensymphony.user.User creator, JiraWorkflow workflow) throws WorkflowException
    {
        createWorkflow((User) creator, workflow);
    }

    public void createWorkflow(User creator, JiraWorkflow workflow) throws WorkflowException
    {
        String username = null;
        if (creator != null)
        {
            username = creator.getName();
        }
        createWorkflow(username, workflow);
    }

    private void addAuditInfo(String userName, JiraWorkflow workflow)
    {
        if (workflow == null)
        {
            return;
        }
        WorkflowDescriptor descriptor = workflow.getDescriptor();
        log.info("User '" + userName + "' updated workflow '" + workflow.getName() + "' at '" + new Date() + "'");

        // If a non-logged in user is storing the workflow then we will store it as an empty string
        if (userName != null)
        {
            descriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_NAME, userName);
        }
        else
        {
            descriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_NAME, "");
        }
        descriptor.getMetaAttributes().put(JiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(System.currentTimeMillis()));
    }

    public void saveWorkflowWithoutAudit(JiraWorkflow workflow) throws WorkflowException
    {
        if (workflow.isDraftWorkflow())
        {
            // Save the passed workflow over the existing draft
            draftWorkflowStore.updateDraftWorkflowWithoutAudit(workflow.getName(), workflow);
        }
        try
        {
            // Store the last edit author and updated date in the workflow descriptor
            getConfiguration().saveWorkflow(workflow.getName(), workflow.getDescriptor(), true);
            workflow.reset();
        }
        catch (FactoryException e)
        {
            throw new WorkflowException(e);
        }
    }

    protected WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return ComponentAccessor.getWorkflowSchemeManager();
    }

    public void deleteWorkflow(JiraWorkflow workflow) throws WorkflowException
    {
        if (isActive(workflow))
        {
            throw new WorkflowException("You cannot delete an enabled workflow.");
        }

        try
        {
            //we need to delete the draft first since it needs a reference to the parent
            deleteDraftWorkflow(workflow.getName());

            //TODO: Should we move this to the store?
            getConfiguration().removeWorkflow(workflow.getName());
            eventPublisher.publish(new WorkflowDeletedEvent(workflow));

        }
        catch (FactoryException e)
        {
            throw new WorkflowException("Error deleting workflow: " + e, e);
        }
    }

    public JiraWorkflow getWorkflow(GenericValue issue) throws WorkflowException
    {
        String workflowName = getWorkflowName(issue.getLong("project"), issue.getString("type"));
        return getWorkflow(workflowName);
    }

    public void migrateIssueToWorkflow(GenericValue issue, JiraWorkflow newWorkflow, GenericValue newStatus)
            throws WorkflowException
    {
        try
        {
            WorkflowStore store = getStore();

            // find the current step for the current entry
            long wfid = issue.getLong("workflowId");
            List currentSteps = store.findCurrentSteps(wfid);
            SimpleStep currentStep = null;

            if (!currentSteps.isEmpty())
            {
                currentStep = (SimpleStep) currentSteps.get(0);
            }

            // create a new workflow entry
            WorkflowEntry newEntry = store.createEntry(newWorkflow.getName());
            store.setEntryState(newEntry.getId(), WorkflowEntry.ACTIVATED);
            store.setEntryState(wfid, WorkflowEntry.KILLED);

            // now create a current step matching the old current step, but with the new workflow
            StepDescriptor stepInNewWorkflow = newWorkflow.getLinkedStep(newStatus);
            if (stepInNewWorkflow == null)
            {
                throw new RuntimeException("No step associated with status " + (newStatus == null ? "null" : newStatus.getString("name")) + " in new workflow " + newWorkflow.getName());
            }

            // Check if the workflow entry had a corresponding step
            if (currentStep != null)
            {
                store.createCurrentStep(newEntry.getId(), stepInNewWorkflow.getId(), currentStep.getOwner(), currentStep.getStartDate(), currentStep.getDueDate(), currentStep.getStatus(), null);
                // Move the original step to the OS_HISTORYSTEP table
                store.moveToHistory(currentStep);
            }
            else
            {
                // Create a new step - set the start date and status
                Date startDate = issue.getTimestamp("created");
                store.createCurrentStep(newEntry.getId(), stepInNewWorkflow.getId(), null, startDate, null, newStatus.getString("id"), null);
            }

            updateIssueStatusAndUpdatedDate(issue, newStatus);

            issue.set("workflowId", newEntry.getId());
            issue.store();

            ManagerFactory.getIndexManager().reIndex(issue);
        }
        catch (StoreException e)
        {
            throw new WorkflowException(e);
        }
        catch (GenericEntityException e)
        {
            throw new WorkflowException(e);
        }
        catch (IndexException e)
        {
            log.error("Error indexing issue during workflow migration: " + e, e);
        }
    }

    public void overwriteActiveWorkflow(String username, String workflowName)
    {
        // Get the draft workflow from the Store
        JiraWorkflow draftWorkflow = draftWorkflowStore.getDraftWorkflow(workflowName);
        if (draftWorkflow == null)
        {
            throw new WorkflowException("No draft workflow named '" + workflowName + "'");
        }

        boolean saved;
        // save the draft over the active workflow
        try
        {
            // Add Audit info
            addAuditInfo(username, draftWorkflow);
            saved = getConfiguration().saveWorkflow(workflowName, draftWorkflow.getDescriptor(), true);
            eventPublisher.publish(new DraftWorkflowPublishedEvent(draftWorkflow));
        }
        catch (FactoryException e)
        {
            throw new WorkflowException(e);
        }

        if (!saved)
        {
            throw new WorkflowException("Workflow '" + workflowName + "' could not be overwritten!");
        }
        else
        {
            // Now remove the "Draft" copy
            draftWorkflowStore.deleteDraftWorkflow(workflowName);
        }
    }

    protected void updateIssueStatusAndUpdatedDate(GenericValue issue, GenericValue newStatus)
    {
        if (!issue.getString("status").equals(newStatus.getString("id")))
        {
            issue.set("updated", UtilDateTime.nowTimestamp());
            issue.set("status", newStatus.getString("id"));
        }
    }


    public void updateWorkflow(String username, JiraWorkflow workflow)
    {
        if (username == null)
        {
            throw new IllegalArgumentException("Can not update a workflow with a null username.");
        }
        if (workflow == null || workflow.getDescriptor() == null)
        {
            throw new IllegalArgumentException("Can not update a workflow with a null workflow/descriptor.");
        }
        if (workflow.isDraftWorkflow())
        {
            String workflowName = workflow.getName();
            final JiraWorkflow parentWorkflow = getWorkflow(workflowName);
            if (parentWorkflow == null)
            {
                throw new IllegalStateException("You can not update a draft workflow for a parent that does not exist.");
            }

            draftWorkflowStore.updateDraftWorkflow(username, workflowName, workflow);
        }
        else
        {
            if (workflow.isActive())
            {
                throw new WorkflowException("Cannot save an active workflow.");
            }
            if (workflow.isSystemWorkflow())
            {
                throw new WorkflowException("Cannot change the system workflow.");
            }
            createWorkflow(username, workflow);
        }
    }

    public JiraWorkflow copyWorkflow(String username, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone)
    {
        final WorkflowDescriptor workflowDescriptor;
        try
        {
            workflowDescriptor = cloneDescriptor(workflowToClone.getDescriptor());
        }
        catch (FactoryException e)
        {
            throw new WorkflowException("Unexpected exception copying a workflowDescriptor for workflow '" + clonedWorkflowName + "'!", e);
        }
        ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(clonedWorkflowName, workflowDescriptor, this);

        // Set description if we have one, otherwise set it to an empty string
        if (StringUtils.isNotEmpty(clonedWorkflowDescription))
        {
            newWorkflow.setDescription(clonedWorkflowDescription);
        }
        else
        {
            newWorkflow.setDescription("");
        }

        createWorkflow(username, newWorkflow);
        eventPublisher.publish(new WorkflowCopiedEvent(workflowToClone, newWorkflow));
        return newWorkflow;
    }

    public void updateWorkflowNameAndDescription(String username, JiraWorkflow currentWorkflow, String newName, String newDescription)
    {
        Null.not("currentWorkflow", currentWorkflow);
        final String currentWorkflowName = currentWorkflow.getName();

        //get an editable copy of the workflow.  If the workflow passed in is not
        //a draft workflow, we need to get an editable clone of the real workflow.
        JiraWorkflow workflow = currentWorkflow;
        if (!currentWorkflow.isDraftWorkflow())
        {
            workflow = getWorkflowClone(currentWorkflowName);
        }

        //update the description in the database
        if (newDescription != null && !newDescription.equals(workflow.getDescription()))
        {
            WorkflowDescriptor descriptor = workflow.getDescriptor();
            descriptor.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, newDescription);
            updateWorkflow(username, workflow);
        }

        //update the name in the database only if needed, and if the workflow is not a draft workflow!
        if (!currentWorkflowName.equals(newName) && !currentWorkflow.isDraftWorkflow())
        {
            try
            {
                getConfiguration().removeWorkflow(currentWorkflowName);
                getConfiguration().saveWorkflow(newName, workflow.getDescriptor(), true);

                //update the associated schemes foreign key reference the new workflow name
                getWorkflowSchemeManager().updateSchemesForRenamedWorkflow(currentWorkflowName, newName);

                //check if there's a draft hanging around.  This should never be the case, as an in-active workflow
                //should never have a draft, but just in case we should change its name too to make sure
                //we don't end up with an orphaned draft!
                JiraWorkflow draftWorkflow = draftWorkflowStore.getDraftWorkflow(currentWorkflowName);
                if (draftWorkflow != null)
                {
                    log.warn("Inactive workflow '" + newName + "' has a draft workflow. Please remove this draft!");
                    JiraDraftWorkflow newWorkflow = new JiraDraftWorkflow(newName, this, draftWorkflow.getDescriptor());
                    draftWorkflowStore.createDraftWorkflow(username, newWorkflow);
                    draftWorkflowStore.deleteDraftWorkflow(currentWorkflowName);
                }
            }
            catch (FactoryException e)
            {
                throw new WorkflowException("Error renaming workflow '" + currentWorkflow + "' to '" + newName + "' " + e, e);
            }
        }
    }

    //a couple of helper methods mainly to make the code more testable!
    I18nHelper getI18nBean(User user)
    {
        return new I18nBean(user);
    }

    String getClonedWorkflowName(String parentWorkflowName)
    {
        return WorkflowUtil.cloneWorkflowName(parentWorkflowName);
    }

    protected void resetConfiguration()
    {
        try
        {
            Configuration configuration = new DefaultConfiguration();
            configuration.load(ClassLoaderUtils.getResource("osworkflow.xml", getClass()));
            // This will ensure that os workflow uses the right delegator for the right tenant
            configuration.getPersistenceArgs().put("delegator", new DefaultOfBizConnectionFactory().getDelegatorName());
            // This is here because there is a concurrency bug in osworkflow such that the configuration
            // does not safely initialize its GenericDelegator. If we do not "prime" the reference to the
            // delegator then you can run into issues where you get a null pointer when concurrently trying
            // to create issues. DO NOT REMOVE THIS BLOCK OF CODE!!
            try
            {
                configuration.getWorkflowStore();
            }
            catch (StoreException e)
            {
                throw new DataAccessException(e);
            }
            setConfiguration(configuration);
        }
        catch (FactoryException e)
        {
            log.error("Error loading configuration: " + e, e);
            throw new InfrastructureException("Error loading osworkflow.xml file: " + e, e);
        }
    }

    /*
     * needed for tests
     */
    void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }


    public JiraWorkflow getDefaultWorkflow() throws WorkflowException
    {
        return getWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
    }

    public GenericValue createIssue(String remoteUserName, Map<String, Object> fields) throws WorkflowException
    {
        try
        {
            // Determine the workflow for the issue to use
            Issue issue = (Issue) fields.get("issue");
            final Long projectId = issue.getProjectObject().getId();
            final String issueTypeId = issue.getIssueTypeObject().getId();
            final JiraWorkflow jiraWorkflow = getWorkflow(projectId, issueTypeId);
            if (jiraWorkflow == null)
            {
                throw new IllegalArgumentException("Cannot find workflow for project with id '" + projectId + "' and issue type with id '" + issueTypeId + "'.");
            }

            final Workflow workflow = makeWorkflow(remoteUserName);

            // Get the initial action for the
            final WorkflowDescriptor workflowDescriptor = jiraWorkflow.getDescriptor();
            final List initialActions = workflowDescriptor.getInitialActions();

            if (initialActions == null || initialActions.isEmpty())
            {
                throw new WorkflowException("No initial actions exist for workflow with name '" + jiraWorkflow.getName() + ".");
            }

            // NOTE: Only one initial action is supported.
            // REMEMBER: THERE CAN BE ONLY ONE!
            final ActionDescriptor actionDescriptor = (ActionDescriptor) initialActions.get(0);

            long wfId = workflow.initialize(jiraWorkflow.getName(), actionDescriptor.getId(), fields);

            final GenericValue issueGV = getIssueManager().getIssueByWorkflow(wfId);
            // https://support.atlassian.com/browse/JST-15144  Looks like IssueCreateFunction can screw up if you configure your post functions incorrectly.
            if (issueGV == null)
            {
                throw new WorkflowException("Issue workflow initialization error: unable to find Issue created with workflowId '" +
                        wfId + "'. Did the IssueCreateFunction run successfully on workflow.initialize() ?");
            }
            return issueGV;

        }
        catch (InvalidRoleException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (InvalidInputException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (GenericEntityException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (InvalidEntryStateException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (com.opensymphony.workflow.WorkflowException e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        catch (ClassCastException e)
        {
            String message = "Error occurred while creating issue. This could be due to a plugin being incompatible with this version of JIRA. For more details please consult the logs, and see: " +
                    "http://confluence.atlassian.com/x/3McB";

            throw new WorkflowException(message, e);
        }
        catch (Exception e)
        {
            throw new WorkflowException(e.getMessage(), e);
        }
    }

    public void removeWorkflowEntries(GenericValue issue) throws GenericEntityException
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        genericDelegator.removeByAnd("OSWorkflowEntry", EasyMap.build("id", issue.getLong("workflowId")));
        genericDelegator.removeByAnd("OSCurrentStep", EasyMap.build("entryId", issue.getLong("workflowId")));
        genericDelegator.removeByAnd("OSHistoryStep", EasyMap.build("entryId", issue.getLong("workflowId")));
    }

    public void doWorkflowAction(WorkflowProgressAware from)
    {
        final boolean indexingPreviouslyEnabled = disableIndexingForThisThread();

        Transaction txn = Txn.begin();
        try
        {
            doWorkflowActionInsideTxn(txn, from, indexingPreviouslyEnabled);
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();

            // If indexing was enabled then turn it on no matter what happened
            if (indexingPreviouslyEnabled)
            {
                ImportUtils.setIndexIssues(true);
            }
        }
    }

    private void doWorkflowActionInsideTxn(final Transaction txn, final WorkflowProgressAware from, boolean indexingPreviouslyEnabled)
    {
        Issue issue = null;
        Long wfid = null;

        try
        {
            issue = from.getIssue();
            wfid = issue.getLong("workflowId");

            // if these are available at the time an exception is thrown, they will be logged
            final Workflow wf = getWorkflowObject(from);

            // get inputs
            Map<Object, Object> inputs = new HashMap<Object, Object>();
            inputs.put("issue", issue);
            GenericValue originalIssueGV = ComponentAccessor.getIssueManager().getIssue(issue.getId());
            MutableIssue originalIssue = ComponentAccessor.getIssueFactory().getIssue(originalIssueGV);
            inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssue);
            inputs.put("project", from.getProject());
            inputs.put("pkey", from.getProject().getString("key")); // Allows ${pkey} in condition args
            if (from.getAdditionalInputs() != null)
            {
                inputs.putAll(from.getAdditionalInputs());
            }

            wf.doAction(wfid, from.getAction(), inputs);

            // save the issue updates - the generate change history function stores the issue as well, but if it has been modified by some post functions
            // after that we need to store the issue again.
            issue.store();

            // commit the TX
            txn.commit();

            // If indexing was enabled then turn it back on and re-index the issue
            if (indexingPreviouslyEnabled)
            {
                ImportUtils.setIndexIssues(true);
                ComponentManager.getInstance().getIndexManager().reIndex(issue);
                if (!ObjectUtils.equals(originalIssue.getSecurityLevelId(), issue.getSecurityLevelId()))
                {
                    ComponentManager.getInstance().getIndexManager().reIndexIssueObjects(issue.getSubTaskObjects());
                }
            }
        }
        catch (InvalidInputException e)
        {
            for (Iterator<?> iterator = e.getGenericErrors().iterator(); iterator.hasNext(); )
            {
                String error = (String) iterator.next();
                from.addErrorMessage(error);
            }

            for (Iterator<?> iterator = e.getErrors().entrySet().iterator(); iterator.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) iterator.next();
                from.addError((String) entry.getKey(), (String) entry.getValue());
            }

            log.error(String.format("Caught exception while attempting to perform action %d from workflow %d on issue '%s'", from.getAction(), wfid, issue), e);

        }
        catch (ClassCastException e)
        {
            String message = "Error occurred while creating issue. This could be due to a plugin being incompatible with this version of JIRA. For more details please consult the logs, and see: " +
                    "http://confluence.atlassian.com/x/3McB";
            log.error(String.format("Caught exception while attempting to perform action %d from workflow %d on issue '%s'", from.getAction(), wfid, issue), e);
            from.addErrorMessage(message + " " + e.getMessage());
        }
        catch (Exception e)
        {
            from.addErrorMessage(e.getMessage());
            log.error(String.format("Caught exception while attempting to perform action %d from workflow %d on issue '%s'", from.getAction(), wfid, issue), e);
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();
        }
    }

    private boolean disableIndexingForThisThread()
    {
        boolean indexingPreviouslyEnabled = ImportUtils.isIndexIssues();
        if (indexingPreviouslyEnabled)
        {
            log.debug("Disabling indexes temporarily");
            // If indexing was enabled then disable it.
            ImportUtils.setIndexIssues(false);
        }
        return indexingPreviouslyEnabled;
    }

    private Workflow getWorkflowObject(WorkflowProgressAware from)
    {
        String username = null;

        // Allows actions to be run as other users.
        if (from.getAdditionalInputs() != null)
        {
            if (from.getAdditionalInputs().containsKey("username"))
            {
                username = (String) from.getAdditionalInputs().get("username");
            }
        }

        if (username == null && ActionContext.getPrincipal() != null)
        {
            username = ActionContext.getPrincipal().getName();
        }

        if (username == null)
        {
            User user = from.getRemoteUser();
            if (user != null)
            {
                username = user.getName();
            }
        }

        return makeWorkflow(username);
    }

    public com.opensymphony.user.User getRemoteUser(Map transientVars) throws EntityNotFoundException
    {
        WorkflowContext context = (WorkflowContext) transientVars.get("context");
        String username = context.getCaller();

        if (username != null)
        {
            return ComponentAccessor.getUserManager().getUser(username);
        }
        else
        {
            return null;
        }
    }

    /**
     * This is used for unit testing so we can return our own 'mock store' instead of the static
     * StoreFactory.getPersistence(ctx);
     * <p/>
     * Also used in the migrateIssueToWorkflow method.
     */
    public WorkflowStore getStore() throws StoreException
    {
        return configuration.getWorkflowStore();
    }


    public ActionDescriptor getActionDescriptor(WorkflowProgressAware workflowProgressAware) throws Exception
    {
        JiraWorkflow workflow = getWorkflow(workflowProgressAware.getIssue().getGenericValue());
        return workflow.getDescriptor().getAction(workflowProgressAware.getAction());
    }

    /**
     * Migrates given issue to new workflow and sets new status on it.
     *
     * @param issue issue to migrate
     * @param newWorkflow new workflow
     * @param status new status
     * @throws WorkflowException if migration fails
     */
    public void migrateIssueToWorkflow(MutableIssue issue, JiraWorkflow newWorkflow, Status status)
            throws WorkflowException
    {
        final GenericValue issueGV = issue.getGenericValue();
        migrateIssueToWorkflow(issueGV, newWorkflow, status.getGenericValue());
        issue.setWorkflowId(issueGV.getLong("workflowId"));
        issue.setStatusId(issueGV.getString("status"));
    }

    public Workflow makeWorkflow(String userName)
    {
        Workflow workflow = new BasicWorkflow(userName);
        workflow.setConfiguration(configuration);
        return workflow;
    }

    public boolean isEditable(Issue issue)
    {
        try
        {
            JiraWorkflow workflow = getWorkflow(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
            final String status = issue.getStatusObject().getId();
            if (status != null) //this should never really be null - but it saves hassle of setting up hundreds of tests with a status
            {
                StepDescriptor currentStep = workflow.getLinkedStep(ManagerFactory.getConstantsManager().getStatus(status));
                if (!"false".equals(currentStep.getMetaAttributes().get(JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED)))
                {
                    return true;
                }
            }

            return false;
        }
        catch (WorkflowException e)
        {
            throw new NestableRuntimeException(e + " when trying to access workflow for issue " + issue, e);
        }
    }

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    public Map<ActionDescriptor, Collection<FunctionDescriptor>> getPostFunctionsForWorkflow(JiraWorkflow workflow)
    {
        Map<ActionDescriptor, Collection<FunctionDescriptor>> transitionPostFunctionMap = new HashMap<ActionDescriptor, Collection<FunctionDescriptor>>();

        Collection<ActionDescriptor> actions = workflow.getAllActions();
        for (final ActionDescriptor actionDescriptor : actions)
        {
            Collection<FunctionDescriptor> postFunctions = workflow.getPostFunctionsForTransition(actionDescriptor);

            transitionPostFunctionMap.put(actionDescriptor, postFunctions);
        }

        return transitionPostFunctionMap;
    }

    public String getStepId(long actionDescriptorId, String workflowName)
    {
        int actionDescId = new Long(actionDescriptorId).intValue();

        String stepId = null;

        JiraWorkflow workflow = getWorkflow(workflowName);
        ActionDescriptor actionDescriptor = workflow.getDescriptor().getAction(actionDescId);

        if (actionDescriptor != null)
        {
            Collection stepsForTransition = workflow.getStepsForTransition(actionDescriptor);

            for (Iterator<?> iterator = stepsForTransition.iterator(); iterator.hasNext(); )
            {
                StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
                stepId = String.valueOf(stepDescriptor.getId());
                break;
            }
        }

        return stepId;
    }

    /**
     * Returns the IssueManager. Needed to avoid circular dependency.
     *
     * @return IssueManager
     */
    private IssueManager getIssueManager()
    {
        return ComponentAccessor.getIssueManager();
    }

}
