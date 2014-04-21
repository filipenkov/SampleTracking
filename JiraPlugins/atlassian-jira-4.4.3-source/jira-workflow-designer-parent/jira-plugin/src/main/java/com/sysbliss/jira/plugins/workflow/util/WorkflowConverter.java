/**
 *
 */
package com.sysbliss.jira.plugins.workflow.util;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.web.bean.WorkflowConditionFormatBean;
import com.atlassian.jira.web.bean.WorkflowDescriptorInfo;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.opensymphony.workflow.loader.*;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowAnnotationManager;
import com.sysbliss.jira.plugins.workflow.model.*;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.*;

/**
 * @author jdoklovic
 */
public class WorkflowConverter {

    private WorkflowConverter() {

    }

    public static FlexJiraWorkflow convertMinimalWorkflow(final JiraWorkflow jiraWorkflow, final WorkflowSchemeManager schemeManager) {
        final FlexJiraWorkflow fjw = new FlexJiraWorkflowImpl();
        fjw.setName(jiraWorkflow.getName());
        fjw.setDescription(jiraWorkflow.getDescription());
        fjw.setIsEditable(jiraWorkflow.isEditable());
        fjw.setHasDraftWorkflow(jiraWorkflow.hasDraftWorkflow());
        fjw.setIsActive(jiraWorkflow.isActive());
        fjw.setIsDraftWorkflow(jiraWorkflow.isDraftWorkflow());
        fjw.setIsSystemWorkflow(jiraWorkflow.isSystemWorkflow());
        fjw.setId(jiraWorkflow.getDescriptor().getId());
        fjw.setEntityId(jiraWorkflow.getDescriptor().getEntityId());

        boolean hasSchemes = false;
        final Collection workflowSchemes = schemeManager.getSchemesForWorkflow(jiraWorkflow);
        if (workflowSchemes.size() > 0) {
            hasSchemes = true;
        }

        fjw.setHasSchemes(hasSchemes);

        return fjw;
    }

    public static FlexJiraWorkflow convertFullWorkflow(final JiraWorkflow jiraWorkflow, final WorkflowSchemeManager schemeManager, final WorkflowAnnotationManager workflowAnnotationManager) {
        final FlexJiraWorkflow fjw = convertMinimalWorkflow(jiraWorkflow, schemeManager);

        fjw.setInitialActions(convertActions(jiraWorkflow.getDescriptor().getInitialActions()));

        fjw.setAllSteps(convertSteps(jiraWorkflow.getDescriptor().getSteps(), jiraWorkflow));

        fjw.setUnlinkedStatuses(convertUnlinkedStatuses(jiraWorkflow));

        fjw.setAllActions(getAllActions(jiraWorkflow));

        fjw.setGlobalActions(getGlobalActions(jiraWorkflow));
        
        List<WorkflowAnnotation> annotations;

        try {
            annotations= workflowAnnotationManager.getAnnotationsForWorkflow(jiraWorkflow);
        } catch (Exception e) {
            annotations = new ArrayList<WorkflowAnnotation>();
        }

        fjw.setWorkflowAnnotations(annotations);

        fjw.setIsLoaded(true);

        return fjw;
    }


    private static List getAllActions(final JiraWorkflow jiraWorkflow) {
        List<FlexJiraAction> allActions = new ArrayList();
        for (Object obj : jiraWorkflow.getAllActions()) {
            ActionDescriptor action = (ActionDescriptor) obj;
            //if (!jiraWorkflow.isInitialAction(action)) {
                allActions.add(convertAction(action));
            //}
        }

        return allActions;
    }

    private static List getGlobalActions(final JiraWorkflow jiraWorkflow) {
        final ArrayList actions = new ArrayList();
        final Iterator it = jiraWorkflow.getDescriptor().getGlobalActions().iterator();
        ActionDescriptor action;
        FlexJiraAction fjAction;
        while (it.hasNext()) {
            action = (ActionDescriptor) it.next();
            fjAction = convertAction(action);
            actions.add(fjAction);
        }

        return actions;
    }

