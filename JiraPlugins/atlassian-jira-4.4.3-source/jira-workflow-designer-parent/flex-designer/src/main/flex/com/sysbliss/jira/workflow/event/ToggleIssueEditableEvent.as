/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 5/16/11
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.jira.workflow.event {
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;

import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;

import flash.events.Event;

public class ToggleIssueEditableEvent extends Event
	{
        [Bindable]
		public var workflow:FlexJiraWorkflow;

		[Bindable]
		public var step:FlexJiraStep

		[Bindable]
		public var issueEditable:Boolean;

        [Bindable]
		public var menuItem:Object;

		public function ToggleIssueEditableEvent(type:String, workflow:FlexJiraWorkflow, step:FlexJiraStep, issueEditable:Boolean, menuItem:Object, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
            this.workflow = workflow;
			this.step = step;
			this.issueEditable = issueEditable;
            this.menuItem = menuItem;
		}

		override public function clone():Event {
			return new ToggleIssueEditableEvent(type, workflow, step, issueEditable, menuItem, bubbles, cancelable);
		}

	}
}
