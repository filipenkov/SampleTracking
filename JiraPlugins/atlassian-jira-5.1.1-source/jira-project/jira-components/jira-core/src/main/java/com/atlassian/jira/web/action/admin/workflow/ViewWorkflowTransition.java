/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionalResultDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@WebSudoRequired
public class ViewWorkflowTransition extends AbstractWorkflowTransitionAction
{
    public static final String DESCRIPTOR_TAB_ALL = "all";
    public static final String DESCRIPTOR_TAB_CONDITIONS = "conditions";
    public static final String DESCRIPTOR_TAB_VALIDATORS = "validators";
    public static final String DESCRIPTOR_TAB_POST_FUNCTIONS = "postfunctions";
    public static final String DESCRIPTOR_TAB_OTHER = "other";
    public static final String DESCRIPTOR_TAB_DEFAULT = DESCRIPTOR_TAB_CONDITIONS;


    private final ConstantsManager constantsManager;
    private final CollectionReorderer collectionReorderer;
    private final WorkflowActionsBean workflowActionsBean;

    private int up;
    private int down;

    private String count;
    private String currentCount;

    private String descriptorTab;


    public ViewWorkflowTransition(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, ConstantsManager constantsManager, CollectionReorderer collectionReorderer,
            WorkflowService workflowService)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
        this.constantsManager = constantsManager;
        this.collectionReorderer = collectionReorderer;
        this.workflowActionsBean = new WorkflowActionsBean();
    }

    public ViewWorkflowTransition(JiraWorkflow workflow, ActionDescriptor transition, PluginAccessor pluginAccessor,
            ConstantsManager constantsManager, CollectionReorderer collectionReorderer, WorkflowService workflowService)
    {
        // Used for working with global actions 
        super(workflow, transition, pluginAccessor, workflowService);
        this.constantsManager = constantsManager;
        this.collectionReorderer = collectionReorderer;
        this.workflowActionsBean = new WorkflowActionsBean();
    }

    public StepDescriptor getStepDescriptor(ConditionalResultDescriptor conditionalResultDescriptor)
    {
        final int targetStepId = conditionalResultDescriptor.getStep();
        return getWorkflow().getDescriptor().getStep(targetStepId);
    }

    public GenericValue getStatus(String id)
    {
        return constantsManager.getStatus(id);
    }

    @RequiresXsrfCheck
    public String doMoveWorkflowFunctionUp() throws Exception
    {
        final List postFunctions = getTransition().getUnconditionalResult().getPostFunctions();

        if (up <= 0 || up >= postFunctions.size())
        {
            addErrorMessage(getText("admin.errors.workflows.invalid.index", "" + up));
        }
        else
        {
            Object toMove = postFunctions.get(up);
            collectionReorderer.increasePosition(postFunctions, toMove);
            workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
        }

        return getViewRedirect("&currentCount=workflow-function" + (up));
    }

    @RequiresXsrfCheck
    public String doMoveWorkflowFunctionDown() throws Exception
    {
        final List postFunctions = getTransition().getUnconditionalResult().getPostFunctions();

        if (down < 0 || down >= (postFunctions.size() - 1))
        {
            addErrorMessage(getText("admin.errors.workflows.invalid.index", "" + down));
        }
        else
        {
            Object toMove = postFunctions.get(down);
            collectionReorderer.decreasePosition(postFunctions, toMove);
            workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
        }

        return getViewRedirect("&currentCount=workflow-function" + (down + 2));
    }

    @RequiresXsrfCheck
    public String doChangeLogicOperator() throws Exception
    {
        WorkflowEditorTransitionConditionUtil wetcu = new WorkflowEditorTransitionConditionUtil();
        wetcu.changeLogicOperator(getTransition(), getCount());

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getViewRedirect("");
    }

    protected String getViewRedirect(String postfix)
    {
        if (getStep() == null)
        {
            return getRedirect("ViewWorkflowTransition.jspa?workflowName=" + URLEncoder.encode(getWorkflow().getName()) +
                               "&workflowMode=" + getWorkflow().getMode() +
                               "&workflowTransition=" + getTransition().getId() + postfix);
        }
        else
        {
            return getRedirect("ViewWorkflowTransition.jspa?workflowName=" + URLEncoder.encode(getWorkflow().getName()) + 
                               "&workflowMode=" + getWorkflow().getMode() +
                               "&workflowStep=" + getStep().getId() +
                               "&workflowTransition=" + getTransition().getId() + postfix);
        }
    }

    public int getUp()
    {
        return up;
    }

    public void setUp(int up)
    {
        this.up = up;
    }

    public int getDown()
    {
        return down;
    }

    public void setDown(int down)
    {
        this.down = down;
    }

    public Collection getStepsForTransition()
    {
        return getWorkflow().getStepsForTransition(getTransition());
    }

    public boolean isInitial()
    {
        return getWorkflow().isInitialAction(getTransition());
    }

    public boolean isGlobal()
    {
        return getWorkflow().isGlobalAction(getTransition());
    }

    public boolean isCommon()
    {
        return getWorkflow().isCommonAction(getTransition());
    }

    public boolean isTransitionWithoutStepChange()
    {
        return getTransition().getUnconditionalResult().getStep() == JiraWorkflow.ACTION_ORIGIN_STEP_ID;
    }

    public String getDescriptorTab()
    {
        if (!TextUtils.stringSet(descriptorTab))
        {
            descriptorTab = (String) ActionContext.getSession().get(SessionKeys.WF_EDITOR_TRANSITION_TAB);

            if (!TextUtils.stringSet(descriptorTab))
            {
                descriptorTab = DESCRIPTOR_TAB_DEFAULT;
            }
            if (isInitial() && DESCRIPTOR_TAB_CONDITIONS.equals(descriptorTab))
            {
                descriptorTab = DESCRIPTOR_TAB_VALIDATORS;
            }
        }

        if (DESCRIPTOR_TAB_OTHER.equals(descriptorTab) && !isShowOtherTab())
        {
            descriptorTab = DESCRIPTOR_TAB_DEFAULT;
        }

        return descriptorTab;
    }

    public void setDescriptorTab(String descriptorTab)
    {
        if (TextUtils.stringSet(descriptorTab))
        {
            ActionContext.getSession().put(SessionKeys.WF_EDITOR_TRANSITION_TAB, descriptorTab);
        }

        this.descriptorTab = descriptorTab;
    }

    public int getNumberConditions()
    {
        RestrictionDescriptor restriction = getTransition().getRestriction();
        if (restriction != null)
        {
            return getNumberConditions(restriction.getConditionsDescriptor());
        }
        else
        {
            return 0;
        }
    }

    private int getNumberConditions(ConditionsDescriptor conditionsDescriptor)
    {
        int number = 0;
        if (conditionsDescriptor != null)
        {
            Collection conditions = conditionsDescriptor.getConditions();
            if (conditions != null)
            {
                for (Iterator iterator = conditions.iterator(); iterator.hasNext();)
                {
                    Object o = iterator.next();
                    if (o instanceof ConditionDescriptor)
                    {
                        number++;
                    }
                    else if (o instanceof ConditionsDescriptor)
                    {
                        number += getNumberConditions((ConditionsDescriptor) o);
                    }
                    else
                    {
                        throw new IllegalArgumentException("Invalid object " + o + " found in condition collection.");
                    }
                }
            }
        }

        return number;
    }

    public boolean isShowOtherTab()
    {
        if (getTransition().getConditionalResults() != null && !getTransition().getConditionalResults().isEmpty())
        {
            return true;
        }

        ResultDescriptor unconditionalResult = getTransition().getUnconditionalResult();
        if (unconditionalResult != null)
        {
            if (unconditionalResult.getValidators() != null && !unconditionalResult.getValidators().isEmpty())
            {
                return true;
            }

            if (unconditionalResult.getPreFunctions() != null && !unconditionalResult.getPreFunctions().isEmpty())
            {
                return true;
            }
        }

        if (getTransition().getPreFunctions() != null && !getTransition().getPreFunctions().isEmpty())
        {
            return true;
        }

        if (getTransition().getPostFunctions() != null && !getTransition().getPostFunctions().isEmpty())
        {
            return true;
        }

        return false;
    }

    public FieldScreen getFieldScreen()
    {
        return workflowActionsBean.getFieldScreenForView(getTransition());
    }

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }

    public String getCurrentCount()
    {
        return currentCount;
    }

    public void setCurrentCount(String currentCount)
    {
        this.currentCount = currentCount;
    }
}