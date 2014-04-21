package com.sysbliss.jira.workflow.service
{
	
	import flash.utils.describeType;
	
	import mx.rpc.remoting.mxml.RemoteObject;
	
	import org.swizframework.factory.IFactoryBean;

	public class ServiceFactoryBean implements IFactoryBean
	{
		public var config:RemotingConfiguration;
		
		public function ServiceFactoryBean()
		{
//			config = new TestJiraServiceConfiguration();
			config = new JiraServiceConfiguration();
		}

		public function getObject():*
		{
			var ro:RemoteObject = new RemoteObject();
			ro.destination = config.destination;
			ro.endpoint = config.endpoint;
			ro.source = config.destination;
			return ro;
		}
		
		public function getObjectDescription():XML
		{
			return describeType(RemoteObject);
		}
		
		public function getObjectType():String
		{
			var descXML:XML = describeType(RemoteObject);
			return descXML..type.@name;

		}
		
		public function getService():RemotingConfiguration {
			return this.config;
		}
		
	}
}