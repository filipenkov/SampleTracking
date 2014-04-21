package com.sysbliss.jira.workflow.controller {
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
import com.sysbliss.jira.workflow.service.JiraWorkflowService;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

import mx.controls.Alert;
import mx.events.CloseEvent;
import mx.rpc.events.ResultEvent;

public class ToggleIssueEditableController extends WorkflowAbstractController {

    [Autowire]
    public var jiraProgressDialog:JiraProgressDialog;

    [Autowire]
    public var jiraService:JiraWorkflowService;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    private var _currentWorkflow:FlexJiraWorkflow;
    private var _currentStep:FlexJiraStep;
    private var _toggleValue:Boolean;
    private var _currentMenuItem:Object;

    public function ToggleIssueEditableController() {
    }

    [Mediate(event="${eventTypes.TOGGLE_ISSUE_EDITABLE}", properties="workflow,step,issueEditable,menuItem")]
    public function confirmIssueEditable(wf:FlexJiraWorkflow, step:FlexJiraStep, issueEditable:Boolean, menuItem:Object):void {
        _currentMenuItem = menuItem;
        _currentStep = step;
        _currentWorkflow = wf;
        _toggleValue = issueEditable;
        var alertTitle:String = niceResourceManager.getString("json", "workflow.designer.title.confirm.toggle.issue.editable");
        var alertMessage:String = niceResourceManager.getString("json", "workflow.designer.confirm.toggle.issue.editable");
        Alert.show(alertMessage, alertTitle, Alert.OK | Alert.CANCEL, null, onAlertClosed, null, Alert.OK);

    }

    private function onAlertClosed(e:CloseEvent):void {
        if (e.detail == Alert.OK) {
            doToggleIssueEditable();
        } else {
            _currentMenuItem.toggled = !_currentMenuItem.toggled;
        }
    }

    private function doToggleIssueEditable():void {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.loading.please.wait");
        executeServiceCall(jiraService.updateIssueEditable(_currentStep, _toggleValue, _currentWorkflow), onIssueUpdated, DefaultFaultHandler.handleFault);
    }

    private function onIssueUpdated(e:ResultEvent):void {
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
        _currentMenuItem = null;
        _currentStep = null;
        _currentWorkflow = null;
        _toggleValue = null;
    }
}
}
