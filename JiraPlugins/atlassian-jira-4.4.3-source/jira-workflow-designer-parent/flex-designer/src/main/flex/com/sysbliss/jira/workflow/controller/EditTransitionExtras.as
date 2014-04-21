package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.ui.dialog.TransitionExtrasDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

import flash.events.Event;

import flash.external.ExternalInterface;

import mx.controls.Alert;
import mx.events.FlexEvent;
import mx.logging.ILogger;
import mx.resources.IResourceManager;

import mx.resources.ResourceManager;

import org.swizframework.controller.AbstractController;

public class EditTransitionExtras extends WorkflowAbstractController
{

    [Autowire]
    public var transisitionExtrasDialog:TransitionExtrasDialog;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    public function EditTransitionExtras()
    {
        super();
    }

    [Mediate(event="${eventTypes.SHOW_ACTION_CONDITIONS}", properties="diagram,edge")]
    public function showConditions(d:Diagram, e:Edge):void
    {
        showEdgeExtrasInBrowser(d, e, "conditions");
    }

    [Mediate(event="${eventTypes.SHOW_ACTION_VALIDATORS}", properties="diagram,edge")]
    public function showValidators(d:Diagram, e:Edge):void
    {
        showEdgeExtrasInBrowser(d, e, "validators");
    }

    [Mediate(event="${eventTypes.SHOW_ACTION_FUNCTIONS}", properties="diagram,edge")]
    public function showFunctions(d:Diagram, e:Edge):void
    {
        showEdgeExtrasInBrowser(d, e, "postfunctions");
    }

    private function showEdgeExtrasInBrowser(d:Diagram, e:Edge, descriptorTab:String):void
    {
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowForDiagram(d);

        var mode:String;
        if (workflow.isDraftWorkflow)
        {
            mode = "draft";
        } else
        {
            mode = "live";
        }

        var wfName:String = workflow.name.replace(new RegExp(" ", "g"), "+");
        var transition:int = e.data.id;
        var step:int = e.startNode.data.id;
        var s:Object;
        if (ExternalInterface.available)
        {
            var wrapperFunction:String = "JWD.showTransitionDialog";
            s = ExternalInterface.call(wrapperFunction, mode, wfName, transition, step, descriptorTab);
        } else
        {
            Alert.show(niceResourceManager.getString('json','workflow.designer.external.interface.unsupported'));
        }
    }

}
}