    private static List convertActions(final List jiraActions) {
        final ArrayList actions = new ArrayList();
        FlexJiraAction action;
        for (final Iterator iterator = jiraActions.iterator(); iterator.hasNext();) {
            final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
            action = convertAction(actionDescriptor);
            actions.add(action);
        }

        return actions;
    }

    public static FlexJiraAction convertAction(final ActionDescriptor actionDescriptor) {
        final FlexJiraAction action = new FlexJiraActionImpl();
        action.setId(actionDescriptor.getId());
        action.setEntityId(actionDescriptor.getEntityId());
        action.setName(actionDescriptor.getName());
        action.setLabel(actionDescriptor.getName() + " (" + actionDescriptor.getId() + ")");
        action.setIsCommon(actionDescriptor.isCommon());

        final Map metaData = actionDescriptor.getMetaAttributes();
        final String desc = (String) metaData.get(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE);
        action.setDescription(desc);

        final FlexJiraResult unconditionalResult = convertResult(actionDescriptor.getUnconditionalResult());
        action.setUnconditionalResult(unconditionalResult);

        action.setMetaAttributes(metaData);

        final RestrictionDescriptor restriction = actionDescriptor.getRestriction();
        if (null != restriction) {
            action.setConditions(convertConditions(actionDescriptor.getRestriction().getConditionsDescriptor()));
        }

        action.setView(actionDescriptor.getView());

        if (StringUtils.isNotBlank(actionDescriptor.getView())) {
            final WorkflowActionsBean actionsBean = new WorkflowActionsBean();
            final FieldScreen fieldScreen = actionsBean.getFieldScreenForView(actionDescriptor);
            action.setFieldScreenId(fieldScreen.getId().toString());
        } else {
            action.setFieldScreenId("");
        }

        action.setValidators(convertValidators(actionDescriptor.getValidators()));

        return action;
    }

    private static FlexJiraResult convertResult(final ResultDescriptor jiraResult) {
        final FlexJiraResult result = new FlexJiraResultImpl();

        result.setId(jiraResult.getId());
        result.setEntityId(jiraResult.getEntityId());
        result.setName(jiraResult.getDisplayName());
        result.setDescription("");
        result.setOldStatus(jiraResult.getOldStatus());
        result.setStatus(jiraResult.getStatus());
        result.setStepId(jiraResult.getStep());
        result.setPostFunctions(convertFunctions(jiraResult.getPostFunctions()));

        return result;
    }

    private static List convertFunctions(final List jiraFunctions) {
        final ArrayList functions = new ArrayList();
        FlexJiraFunction function;
        for (final Iterator iterator = jiraFunctions.iterator(); iterator.hasNext();) {
            final FunctionDescriptor functionDescriptor = (FunctionDescriptor) iterator.next();
            function = convertFunction(functionDescriptor);
            functions.add(function);
        }

        return functions;
    }

    private static FlexJiraFunction convertFunction(final FunctionDescriptor jiraFunction) {
        final FlexJiraFunction function = new FlexJiraFunctionImpl();

        function.setName(jiraFunction.getName());
        function.setType(jiraFunction.getType());
        function.setId(jiraFunction.getId());
        function.setEntityId(jiraFunction.getEntityId());
        function.setArgs(jiraFunction.getArgs());
        return function;
    }

    /**
     * @param jiraConditions
     * @return
     */
    private static FlexJiraConditions convertConditions(final ConditionsDescriptor jiraConditions) {
        final FlexJiraConditions conditions = new FlexJiraConditionsImpl();
        if (null == jiraConditions) {
            return conditions;
        }

        conditions.setId(jiraConditions.getId());
        conditions.setType(jiraConditions.getType());
        final ArrayList nestedConditions = new ArrayList();

        final List nestedJiraConditions = jiraConditions.getConditions();
        for (final Iterator iterator = nestedJiraConditions.iterator(); iterator.hasNext();) {
            final Object o = iterator.next();
            if (o instanceof ConditionDescriptor) {
                nestedConditions.add(convertCondition((ConditionDescriptor) o));
            } else if (o instanceof ConditionsDescriptor) {
                nestedConditions.add(convertConditions((ConditionsDescriptor) o));
            }
        }

        conditions.setConditions(nestedConditions);

        return conditions;
    }

