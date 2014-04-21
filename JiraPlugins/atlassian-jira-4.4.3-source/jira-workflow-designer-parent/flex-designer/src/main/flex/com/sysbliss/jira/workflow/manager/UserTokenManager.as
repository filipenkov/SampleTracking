package com.sysbliss.jira.workflow.manager
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	
	import org.swizframework.Swiz;
	
	public class UserTokenManager
	{
		public var token:String;
		
		public function UserTokenManager()
		{
		}
		
		[Mediate(event="${eventTypes.USER_SESSION_RETRIEVED}", properties="data")]
		public function updateUserToken(newToken:String):void {
			this.token = newToken;
			Swiz.dispatch(EventTypes.USER_TOKEN_AVAILABLE);
		}

	}
}