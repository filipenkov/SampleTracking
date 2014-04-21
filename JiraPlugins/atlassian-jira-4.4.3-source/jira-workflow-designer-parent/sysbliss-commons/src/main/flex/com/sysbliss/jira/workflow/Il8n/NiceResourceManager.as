/**
 * Created by IntelliJ IDEA.
 * User: rshuttleworth
 * Date: 1/03/11
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.jira.workflow.Il8n {
import flash.events.Event;
import flash.events.EventDispatcher;

import mx.resources.IResourceManager;
import mx.resources.ResourceManager;

[Event(name="change",type="flash.events.Event")]
public class NiceResourceManager extends flash.events.EventDispatcher implements INiceResourceManager {

    private static var ONLY_INSTANCE:INiceResourceManager = null;

    public static function getInstance():INiceResourceManager {
        if (ONLY_INSTANCE == null) {
            ONLY_INSTANCE = new NiceResourceManager();
        }

        return ONLY_INSTANCE;
    }

    private var resourceManager:IResourceManager;

    function NiceResourceManager () {
        resourceManager = ResourceManager.getInstance();
        resourceManager.addEventListener("change", resourceManagerListener);
    }

    public function resourceManagerListener(e:Event):void {
        dispatchEvent(e);
    }

    [Bindable("change")]
    public function getString(bundleName:String,resourceName:String,parameters:Array = null,locale:String = null):String
    {
        var value:String = resourceManager.getString(bundleName, resourceName, parameters, locale);

        if (value == null) {
            value = resourceName;
        }

        return value;
    }
}
}