    /**
     * @param jiraCondition
     * @return
     */
    private static FlexJiraCondition convertCondition(final ConditionDescriptor jiraCondition) {
        final FlexJiraCondition condition = new FlexJiraConditionImpl();
        condition.setId(jiraCondition.getId());
        condition.setEntityId(jiraCondition.getEntityId());
        condition.setName(jiraCondition.getName());
        condition.setType(jiraCondition.getType());
        condition.setArgs(jiraCondition.getArgs());

        final WorkflowConditionFormatBean formatBean = new WorkflowConditionFormatBean();
        formatBean.setPluginType("workflow-condition");
        final WorkflowDescriptorInfo info = formatBean.formatDescriptor(jiraCondition);
        condition.setDescription(info.getDescription());
        return condition;
    }

    private static List convertValidators(final List jiraValidators) {
        final ArrayList validators = new ArrayList();
        FlexJiraValidator validator;

        for (final Iterator iterator = jiraValidators.iterator(); iterator.hasNext();) {
            final ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) iterator.next();
            validator = convertValidator(validatorDescriptor);
            validators.add(validator);
        }

        return validators;
    }

    private static FlexJiraValidator convertValidator(final ValidatorDescriptor jiraValidator) {
        final FlexJiraValidator validator = new FlexJiraValidatorImpl();

        validator.setName(jiraValidator.getName());
        validator.setType(jiraValidator.getType());
        validator.setId(jiraValidator.getId());
        validator.setEntityId(jiraValidator.getEntityId());
        validator.setArgs(jiraValidator.getArgs());

        return validator;
    }

    /**
     * @param jiraSteps
     * @return
     */
    private static List convertSteps(final List jiraSteps, final JiraWorkflow jiraWorkflow) {
        final ArrayList steps = new ArrayList();
        FlexJiraStep step;

        for (final Iterator iterator = jiraSteps.iterator(); iterator.hasNext();) {
            final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
            step = convertStep(stepDescriptor, jiraWorkflow);
            steps.add(step);
        }

        return steps;
    }

    /**
     * @param jiraStep
     * @return
     */
    public static FlexJiraStep convertStep(final StepDescriptor jiraStep, final JiraWorkflow jiraWorkflow) {
        final FlexJiraStep step = new FlexJiraStepImpl();
        step.setId(jiraStep.getId());
        step.setEntityId(jiraStep.getEntityId());
        step.setName(jiraStep.getName());
        step.setMetaAttributes(jiraStep.getMetaAttributes());

        final Map metaData = jiraStep.getMetaAttributes();
        step.setLinkedStatus((String) metaData.get(JiraWorkflow.STEP_STATUS_KEY));

        final List stepActions = convertActions(jiraStep.getActions());

        step.setActions(stepActions);

        return step;
    }

    /**
     * @param jiraWorkflow
     * @return
     */
    private static List convertUnlinkedStatuses(final JiraWorkflow jiraWorkflow) {
        final List ret = new ArrayList();

        final ConstantsManager manager = ManagerFactory.getConstantsManager();
        final List statusList = new ArrayList(manager.getStatusObjects());

        for (final Iterator iterator = statusList.iterator(); iterator.hasNext();) {
            final Status status = (Status) iterator.next();
            final GenericValue statusGV = status.getGenericValue();

            if (!isStatusLinked(statusGV, jiraWorkflow)) {
                final FlexJiraStatus fjs = new FlexJiraStatusImpl();
                fjs.setId(status.getId());
                fjs.setName(status.getName());
                fjs.setDescription(status.getDescription());
                fjs.setIconUrl(status.getIconUrl());

                ret.add(fjs);
            }

        }

        return ret;
    }

    private static boolean isStatusLinked(final GenericValue status, final JiraWorkflow jiraWorkflow) {
        for (final Iterator iterator = jiraWorkflow.getDescriptor().getSteps().iterator(); iterator.hasNext();) {
            final StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
            if (status.getString("id").equals(stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY))) {
                return true;
            }
        }
        return false;
    }
}
