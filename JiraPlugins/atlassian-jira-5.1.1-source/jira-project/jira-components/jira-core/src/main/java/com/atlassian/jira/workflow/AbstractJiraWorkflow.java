/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 24, 2004
 * Time: 6:37:13 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.Status;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AbstractJiraWorkflow implements JiraWorkflow
{
    private static final Logger log = Logger.getLogger(AbstractJiraWorkflow.class);

    WorkflowDescriptor descriptor;
    protected final WorkflowManager workflowManager;

    // Records the field screens that this workflow uses.
    private final MultiMap fieldScreens;

    protected AbstractJiraWorkflow(final WorkflowManager workflowManager, final WorkflowDescriptor workflowDescriptor)
    {
        this.workflowManager = workflowManager;
        descriptor = workflowDescriptor;
        fieldScreens = new MultiHashMap();
        reset();
    }

    public abstract String getName();

    public String getDescription()
    {
        return (String) descriptor.getMetaAttributes().get(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE);
    }

    public WorkflowDescriptor getDescriptor()
    {
        return descriptor;
    }

    public Collection getAllActions()
    {
        return getAllActionsMap().values();
    }

    public int getNextActionId()
    {
        int offset = 0;
        final SortedMap allActionsMap = getAllActionsMap();
        if (!allActionsMap.isEmpty())
        {
            offset = ((Integer) allActionsMap.lastKey()).intValue();
        }

        return offset + 10;
    }

    private SortedMap getAllActionsMap()
    {
        final SortedMap actions = new TreeMap();

        // Register all initial actions
        addActionsToMap(actions, descriptor.getInitialActions());

        // Register all global actions
        addActionsToMap(actions, descriptor.getGlobalActions());

        // Register all common actions
        actions.putAll(descriptor.getCommonActions());

        // Register all normal actions
        final List steps = descriptor.getSteps();
        for (final Iterator iterator = steps.iterator(); iterator.hasNext();)
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
            for (final Iterator iterator1 = stepDescriptor.getActions().iterator(); iterator1.hasNext();)
            {
                final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator1.next();
                // If the action id is already in the list - it is likely to be a common action :)
                // So no need to add it as it is already in the list
                if (!actions.containsKey(new Integer(actionDescriptor.getId())))
                {
                    actions.put(new Integer(actionDescriptor.getId()), actionDescriptor);
                }
            }
        }

        return actions;
    }

    private void addActionsToMap(final SortedMap actions, final Collection initialActions)
    {
        for (final Iterator iterator = initialActions.iterator(); iterator.hasNext();)
        {
            final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
            actions.put(new Integer(actionDescriptor.getId()), actionDescriptor);
        }
    }

    public Collection getActionsWithResult(final StepDescriptor stepDescriptor)
    {
        final Collection actions = getAllActions();
        actionloop : for (final Iterator iterator = actions.iterator(); iterator.hasNext();)
        {
            final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
            // Check all conditional results
            for (final Iterator iterator1 = actionDescriptor.getConditionalResults().iterator(); iterator1.hasNext();)
            {
                final ResultDescriptor resultDescriptor = (ResultDescriptor) iterator1.next();
                if (resultDescriptor.getStep() == stepDescriptor.getId())
                {
                    // The step is a destination step for action's conditional result
                    // Leave the action in the collection
                    continue actionloop;
                }
            }

            // Now check the unconditional result
            if (actionDescriptor.getUnconditionalResult().getStep() != stepDescriptor.getId())
            {
                // If the step is not a destination of any conditional and unconditional result remove the action from the list
                iterator.remove();
            }
        }
        return actions;
    }

    public Collection getStepsWithAction(final StepDescriptor stepDescriptor)
    {
        // If global Action

        final Collection actions = getAllActions();
        actionloop : for (final Iterator iterator = actions.iterator(); iterator.hasNext();)
        {
            final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
            // Check all conditional results
            for (final Iterator iterator1 = actionDescriptor.getConditionalResults().iterator(); iterator1.hasNext();)
            {
                final ResultDescriptor resultDescriptor = (ResultDescriptor) iterator1.next();
                if (resultDescriptor.getStep() == stepDescriptor.getId())
                {
                    // The step is a destination step for action's conditional result
                    // Leave the action in the collection
                    continue actionloop;
                }
            }

            // Now check the unconditional result
            if (actionDescriptor.getUnconditionalResult().getStep() != stepDescriptor.getId())
            {
                // If the step is not a destination of any conditional and unconditional result remove the action from the list
                iterator.remove();
            }
        }
        return actions;
    }

    public boolean removeStep(final StepDescriptor stepDescriptor)
    {
        // Remove any transitions that end in this step
        if (!getActionsWithResult(stepDescriptor).isEmpty())
        {
            throw new IllegalArgumentException("Cannot remove step - it is a destination step of at least one transition.");
        }

        return descriptor.getSteps().remove(stepDescriptor);
    }

    /* @return Workflow step associated with a status in this workflow, or null if not found. */
    public StepDescriptor getLinkedStep(final GenericValue status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException();
        }

        return getLinkedStep(status.getString("id"));
    }

    public StepDescriptor getLinkedStep(final Status status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException();
        }

        return getLinkedStep(status.getId());
    }

    private StepDescriptor getLinkedStep(final String statusId)
    {
        if (statusId == null)
        {
            throw new IllegalArgumentException();
        }
        for (final Iterator iterator = descriptor.getSteps().iterator(); iterator.hasNext();)
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
            if (statusId.equals(stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
            {
                return stepDescriptor;
            }
        }
        // not found
        return null;
    }

    public GenericValue getLinkedStatus(final StepDescriptor stepDescriptor)
    {
        if (stepDescriptor == null)
        {
            throw new IllegalArgumentException("Step cannot be null.");
        }

        if ((stepDescriptor.getMetaAttributes() != null) && stepDescriptor.getMetaAttributes().containsKey(STEP_STATUS_KEY))
        {
            final String statusId = (String) stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY);

            if (statusId != null)
            {
                return ManagerFactory.getConstantsManager().getStatus(statusId);
            }
        }

        throw new IllegalStateException("Step with id '" + stepDescriptor.getId() + "' does not have a valid linked status.");
    }

    public Status getLinkedStatusObject(final StepDescriptor stepDescriptor)
    {
        if (stepDescriptor == null)
        {
            throw new IllegalArgumentException("Step cannot be null.");
        }

        if ((stepDescriptor.getMetaAttributes() != null) && stepDescriptor.getMetaAttributes().containsKey(STEP_STATUS_KEY))
        {
            final String statusId = (String) stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY);

            if (statusId != null)
            {
                return ManagerFactory.getConstantsManager().getStatusObject(statusId);
            }
        }

        throw new IllegalStateException("Step with id '" + stepDescriptor.getId() + "' does not have a valid linked status.");
    }

    public List getLinkedStatuses()
    {
        final List statuses = new ArrayList();

        for (final Iterator iterator = descriptor.getSteps().iterator(); iterator.hasNext();)
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
            statuses.add(getLinkedStatus(stepDescriptor));
        }

        return statuses;
    }

    public List /* <Status> */getLinkedStatusObjects()
    {
        final List statuses = new ArrayList();

        for (final Iterator iterator = descriptor.getSteps().iterator(); iterator.hasNext();)
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
            statuses.add(getLinkedStatusObject(stepDescriptor));
        }

        return statuses;
    }

    public Collection getStepsForTransition(final ActionDescriptor actionDescriptor)
    {
        if (isInitialAction(actionDescriptor))
        {
            // There are no originating steps for the initial action
            return Collections.EMPTY_LIST;
        }

        if (isGlobalAction(actionDescriptor))
        {
            // Global actions are available from everywhere
            return getDescriptor().getSteps();
        }

        if (isCommonAction(actionDescriptor))
        {
            final Collection steps = new LinkedList();

            for (final Iterator iterator = descriptor.getSteps().iterator(); iterator.hasNext();)
            {
                final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
                for (final Iterator iterator1 = stepDescriptor.getCommonActions().iterator(); iterator1.hasNext();)
                {
                    if (((Integer) iterator1.next()).intValue() == actionDescriptor.getId())
                    {
                        steps.add(stepDescriptor);
                    }
                }
            }

            return steps;
        }
        else
        {
            final Collection steps = new LinkedList();

            for (final Iterator iterator = descriptor.getSteps().iterator(); iterator.hasNext();)
            {
                final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
                for (final Iterator iterator1 = stepDescriptor.getActions().iterator(); iterator1.hasNext();)
                {
                    final ActionDescriptor ad = (ActionDescriptor) iterator1.next();
                    if (ad.getId() == actionDescriptor.getId())
                    {
                        steps.add(stepDescriptor);
                        // If the action is a 'ordinary action' then it is available from only one step
                        return steps;
                    }
                }
            }

            // Could not find any steps
            return Collections.EMPTY_LIST;
        }
    }

    public Collection<FunctionDescriptor> getPostFunctionsForTransition(final ActionDescriptor actionDescriptor)
    {
        final Collection<FunctionDescriptor> allPostFunctions = new ArrayList<FunctionDescriptor>();

        if ((actionDescriptor.getUnconditionalResult() != null) && (actionDescriptor.getUnconditionalResult().getPostFunctions() != null))
        {
            allPostFunctions.addAll(actionDescriptor.getUnconditionalResult().getPostFunctions());
        }

        final List conditionalResults = actionDescriptor.getConditionalResults();
        if (conditionalResults != null)
        {
            for (final Iterator iterator = conditionalResults.iterator(); iterator.hasNext();)
            {
                final ResultDescriptor resultDescriptor = (ResultDescriptor) iterator.next();
                allPostFunctions.addAll(resultDescriptor.getPostFunctions());
            }
        }

        if (actionDescriptor.getPostFunctions() != null)
        {
            allPostFunctions.addAll(actionDescriptor.getPostFunctions());
        }

        return allPostFunctions;
    }

    public boolean isActive() throws WorkflowException
    {
        return workflowManager.isActive(this);
    }

    public boolean isSystemWorkflow() throws WorkflowException
    {
        return workflowManager.isSystemWorkflow(this);
    }

    public boolean isEditable() throws WorkflowException
    {
        return !isSystemWorkflow() && !isActive();
    }

    public boolean isDefault()
    {
        return DEFAULT_WORKFLOW_NAME.equals(getName());
    }

    public boolean isInitialAction(final ActionDescriptor actionDescriptor)
    {
        return getDescriptor().getInitialActions().contains(actionDescriptor);
    }

    public boolean isCommonAction(final ActionDescriptor actionDescriptor)
    {
        return actionDescriptor.isCommon();
    }

    public boolean isGlobalAction(final ActionDescriptor actionDescriptor)
    {
        return getDescriptor().getGlobalActions().contains(actionDescriptor);
    }

    public boolean isOrdinaryAction(final ActionDescriptor actionDescriptor)
    {
        return !(isInitialAction(actionDescriptor) || isCommonAction(actionDescriptor) || isGlobalAction(actionDescriptor));
    }

    public String getActionType(final ActionDescriptor actionDescriptor)
    {
        if (actionDescriptor == null)
        {
            throw new IllegalArgumentException("ActionDescriptor cannot be null.");
        }

        if (isInitialAction(actionDescriptor))
        {
            return ACTION_TYPE_INITIAL;
        }
        else if (isGlobalAction(actionDescriptor))
        {
            return ACTION_TYPE_GLOBAL;
        }
        else if (isCommonAction(actionDescriptor))
        {
            return ACTION_TYPE_COMMON;
        }
        else if (isOrdinaryAction(actionDescriptor))
        {
            return ACTION_TYPE_ORDINARY;
        }

        throw new IllegalArgumentException("The action with id '" + actionDescriptor.getId() + "' is of unknown type.");
    }

    public void reset()
    {
        final WorkflowActionsBean workflowActionsBean = new WorkflowActionsBean();
        for (final Iterator iterator = getAllActions().iterator(); iterator.hasNext();)
        {
            final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
            fieldScreens.put(workflowActionsBean.getFieldScreenForView(actionDescriptor), actionDescriptor);
        }
    }

    public Collection getActionsForScreen(final FieldScreen fieldScreen)
    {
        if (fieldScreens.containsKey(fieldScreen))
        {
            return (Collection) fieldScreens.get(fieldScreen);
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractJiraWorkflow))
        {
            return false;
        }

        final AbstractJiraWorkflow abstractJiraWorkflow = (AbstractJiraWorkflow) o;

        return getName().equals(abstractJiraWorkflow.getName());
    }

    @Override
    public int hashCode()
    {
        return (getName() != null ? getName().hashCode() : 0);
    }

    public int compareTo(final JiraWorkflow o)
    {
        return JiraWorkflowComparator.COMPARATOR.compare(this, o);
    }

    public String getUpdateAuthorName()
    {
        String updateAuthor = null;
        final Map metaAttributes = descriptor.getMetaAttributes();
        if (metaAttributes != null)
        {
            updateAuthor = (String) metaAttributes.get(JIRA_META_UPDATE_AUTHOR_NAME);
        }
        return updateAuthor;
    }

    public Date getUpdatedDate()
    {
        final Map metaAttributes = descriptor.getMetaAttributes();
        if (metaAttributes != null)
        {
            final String updateDateStr = (String) metaAttributes.get(JIRA_META_UPDATED_DATE);
            if (updateDateStr != null)
            {
                try
                {
                    final long timeInMillis = Long.parseLong(updateDateStr);
                    return new Date(timeInMillis);
                }
                catch (final NumberFormatException e)
                {
                    log.error("The workflow '" + getName() + "' is storing a invalid updated date string '" + updateDateStr + "'.", e);
                }
            }
        }
        return null;
    }

    public boolean hasDraftWorkflow()
    {
        // Test if we can get a draft workflow with our name from the manager.
        return workflowManager.getDraftWorkflow(getName()) != null;
    }

    public String getMode()
    {
        if (isDraftWorkflow())
        {
            return DRAFT;
        }
        else
        {
            return LIVE;
        }
    }
}