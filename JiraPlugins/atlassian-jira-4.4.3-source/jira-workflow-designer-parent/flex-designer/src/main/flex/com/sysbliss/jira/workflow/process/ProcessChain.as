package com.sysbliss.jira.workflow.process
{
	public interface ProcessChain
	{
		function get successor():ProcessChain;
		function set successor(p:ProcessChain):void;
		function processRequest(request:Request):void;
	}
}