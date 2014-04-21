/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkEditAction;
import com.atlassian.jira.bulkedit.operation.BulkEditActionImpl;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueUtilsBean;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.issue.util.ScreenTabErrorHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.jelly.util.NestedRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class BulkWorkflowTransition extends AbstractBulkOperationDetailsAction
{
    private static final String WORKFLOW_TRANSITION = "wftransition";
    public static final String RADIO_ERROR_MSG = "buik.edit.must.select.one.action.to.perform";
    private static final String FORCED_RESOLUTION = "forcedResolution";

    // actions array is retrieved from the checkbox group in the JSP called "actions"
    // this stores the fields that the user has indicated an intention to bulk edit (ie. my checking it)
    private String[] actions;
    private Map selectedActions;
    private String commentaction;

    // Cache the fields available for editing per field screen tab
    // Maps the field tab name -> collection of available fields for editing
    private Map editActionsMap;

    private SortedSet tabsWithErrors;
    private int selectedTab;

    private final IssueUtilsBean issueUtilsBean;
    private final WorkflowManager workflowManager;
    private final IssueManager issueManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final JiraAuthenticationContext authenticationContext;
    private final BulkWorkflowTransitionOperation bulkWorkflowTransitionOperation;
    private BulkEditActionImpl commentBulkEditAction;
    private final PermissionManager permissionManager;

    public BulkWorkflowTransition(SearchService searchService, IssueUtilsBean issueUtilsBean, WorkflowManager workflowManager, IssueManager issueManager,
            FieldScreenRendererFactory fieldScreenRendererFactory, FieldManager fieldManager, JiraAuthenticationContext authenticationContext, FieldLayoutManager fieldLayoutManager,
            BulkWorkflowTransitionOperation bulkWorkflowTransitionOperation, PermissionManager permissionManager)
    {
        super(searchService);
        this.issueUtilsBean = issueUtilsBean;
        this.workflowManager = workflowManager;
        this.issueManager = issueManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
        this.fieldLayoutManager = fieldLayoutManager;
        this.bulkWorkflowTransitionOperation = bulkWorkflowTransitionOperation;
        this.permissionManager = permissionManager;
    }


    // ---- Webwork Actions --------------------------------------------------------------------------------------------
    public String doDetails() throws Exception
    {
        BulkEditBean bulkEditBean = getBulkEditBean();

        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (bulkEditBean == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        bulkEditBean.setCurrentStep(3);
        getBulkEditBean().addAvailablePreviousStep(2);

        // Ensure that bulk notification can be disabled
        if (isCanDisableMailNotifications())
            bulkEditBean.setSendBulkNotification(false);
        else
            bulkEditBean.setSendBulkNotification(true);

        setWorkflowTransitionMap();

        // Ensure no selections are still available
        bulkEditBean.setActions(null);
        bulkEditBean.setSelectedWFTransitionKey(null);

        return INPUT;
    }

    public String doDetailsValidation() throws Exception
    {
        if(getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        if (!setWorkflowTransitionSelection())
        {
            addErrorMessage(getText("bulkworkflowtransition.select.transition.error"));
            return ERROR;
        }

        initBeanWithSelection();

        return SUCCESS;
    }

    private void initBeanWithSelection() throws Exception
    {
        MultiMap workflowTransitionMap = getBulkEditBean().getWorkflowTransitionMap();
        Collection issues = new ArrayList();

        WorkflowTransitionKey workflowTransitionKey = getBulkEditBean().getSelectedWFTransitionKey();
        Collection issueKeys = (Collection) workflowTransitionMap.get(workflowTransitionKey);
        ActionDescriptor actionDescriptor = getBulkWorkflowTransitionOperation().getActionDescriptor(workflowTransitionKey);

        for (Iterator iterator1 = issueKeys.iterator(); iterator1.hasNext();)
        {
            String issueKey = (String) iterator1.next();
            Issue issue = issueManager.getIssueObject(issueKey);
            issues.add(issue);
        }

        getBulkEditBean().initSelectedIssues(issues);

        FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(issues, actionDescriptor);

        getBulkEditBean().setFieldScreenRenderer(fieldScreenRenderer);
    }

    public String doEditValidation()
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        validateInput();

        if (invalidInput())
            return ERROR;
        else
        {
            updateBean();
            return SUCCESS;
        }
    }

    public String doPerform() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        validationPerform();

        if (invalidInput())
            return ERROR;

        getBulkWorkflowTransitionOperation().perform(getBulkEditBean(), getRemoteUser());

        return finishWizard();
    }

    /**
     * Determine if the bulk workflow transition action can be completed
     */
    private void validationPerform()
    {
        // Ensure the user has the global BULK CHANGE permission
        if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getRemoteUser()))
        {
            addErrorMessage(getText("bulk.change.no.permission", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            return;
        }

        if (!getBulkWorkflowTransitionOperation().canPerform(getBulkEditBean(), getRemoteUser()))
        {
            addErrorMessage(getText("bulk.workflowtransition.cannotperform", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            return;
        }
    }

    // ---- Edit Actions -----------------------------------------------------------------------------------------------

    // Retrive edit actions appearing on the specified field screen tab
    public Collection getEditActions(String fieldScreenTabName)
    {
        if (editActionsMap == null)
        {
            editActionsMap = new HashMap();
        }
        else if (editActionsMap.containsKey(fieldScreenTabName))
        {
            return (Collection) editActionsMap.get(fieldScreenTabName);
        }

        Collection editActions = new ArrayList();

        Collection fieldScreenRenderTabs = getBulkEditBean().getFieldScreenRenderer().getFieldScreenRenderTabs();

        for (Iterator iterator = fieldScreenRenderTabs.iterator(); iterator.hasNext();)
        {
            FieldScreenRenderTab screenRenderTab = (FieldScreenRenderTab) iterator.next();
            if (screenRenderTab.getName().equals(fieldScreenTabName))
            {
                Collection bulkFieldScreenRenderLayoutItems = screenRenderTab.getFieldScreenRenderLayoutItems();

                for (Iterator iterator1 = bulkFieldScreenRenderLayoutItems.iterator(); iterator1.hasNext();)
                {
                    FieldScreenRenderLayoutItem bulkFieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                    String actionName = bulkFieldScreenRenderLayoutItem.getFieldScreenLayoutItem().getFieldId();
                    editActions.add(buildBulkEditAction(actionName));
                }
                break;
            }
        }

        editActionsMap.put(fieldScreenTabName, editActions);
        return editActions;
    }

    public BulkEditAction getCommentBulkEditAction()
    {
        if (commentBulkEditAction == null)
            commentBulkEditAction = new BulkEditActionImpl(IssueFieldConstants.COMMENT, fieldManager, authenticationContext);

        return commentBulkEditAction;
    }

    public String getCommentHtml()
    {
        OrderableField commentField = fieldManager.getOrderableField(IssueFieldConstants.COMMENT);
        boolean required = false;
        boolean hidden = false;
        String rendererType = null;

        for (Iterator iterator = getBulkEditBean().getFieldLayouts().iterator(); iterator.hasNext();)
        {
            FieldLayout fieldLayout = (FieldLayout) iterator.next();
            FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(commentField);
            if (fieldLayoutItem.isHidden())
                hidden = true;

            if (fieldLayoutItem.isRequired())
                required = true;

            // If the field is using different renderers then it should not be available for
            // editing. So just record a renderer here
            rendererType = fieldLayoutItem.getRendererType();

            // If both are true then no need to look further
            if (hidden && required)
                break;
        }

        FieldLayoutItemImpl fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(commentField)
                .setFieldDescription(null)
                .setHidden(hidden)
                .setRequired(required)
                .setRendererType(rendererType)
                .build();

        return commentField.getEditHtml(fieldLayoutItem, getBulkEditBean(), this, null, EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE));
    }


    public Map getAllEditActions()
    {
        Collection fieldScreenRenderTabs = getBulkEditBean().getFieldScreenRenderer().getFieldScreenRenderTabs();

        Map editActions = new HashMap();

        for (Iterator iterator = fieldScreenRenderTabs.iterator(); iterator.hasNext();)
        {

            FieldScreenRenderTab screenRenderTab = (FieldScreenRenderTab) iterator.next();
            Collection bulkFieldScreenRenderLayoutItems = screenRenderTab.getFieldScreenRenderLayoutItems();

            for (Iterator iterator1 = bulkFieldScreenRenderLayoutItems.iterator(); iterator1.hasNext();)
            {
                FieldScreenRenderLayoutItem bulkFieldScreenRenderLayoutItem = (FieldScreenRenderLayoutItem) iterator1.next();
                String actionName = bulkFieldScreenRenderLayoutItem.getFieldScreenLayoutItem().getFieldId();
                editActions.put(actionName, buildBulkEditAction(actionName));
            }
        }

        return editActions;

    }

    // Build a BulkEditAction for specified field
    private BulkEditAction buildBulkEditAction(String fieldId)
    {
        return new BulkEditActionImpl(fieldId, fieldManager, authenticationContext);
    }

    // Validate the edit actions selected
    private void validateInput()
    {
        selectedActions = new ListOrderedMap();
        
        if (getActions() != null && getActions().length != 0)
        {
            final Map allEditActions = getAllEditActions();

            for (int i = 0; i < getActions().length; i++)
            {
                String fieldId = getActions()[i];
                BulkEditAction bulkEditAction = (BulkEditAction) allEditActions.get(fieldId);
                selectedActions.put(bulkEditAction.getField().getId(), bulkEditAction);
                bulkEditAction.getField().populateFromParams(getBulkEditBean().getFieldValuesHolder(), ActionContext.getParameters());
                for (Iterator iterator1 = getBulkEditBean().getSelectedIssues().iterator(); iterator1.hasNext();)
                {
                    Issue issue = (Issue) iterator1.next();
                    bulkEditAction.getField().validateParams(getBulkEditBean(), this, this, issue, buildFieldScreenRenderLayoutItem(bulkEditAction.getField(), issue.getGenericValue()));
                }
            }
        }

        // Validate Comment
        if (TextUtils.stringSet(getCommentaction()))
        {
            BulkEditAction bulkEditAction = getCommentBulkEditAction();
            if (bulkEditAction.isAvailable(getBulkEditBean()))
            {
                selectedActions.put(bulkEditAction.getField().getId(), bulkEditAction);
                bulkEditAction.getField().populateFromParams(getBulkEditBean().getFieldValuesHolder(), ActionContext.getParameters());
                for (Iterator iterator1 = getBulkEditBean().getSelectedIssues().iterator(); iterator1.hasNext();)
                {
                    Issue issue = (Issue) iterator1.next();
                    bulkEditAction.getField().validateParams(getBulkEditBean(), this, this, issue, buildFieldScreenRenderLayoutItem(bulkEditAction.getField(), issue.getGenericValue()));
                }
            }
            else
            {
                addErrorMessage(getText("bulkworkflowtransition.comment.cannotspecify"));
            }
        }
    }

    // Check if there are available transitions on the selected issues
    public boolean isHasAvailableActions() throws Exception
    {
        return getBulkWorkflowTransitionOperation().canPerform(getBulkEditBean(), getRemoteUser());
    }

    private void updateBean()
    {
        if (selectedActions != null && !selectedActions.isEmpty())
        {
            // set values in bean once form data has been validated
            getBulkEditBean().setActions(selectedActions);
            try
            {
                for (Iterator iterator = getBulkEditBean().getActions().values().iterator(); iterator.hasNext();)
                {
                    BulkEditAction bulkEditAction = (BulkEditAction) iterator.next();
                    OrderableField field = bulkEditAction.getField();
                    Object value = field.getValueFromParams(getBulkEditBean().getFieldValuesHolder());
                    getBulkEditBean().getFieldValues().put(field.getId(), value);
                }
            }
            catch (FieldValidationException e)
            {
                log.error("Error getting field value.", e);
                throw new NestedRuntimeException("Error getting field value.", e);
            }
        }

        getBulkEditBean().clearAvailablePreviousSteps();
        getBulkEditBean().addAvailablePreviousStep(1);
        getBulkEditBean().addAvailablePreviousStep(2);
        getBulkEditBean().addAvailablePreviousStep(3);
        getBulkEditBean().setCurrentStep(4);
    }

    public String getOperationDetailsActionName()
    {
        return getBulkWorkflowTransitionOperation().getOperationName() + "Details.jspa";
    }

    // Sets checkbox value if field validation error occurred.
    public boolean isChecked(String value)
    {
        if (getActions() == null || getActions().length == 0)
        {
            // If there were no actions submitted we are either being invoked with no check boxes checked
            // (which should be OK, as there is nothing to validate), or we are coming from the later stage of
            // the wizard. In this case we should look into BulkEditBean
            if (getBulkEditBean().getActions() != null)
            {
                return getBulkEditBean().getActions().containsKey(value);
            }

            return false;
        }
        else
        {
            if (IssueFieldConstants.COMMENT.equals(value))
            {
                return TextUtils.stringSet(getCommentaction());
            }

            // If we have check boxes (actions) submitted use them
            for (int i = 0; i < getActions().length; i++)
            {
                String action = getActions()[i];
                if (action.equals(value))
                    return true;
            }

            return false;
        }
    }

    // ---- Workflow Transition Mappings -------------------------------------------------------------------------------

    /**
     * Initialise the 'workflow to transition' multimap.
     * <p/>
     * Multimap:
     * Key = WorkflowTransitionKey
     * Value = Collection of Issue Keys
     *
     * @throws WorkflowException
     */
    private void setWorkflowTransitionMap() throws WorkflowException
    {
        if (getBulkEditBean().getWorkflowTransitionMap() == null || getBulkEditBean().getWorkflowTransitionMap().isEmpty())
        {
            Collection selectedIssues = getBulkEditBean().getSelectedIssues();
            MultiMap workflowTransitionMap = new MultiHashMap();

            for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext();)
            {
                Issue issue = (Issue) iterator.next();
                final JiraWorkflow workflow = workflowManager.getWorkflow(issue.getGenericValue());
                String workflowName = workflow.getName();

                Map availableActions = issueUtilsBean.loadAvailableActions(issue);
                Collection actions = availableActions.values();

                for (Iterator iterator1 = actions.iterator(); iterator1.hasNext();)
                {
                    ActionDescriptor actionDescriptor = (ActionDescriptor) iterator1.next();
                    String actionDescriptorId = String.valueOf(actionDescriptor.getId());

                    int resultStep = actionDescriptor.getUnconditionalResult().getStep();
                    WorkflowTransitionKey workflowTransitionKey;
                    if(resultStep == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
                    {
                        //JRA-12017: if we have workflowtransition that goes back to itself, we use the original status in for the
                        //destination status.  This means that if two issues have different statuses currently, they will
                        //show up as two separate lines in the transitions screeen.  This is correct, as permissions may
                        //be different dependening on the destination status, which determines what may be shown on
                        //the fields screen on the next page in the wizard.
                        workflowTransitionKey = new WorkflowTransitionKey(workflowName, actionDescriptorId, issue.getStatusObject().getId());
                    }
                    else
                    {
                        Status resultStatus = workflow.getLinkedStatusObject(workflow.getDescriptor().getStep(resultStep));
                        workflowTransitionKey = new WorkflowTransitionKey(workflowName, actionDescriptorId,
                                resultStatus.getId());
                    }

                    workflowTransitionMap.put(workflowTransitionKey, issue.getKey());
                }

            }

            getBulkEditBean().setWorkflowTransitionMap(workflowTransitionMap);
        }
    }

    // Set the selected transition for this bulk workflow transition operation
    private boolean setWorkflowTransitionSelection()
    {
        // Reset the selection as new selection is being made
        getBulkEditBean().resetWorkflowTransitionSelection();
        boolean selectionMade = false;

        Map params = ActionContext.getParameters();
        if (params != null && !params.isEmpty())
        {
            Set keys = params.keySet();

            for (Iterator iterator = keys.iterator(); iterator.hasNext();)
            {
                String key = (String) iterator.next();
                if (key.equals(WORKFLOW_TRANSITION))
                {
                    final String[] actionId = (String[]) params.get(key);
                    String code = actionId[0];
                    WorkflowTransitionKey wtkey = decodeWorkflowTransitionKey(code);
                    getBulkEditBean().setSelectedWFTransitionKey(wtkey);
                    selectionMade = true;
                }
            }
        }
        return selectionMade;
    }

    /**
     * Decodes a string into its WorkflowTransitionKey.
     * @param encoded a string-encoded WorkflowTransitionKey literal.
     * @return the WorkflowTransitionKey that for the given encoded string
     */
    public WorkflowTransitionKey decodeWorkflowTransitionKey(String encoded)
    {
        // last two indexes of _ will be our origin status and actiondescriptor Id.  Anything before that is the
        // workflowname.
        int i = encoded.lastIndexOf('_');
        String destinationStatus = encoded.substring(i+1, encoded.length());
        String rest = encoded.substring(0, i);
        int secondDividerIndex = rest.lastIndexOf('_');
        String actionId = rest.substring(secondDividerIndex+1, rest.length());
        String workflowName = rest.substring(0, secondDividerIndex);

        return new WorkflowTransitionKey(workflowName, actionId, destinationStatus);
    }

    public String encodeWorkflowTransitionKey(WorkflowTransitionKey workflowTransitionKey)
    {
        return workflowTransitionKey.getWorkflowName() + "_" +
               workflowTransitionKey.getActionDescriptorId() + "_" +
               workflowTransitionKey.getDestinationStatus();
    }

    public GenericValue getOriginStatus(WorkflowTransitionKey workflowTransitionKey)
    {
        Collection issueKeys = (Collection) getBulkEditBean().getWorkflowTransitionMap().get(workflowTransitionKey);

        String issueKey = (String) issueKeys.iterator().next();
        Issue issue = issueManager.getIssueObject(issueKey);
        return issue.getStatus();
    }

    public GenericValue getDestinationStatus(WorkflowTransitionKey workflowTransitionKey)
    {
        ActionDescriptor actionDescriptor = getBulkWorkflowTransitionOperation().getActionDescriptor(workflowTransitionKey);
        JiraWorkflow workflow = workflowManager.getWorkflow(workflowTransitionKey.getWorkflowName());
        final int newStepId = actionDescriptor.getUnconditionalResult().getStep();
        if(newStepId == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
        {
            return getOriginStatus(workflowTransitionKey);
        }
        else
        {
            StepDescriptor step = workflow.getDescriptor().getStep(newStepId);
            return workflow.getLinkedStatus(step);
        }
    }

    // Returns a short list of the issue collection
    public List getShortListTransitionIssueKeys(Collection issueKeys)
    {
        int count = 0;
        List shortList = new ArrayList();

        for (Iterator iterator = issueKeys.iterator(); iterator.hasNext();)
        {
            String issueKey = (String) iterator.next();
            shortList.add(issueKey);
            count++;

            if (count >= 5)
                break;
        }

        return shortList;
    }

    // ---- Webwork Getters & Setters ----------------------------------------------------------------------------------
    public String[] getActions()
    {
        Map params = ActionContext.getParameters();

        // Conflicting names in func tests forces the resolution to be added to the actions as follows
        if (params != null && params.containsKey(FORCED_RESOLUTION))
        {
            String[] strings = (String[]) params.get(FORCED_RESOLUTION);
            if (actions == null)
            {
                actions = new String[1];
                actions[0] = strings[0];
                return actions;
            }
            else
            {
                String[] newActions = new String[actions.length + 1];
                for (int i = 0; i < actions.length; i++)
                {
                    newActions[i] = actions[i];
                }
                newActions[actions.length] = strings[0];
                return newActions;
            }
        }

        return actions;
    }

    public void setActions(String[] actions)
    {
        this.actions = actions;
    }

    // Used to force the selection of a resolution if one is detected on a screen.
    // Avoids the scenario where an issue is transitioned to a 'Resolved' status without
    // setting the 'resolution'.
    public boolean isForceResolution(Field field)
    {
        return IssueFieldConstants.RESOLUTION.equals(field.getId());
    }

    public String getCommentaction()
    {
        return commentaction;
    }

    public void setCommentaction(String commentaction)
    {
        this.commentaction = commentaction;
    }

    // ---- Field View Methods -----------------------------------------------------------------------------------------

    public String getFieldViewHtml(OrderableField orderableField)
    {
        // There is a validation that will not allow an edit to occur on a field that has different renderer types
        // defined in the field layout item so if we get here then we know it is safe to grab the first layout
        // item we can find for the field and that this will imply the correct renderer type.
        FieldLayoutItem layoutItem = null;
        if (!getBulkEditBean().getFieldLayouts().isEmpty())
        {
            layoutItem = ((FieldLayout) getBulkEditBean().getFieldLayouts().iterator().next()).getFieldLayoutItem(orderableField);
        }

        // Let the fields know they are being shown as part of the Preview for Bulk Transition. For example, the comment
        // field needs to display the user group its been retsricted to, not just the comment text 

        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("bulkoperation", getBulkEditBean().getOperationName())
                .add("prefix", "new_").toMutableMap();

        return orderableField.getViewHtml(layoutItem, this, (Issue) getBulkEditBean().getSelectedIssues().iterator().next(), getBulkEditBean().getFieldValues().get(orderableField.getId()), displayParams);
    }

    public String getFieldHtml(OrderableField orderableField) throws Exception
    {
        return orderableField.getBulkEditHtml(getBulkEditBean(), this, getBulkEditBean(), EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE));
    }

    // ---- Field Screen Methods ---------------------------------------------------------------------------------------

    public Collection getFieldScreenRenderTabs()
    {
        return getBulkEditBean().getFieldScreenRenderer().getFieldScreenRenderTabs();
    }

    protected FieldScreenRenderLayoutItem buildFieldScreenRenderLayoutItem(final OrderableField field, GenericValue issue)
    {
        return new FieldScreenRenderLayoutItemImpl(null, fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field));
    }

    // ---- Operation Methods ------------------------------------------------------------------------------------------

    public BulkWorkflowTransitionOperation getBulkWorkflowTransitionOperation()
    {
        return bulkWorkflowTransitionOperation;
    }

    private void initTabsWithErrors()
    {
        tabsWithErrors = new TreeSet<FieldScreenRenderTab>();
        selectedTab = new ScreenTabErrorHelper().initialiseTabsWithErrors(tabsWithErrors, getErrors(), getBulkEditBean().getFieldScreenRenderer(), ActionContext.getParameters());
    }

    public Collection getTabsWithErrors()
    {
        if (tabsWithErrors == null)
        {
            initTabsWithErrors();
        }

        return tabsWithErrors;
    }

    public int getSelectedTab()
    {
        // Init tabs - as the first tab with error will be calculated then.
        if (tabsWithErrors == null)
        {
            initTabsWithErrors();
        }

        return selectedTab;
    }

    public String removeSpaces(String string)
    {
        return StringUtils.deleteWhitespace(string);
    }

    protected String redirectToStart()
    {
        return super.redirectToStart("bulkworkflowtransition.session.timeout.message");
    }
}
