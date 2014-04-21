package com.sysbliss.jira.workflow.process
{
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
import com.sysbliss.util.AbstractClassEnforcer;
	
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	
	public class AbstractProcessor implements ProcessChain
	{
		protected var _successor:ProcessChain;
		protected var resourceManager:IResourceManager;

        protected const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

		public function AbstractProcessor()
		{
			AbstractClassEnforcer.enforceConstructor(this,AbstractProcessor);
			this.resourceManager = ResourceManager.getInstance();
		}

		public function get successor():ProcessChain
		{
			return this._successor;
		}
		
		public function set successor(p:ProcessChain):void
		{
			this._successor = p;
		}
		
		public function processRequest(request:Request):void
		{
			AbstractClassEnforcer.enforceMethod("processRequest");
		}
		
	}
}