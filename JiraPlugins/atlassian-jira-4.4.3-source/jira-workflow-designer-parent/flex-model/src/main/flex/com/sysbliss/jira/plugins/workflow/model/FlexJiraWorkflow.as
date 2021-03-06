/**
 * Generated by Gas3 v2.0.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package com.sysbliss.jira.plugins.workflow.model {
import mx.collections.ArrayCollection;

    public interface FlexJiraWorkflow extends FlexJiraWorkflowBase {
        function get uid():String;
        function set uid(s:String):void;

        function getStep(id:int):FlexJiraStep;

        function getStepForName(name:String):FlexJiraStep;

        function getAction(id:int):FlexJiraAction;

        function isInitialAction(id:int):Boolean;

        function getAllStepNames():ArrayCollection;

        function isGlobalAction(action:FlexJiraAction):Boolean;
    }
}