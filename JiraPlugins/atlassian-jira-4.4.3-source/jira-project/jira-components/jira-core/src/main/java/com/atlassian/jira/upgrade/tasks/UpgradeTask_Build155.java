package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.condition.SubTaskBlockingCondition;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Normally, each condition gets passed parameters.  Eg. the SubTaskBlockingCondition gets passed the parameters of which
 * statuses to use when preventing a transition.
 * <p>
 * In JIRA 3.6 the Workflow Editor added other, irrelevent parameters to the condition ('nested' and 'count').  These were
 * erroneously being passed in the URL.  By luck - this didn't cause any problems with the condition.
 * <p>
 * This upgrade task removes these erroneous parameters, so that they don't cause any problems in the future.
 *
 */
public class UpgradeTask_Build155 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build155.class);

    private final WorkflowManager workflowManager;
    private static final String CLASS_NAME_PARAM = "class.name";
    private static final String STATUSES_PARAM_NAME = "statuses";

    public UpgradeTask_Build155(WorkflowManager workflowManager)
    {
        this.workflowManager = workflowManager;
    }

    public String getBuildNumber()
    {
        return "155";
    }

    public String getShortDescription()
    {
        return "Correct the 'statuses' argument of Sub Task Blocking Condition in workflows.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        Collection workflows = workflowManager.getWorkflows();
        if (workflows != null)
        {
            for (Iterator iterator = workflows.iterator(); iterator.hasNext();)
            {
                JiraWorkflow jiraWorkflow = (JiraWorkflow) iterator.next();
                boolean needSave = upgradeWorkflow(jiraWorkflow);
                if (needSave)
                {
                    saveWorkflow(jiraWorkflow);
                }
            }
        }
    }

    boolean upgradeWorkflow(JiraWorkflow workflow) throws WorkflowException
    {
        boolean changedWorkflow = false;
        if (workflow != null)
        {
            log.info("Inspecting workflow " + workflow.getName());
            // Iterate over all the actions and check whether the SubTaskBlockingCondition is used for any of them
            Collection allActions = workflow.getAllActions();
            if (allActions != null)
            {
                for (Iterator iterator = allActions.iterator(); iterator.hasNext();)
                {
                    ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
                    RestrictionDescriptor restriction = actionDescriptor.getRestriction();
                    if (restriction != null)
                    {
                        boolean changedRestriction = upgradeRestriction(restriction, workflow.isSystemWorkflow());
                        changedWorkflow = changedWorkflow || changedRestriction;
                    }
                }
            }
        }
        return changedWorkflow;
    }

    void saveWorkflow(JiraWorkflow workflow)
    {
        try
        {
            if (!workflow.isSystemWorkflow())
            {
                workflowManager.saveWorkflowWithoutAudit(workflow);
            }
            else
            {
                log.info("***********************************************************************************************");
                log.info("Cannot save workflow '" + workflow.getName() + "' as it is not stored in the database.");
                log.info("Please ensure that the 'statuses' arg of the " + SubTaskBlockingCondition.class.getName() + " consists of only integers that represent valid status ids.");
                log.info("Specifically, please ensure that strings 'nested' and 'count' do not appear in the arg's value.");
                log.info("***********************************************************************************************");
            }
        }
        catch (WorkflowException e)
        {
            log.error("Error while working with workflow.", e);
        }
    }

    boolean upgradeRestriction(RestrictionDescriptor restriction, boolean isSystemWorkflow)
    {
        if (restriction != null)
        {
            ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
            if (conditionsDescriptor != null)
            {
                return upgradeConditionsDescriptor(conditionsDescriptor, isSystemWorkflow);
            }
        }

        return false;
    }

    boolean upgradeConditionsDescriptor(ConditionsDescriptor conditionsDescriptor, boolean isSystemWorkflow)
    {
        boolean changedAnything = false;
        if (conditionsDescriptor != null)
        {
            Collection conditions = conditionsDescriptor.getConditions();
            if (conditions != null)
            {
                for (Iterator iterator = conditions.iterator(); iterator.hasNext();)
                {
                    Object descriptor = iterator.next();
                    if (descriptor instanceof ConditionsDescriptor)
                    {
                        boolean changedDescriptor = upgradeConditionsDescriptor((ConditionsDescriptor) descriptor, isSystemWorkflow);
                        changedAnything = changedDescriptor || changedAnything;
                    }
                    else if (descriptor instanceof ConditionDescriptor)
                    {
                        boolean changedDescriptor = upgradeConditionDescriptor((ConditionDescriptor) descriptor, isSystemWorkflow);
                        changedAnything = changedDescriptor || changedAnything;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Object of type " + descriptor.getClass().getName() + " is not a condition.");
                    }
                }
            }
        }

        return changedAnything;
    }

    boolean upgradeConditionDescriptor(ConditionDescriptor descriptor, boolean isSystemWorkflow)
    {
        boolean changedDescriptor = false;
        if (descriptor != null && "class".equals(descriptor.getType()))
        {
            Map args = descriptor.getArgs();
            if (args != null)
            {
                String className = (String) args.get(CLASS_NAME_PARAM);
                if (className != null && SubTaskBlockingCondition.class.getName().equals(className))
                {
                    log.info(SubTaskBlockingCondition.class.getName() + " found in workflow.");
                    String statusesParam = (String) args.get(STATUSES_PARAM_NAME);
                    if (statusesParam != null)
                    {
                        StringBuffer sb = new StringBuffer();
                        StringTokenizer st = new StringTokenizer(statusesParam, ",");
                        while (st.hasMoreTokens())
                        {
                            String statusId = st.nextToken();

                            try
                            {
                                Long.parseLong(statusId);
                                sb.append(statusId).append(",");
                            }
                            catch (NumberFormatException e)
                            {
                                if (!isSystemWorkflow)
                                {
                                    log.info("Removing parameter value '" + statusId + "' from argument of " + SubTaskBlockingCondition.class.getName());
                                }
                                else
                                {
                                    log.info("Cannot remove '" + statusId + "' from 'statuses' argument of '" + SubTaskBlockingCondition.class.getName() + "' as the workflow is not stored in the database.");
                                }

                                // We have modified the descriptor
                                changedDescriptor = true;
                            }
                        }

                        if (sb.length() > 0)
                        {
                            // Remove the last comma.
                            sb.deleteCharAt(sb.length() - 1);
                        }

                        // Only try to update the worfklow if we have changed something
                        if (changedDescriptor)
                        {
                            // Only update the workflow if we can save it to the database
                            if (!isSystemWorkflow)
                            {
                                descriptor.getArgs().put(STATUSES_PARAM_NAME, sb.toString());
                            }
                        }
                    }
                }
            }
        }

        return changedDescriptor;
    }
}
