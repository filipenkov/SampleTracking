package com.sysbliss.jira.workflow.utils
{
	import com.sysbliss.collections.HashMap;
	import com.sysbliss.jira.workflow.diagram.renderer.DefaultJiraNodeRenderer;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStatus;
	
	import flash.utils.describeType;
	
	import mx.collections.ArrayCollection;
	
	public class StatusUtils
	{
		private var _rendererMap:HashMap;
		private var _iconMap:HashMap;
		private var _thumbMap:HashMap;
		private var _allIcons:ArrayCollection;
		
		private var _allStatuses:ArrayCollection;
		
		public function StatusUtils()
		{
			initDefaultStatuses();
		}
		
		public function getIconForStatus(status:FlexJiraStatus):Class {
			var icon:Class;
			if(_iconMap.keyExists(getCleanFilename(status.iconUrl))){
				icon = _iconMap.getValue(getCleanFilename(status.iconUrl)) as Class;
			} else {
				icon = _iconMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return icon;
		}
		
		public function getIconForStatusId(statusId:String):Class {
			var icon:Class;
			var status:FlexJiraStatus = getStatusForId(statusId);
			if(status != null && _iconMap.keyExists(getCleanFilename(status.iconUrl))){
				icon = _iconMap.getValue(getCleanFilename(status.iconUrl)) as Class;
			} else {
				icon = _iconMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return icon;
		}
		
		public function getIconForStatusName(statusName:String):Class {
			var icon:Class;
			if(_iconMap.keyExists(statusName)){
				icon = _iconMap.getValue(statusName) as Class;
			} else {
				icon = _iconMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return icon;
		}
		
		public function getThumbnailForStatus(status:FlexJiraStatus):Class {
			var icon:Class;
			if(_thumbMap.keyExists(getCleanFilename(status.iconUrl))){
				icon = _thumbMap.getValue(getCleanFilename(status.iconUrl)) as Class;
			} else {
				icon = _thumbMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return icon;
		}
		
		public function getThumbnailForStatusId(statusId:String):Class {
			var icon:Class;
			var status:FlexJiraStatus = getStatusForId(statusId);
			if(_thumbMap.keyExists(getCleanFilename(status.iconUrl))){
				icon = _thumbMap.getValue(getCleanFilename(status.iconUrl)) as Class;
			} else {
				icon = _thumbMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return icon;
		}
		
		public function getThumbnailForStatusName(statusName:String):Class {
			var icon:Class;
			if(_thumbMap.keyExists(statusName)){
				icon = _thumbMap.getValue(statusName) as Class;
			} else {
				icon = _thumbMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return icon;
		}
		
		public function getNodeRendererForStatus(status:FlexJiraStatus):Class {
			var renderer:Class;
			if(_rendererMap.keyExists(getCleanFilename(status.iconUrl))){
				renderer = _rendererMap.getValue(getCleanFilename(status.iconUrl)) as Class;
			} else {
				renderer = _rendererMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return renderer;
		}
		
		public function getNodeRendererForStatusId(statusId:String):Class {
			var renderer:Class;
			var status:FlexJiraStatus = getStatusForId(statusId);
			if(status){
				if(_rendererMap.keyExists(getCleanFilename(status.iconUrl))){
					renderer = _rendererMap.getValue(getCleanFilename(status.iconUrl)) as Class;
				} else {
					renderer = _rendererMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
				}
			} else {
				renderer = _rendererMap.getValue(WorkflowConstants.STATUS_GENERIC) as Class;
			}
			return renderer;
		}
		
		private function initDefaultStatuses():void {
			this._rendererMap = new HashMap();
			this._iconMap = new HashMap();
			this._thumbMap = new HashMap();
			
			var name:String;
			var value:String;
			for each(var element:XML in describeType(WorkflowConstants)..constant){
				name = String(element.@name);
				if("STATUS_" == name.substr(0,7)){
					value = WorkflowConstants[name];
					
					switch(value){
						case WorkflowConstants.STATUS_ASSIGNED :
							addStatusToMaps(WorkflowConstants.STATUS_ASSIGNED,WorkflowConstants.STATUSICON_ASSIGNED,WorkflowConstants.STATUSTHUMB_ASSIGNED);
							break;
						case WorkflowConstants.STATUS_CLOSED :
							addStatusToMaps(WorkflowConstants.STATUS_CLOSED,WorkflowConstants.STATUSICON_CLOSED,WorkflowConstants.STATUSTHUMB_CLOSED);
							break;
						case WorkflowConstants.STATUS_DOCUMENT :
							addStatusToMaps(WorkflowConstants.STATUS_DOCUMENT,WorkflowConstants.STATUSICON_DOCUMENT,WorkflowConstants.STATUSTHUMB_DOCUMENT);
							break;
						case WorkflowConstants.STATUS_DOWN :
							addStatusToMaps(WorkflowConstants.STATUS_DOWN,WorkflowConstants.STATUSICON_DOWN,WorkflowConstants.STATUSTHUMB_DOWN);
							break;
						case WorkflowConstants.STATUS_EMAIL :
							addStatusToMaps(WorkflowConstants.STATUS_EMAIL,WorkflowConstants.STATUSICON_EMAIL,WorkflowConstants.STATUSTHUMB_EMAIL);
							break;
						case WorkflowConstants.STATUS_GENERIC :
							addStatusToMaps(WorkflowConstants.STATUS_GENERIC,WorkflowConstants.STATUSICON_GENERIC,WorkflowConstants.STATUSTHUMB_GENERIC);
							break;
						case WorkflowConstants.STATUS_INFORMATION :
							addStatusToMaps(WorkflowConstants.STATUS_INFORMATION,WorkflowConstants.STATUSICON_INFORMATION,WorkflowConstants.STATUSTHUMB_INFORMATION);
							break;
						case WorkflowConstants.STATUS_IN_PROGRESS :
							addStatusToMaps(WorkflowConstants.STATUS_IN_PROGRESS,WorkflowConstants.STATUSICON_IN_PROGRESS,WorkflowConstants.STATUSTHUMB_IN_PROGRESS);
							break;
						case WorkflowConstants.STATUS_INVISIBLE :
							addStatusToMaps(WorkflowConstants.STATUS_INVISIBLE,WorkflowConstants.STATUSICON_INVISIBLE,WorkflowConstants.STATUSTHUMB_INVISIBLE);
							break;
						case WorkflowConstants.STATUS_NEED_INFO :
							addStatusToMaps(WorkflowConstants.STATUS_NEED_INFO,WorkflowConstants.STATUSICON_NEED_INFO,WorkflowConstants.STATUSTHUMB_NEED_INFO);
							break;
						case WorkflowConstants.STATUS_OPEN :
							addStatusToMaps(WorkflowConstants.STATUS_OPEN,WorkflowConstants.STATUSICON_OPEN,WorkflowConstants.STATUSTHUMB_OPEN);
							break;
						case WorkflowConstants.STATUS_REOPENED :
							addStatusToMaps(WorkflowConstants.STATUS_REOPENED,WorkflowConstants.STATUSICON_REOPENED,WorkflowConstants.STATUSTHUMB_REOPENED);
							break;
						case WorkflowConstants.STATUS_RESOLVED :
							addStatusToMaps(WorkflowConstants.STATUS_RESOLVED,WorkflowConstants.STATUSICON_RESOLVED,WorkflowConstants.STATUSTHUMB_RESOLVED);
							break;
						case WorkflowConstants.STATUS_TRASH :
							addStatusToMaps(WorkflowConstants.STATUS_TRASH,WorkflowConstants.STATUSICON_TRASH,WorkflowConstants.STATUSTHUMB_TRASH);
							break;
						case WorkflowConstants.STATUS_UNASSIGNED :
							addStatusToMaps(WorkflowConstants.STATUS_UNASSIGNED,WorkflowConstants.STATUSICON_UNASSIGNED,WorkflowConstants.STATUSTHUMB_UNASSIGNED);
							break;
						case WorkflowConstants.STATUS_UP :
							addStatusToMaps(WorkflowConstants.STATUS_UP,WorkflowConstants.STATUSICON_UP,WorkflowConstants.STATUSTHUMB_UP);
							break;
						case WorkflowConstants.STATUS_VISIBLE :
							addStatusToMaps(WorkflowConstants.STATUS_VISIBLE,WorkflowConstants.STATUSICON_VISIBLE,WorkflowConstants.STATUSTHUMB_VISIBLE);
							break;
					}
				}
			}
		}
		
		public function setAllStatuses(statuses:ArrayCollection):void {
			_allStatuses = statuses;
		}
		
		public function getAllStatuses():ArrayCollection {
			return _allStatuses;
		}
		
		public function getAllIcons():ArrayCollection {
			if(!_allIcons){
				_allIcons = new ArrayCollection();
				var keys:Array = _iconMap.getKeys();
				var i:int;
				var name:String;
				var icon:Class;
				for(i=0;i<keys.length;i++){
					name = keys[i] as String;
					icon = _iconMap.getValue(name) as Class;
					
					var si:StatusIcon = new StatusIcon();
					si.icon = icon;
					si.iconUrl = "/images/icons/" + name + ".gif";
					_allIcons.addItem(si);
				}
			}
			
			return _allIcons;
		}
		
		public function getStatusForId(statusId:String):FlexJiraStatus {
			var status:FlexJiraStatus = null;
			var i:int;
			for(i=0;i<_allStatuses.length;i++){
				status = _allStatuses.getItemAt(i) as FlexJiraStatus;
				if(status.id == statusId){
					break;
				}
			}
			return status;
		}
		
		private function getCleanFilename(iconUrl:String):String {
			var startIndex:int = iconUrl.lastIndexOf("/")+1;
			var endIndex:int = iconUrl.indexOf(".gif");
			var filename:String = iconUrl.substr(startIndex,(endIndex-startIndex));
			
			return filename;
		}
		
		private function addStatusToMaps(statusName:String, icon:Class, thumb:Class, renderer:Class=null):void {
			var myRenderer:Class = renderer;
			if(!myRenderer){
				myRenderer = DefaultJiraNodeRenderer;
			}
			_rendererMap.put(statusName,myRenderer);
			_iconMap.put(statusName,icon);
			_thumbMap.put(statusName,thumb);
		}
	}
}

class SingletonLock{};

