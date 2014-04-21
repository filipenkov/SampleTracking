package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraFieldScreen;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraFieldScreenImpl;

	import mx.collections.ArrayCollection;
	import mx.logging.ILogger;
	
	import org.swizframework.controller.AbstractController;

	public class JiraFieldScreenController extends WorkflowAbstractController
	{
		private var _listProvider:ArrayCollection;
		
		public function JiraFieldScreenController()
		{
			super();
			this._listProvider = new ArrayCollection();
		}
		
		[Mediate(event="${eventTypes.JIRA_FIELD_SCREENS_RETRIEVED}", properties="screens")]
		public function setFieldScreens(screens:ArrayCollection):void {
			var dp:ArrayCollection = new ArrayCollection();
			var blankField:FlexJiraFieldScreen = new FlexJiraFieldScreenImpl();
			blankField.name = niceResourceManager.getString('json','workflow.designer.no.view.for.transition');
			blankField.id = "";
			dp.addItem(blankField);
			for each(var screen:FlexJiraFieldScreen in screens){
				dp.addItem(screen);
			}
			
			//log.debug("dp length = " + dp.length);
			this.listProvider = dp;
		}
		
		[Bindable]
		public function get listProvider():ArrayCollection {
			return _listProvider;
		}
		
		public function set listProvider(dp:ArrayCollection):void {
			_listProvider = dp;
		}
	}
}