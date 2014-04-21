package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeField;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.issue.bulkedit.BulkMigrate;
import com.atlassian.jira.web.bean.MultiBulkMoveBean;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class MigrateIssueTypes extends BulkMigrate
{
    // ------------------------------------------------------------------------------------------------------- Constants
    private static final String BUTTON_NAME_NEXT = "nextBtn";
    private static final String BUTTON_NAME_PREVIOUS = "previousBtn";
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies

    // ---------------------------------------------------------------------------------------------------- Constructors

    public MigrateIssueTypes(BulkMoveOperation bulkMoveOperation, FieldManager fieldManager, WorkflowManager workflowManager, ConstantsManager constantsManager, IssueFactory issueFactory, BulkMigrateOperation bulkMigrateOperation, PermissionManager permissionManager, final SearchService searchService)
    {
        super(searchService, bulkMoveOperation, fieldManager, workflowManager, constantsManager, issueFactory, bulkMigrateOperation, permissionManager);
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    public String doStart() throws Exception
    {
        return progressSteps();
    }

    public String doChooseContext() throws Exception
    {
        getBulkEditBean().resetMoveData();
        getBulkMigrateOperation().chooseContextNoValidate(getRootBulkEditBean(), getRemoteUser());

        // Check for null statuses
        Collection invalidIssues = getBulkEditBean().getInvalidIssues();
        if (invalidIssues != null && !invalidIssues.isEmpty())
        {
            for (Iterator iterator = invalidIssues.iterator(); iterator.hasNext();)
            {
                Issue invalidIssue = (Issue) iterator.next();
                addErrorMessage(getText("admin.errors.issuetypes.issue.has.nonexistent.status", "'" + invalidIssue.getKey() + "'", invalidIssue.getString("status")));
            }
            return ERROR;
        }

        progressSteps();
        if (currentStep == 1)
        {
            return "start";
        }
        else
        {
            if (isPreviousClicked())
            {
                getRootBulkEditBean().getRelatedMultiBulkMoveBean().progressToPreviousBulkEditBean();
                return "previous";
            }
            else
            {
                if (!getBulkMigrateOperation().isStatusValid(getRootBulkEditBean()))
                {
                    return "choosestatus";
                }
                else
                {
                    return SUCCESS;
                }
            }
        }
    }


    public String doChooseStatus() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        getBulkMigrateOperation().setStatusFields(getCurrentRootBulkEditBean());
        return progressSteps();
    }

    public String doSetFields() throws Exception
    {
        getBulkMigrateOperation().validatePopulateFields(getRootBulkEditBean(), this, this);

        if (invalidInput())
        {
            return INPUT;
        }

        progressSteps();
        if (currentStep == getTotalSteps())
        {
            return "confirm";
        }
        else
        {
            if (isPreviousClicked())
            {
                if (!getBulkMigrateOperation().isStatusValid(getRootBulkEditBean()))
                {
                    return "choosestatus";
                }
                else
                {
                    return "previous";
                }
            }
            else
            {
                getRootBulkEditBean().getRelatedMultiBulkMoveBean().progressToNextBulkEditBean();
                return SUCCESS;
            }
        }
    }

    public String doPerform() throws Exception
    {
        if (isFinishClicked())
        {
            // Ensure the user can perform the operation
            if (!getBulkMigrateOperation().canPerform(getRootBulkEditBean(), getRemoteUser()))
            {
                addErrorMessage(getText("bulk.edit.cannotperform.error", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
                return INPUT;
            }

            // Run the action that needs to be executed
            MultiBulkMoveBean multiBulkMoveBean = getMultiBulkMoveBean();
            multiBulkMoveBean.getExecutableAction().run();

            try
            {
                getBulkMigrateOperation().perform(getRootBulkEditBean(), getRemoteUser());
            }
            catch (Exception e)
            {
                log.error("Error while performing Bulk Edit operation.", e);
                addErrorMessage(getText("bulk.edit.perform.error"));
                return ERROR;
            }

            return getRedirect(multiBulkMoveBean.getFinalLocation());
        }
        else
        {
            // Finished nto clicked
            return progressSteps();
        }
    }

    // --------------------------------------------------------------------------------------------- Public View Methods
    public IssueContext getCurrentIssueContext()
    {
        return getMultiBulkMoveBean().getCurrentIssueContext();
    }

    public int getTotalSteps()
    {
        return getMultiBulkMoveBean().getIssuesInContext().size() * 2 + 2 + getMultiBulkMoveBean().getNumberOfStatusChangeRequired(bulkMoveOperation);
    }

    private List getAvailableIssueTypeOptions()
    {
        Collection selectedOptions = getMultiBulkMoveBean().getSelectedOptions();
        List selectedOptionsList = new ArrayList();
        for (Iterator iterator = selectedOptions.iterator(); iterator.hasNext();)
        {
            String optionId = (String) iterator.next();
            final IssueConstant constantObject = constantsManager.getIssueTypeObject(optionId);
            selectedOptionsList.add(new IssueConstantOption(constantObject));
        }

        return selectedOptionsList;
    }

    public String getIssueTypeEditHtml()
    {
        IssueTypeField issueTypeField = fieldManager.getIssueTypeField();
        final GenericValue issueType = getBulkEditBean().getTargetIssueTypeGV();

        OperationContext operationContext = new OperationContext()
        {
            public Map getFieldValuesHolder()
            {
                return EasyMap.build(IssueFieldConstants.ISSUE_TYPE, issueType != null ? issueType.getString("id") : "");
            }

            public IssueOperation getIssueOperation()
            {
                return getBulkMigrateOperation();
            }
        };

        List options;
        if (!getBulkEditBean().isSubTaskCollection())
        {
            options = getMultiBulkMoveBean().getRegularOptions();
        }
        else
        {
            options = getMultiBulkMoveBean().getSubTaskOptions();
        }

        return issueTypeField.getEditHtml(operationContext, this, options);
    }

    // ------------------------------------------------------------------------------------------------ Wizardry methods
    protected int currentStep = 1;
    protected String nextBtn;

    protected String previousBtn;
    protected String finishButton;


    protected String progressSteps() throws Exception
    {
        if (isPreviousClicked())
        {
            currentStep--;
        }
        else if (isNextClicked())
        {
            currentStep++;
        }

        if (isPreviousClicked())
        {
            return "previous";
        }
        else if (isNextClicked())
        {
            return SUCCESS;
        }
        else
        {
            return "input";
        }
    }

    protected boolean isPreviousClicked()
    {
        return isButtonClickedByName(BUTTON_NAME_PREVIOUS);
    }

    protected boolean isNextClicked()
    {
        return isButtonClickedByName(BUTTON_NAME_NEXT);
    }

    protected boolean isFinishClicked()
    {
        return isNextClicked() && getCurrentStep() >= getTotalSteps();
    }

    protected boolean isButtonClickedByName(String name)
    {
        return StringUtils.isNotBlank(ParameterUtils.getStringParam(ActionContext.getParameters(), name));
    }

    protected boolean isButtonClicked(String buttonValue, String buttonName)
    {
        if (buttonValue == null)
        {
            return false;
        }
        return (buttonValue.toLowerCase().indexOf(buttonName.toLowerCase()) >= 0);
    }

    // ----------------------------------------------------------------- Simple Accessors & Mutators

    public int getCurrentStep()
    {
        return currentStep;
    }

    public void setCurrentStep(int currentStep)
    {
        this.currentStep = currentStep;
    }

    public String getNextBtn()
    {
        return nextBtn;
    }

    public void setNextBtn(String nextBtn)
    {
        this.nextBtn = nextBtn;
    }

    public String getPreviousBtn()
    {
        return previousBtn;
    }

    public void setPreviousBtn(String previousBtn)
    {
        this.previousBtn = previousBtn;
    }

    public String getFinishButton()
    {
        return finishButton;
    }

    public void setFinishButton(String finishButton)
    {
        this.finishButton = finishButton;
    }
}
