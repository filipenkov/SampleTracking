/**
 * Created by IntelliJ IDEA.
 * User: rshuttleworth
 * Date: 3/03/11
 * Time: 9:58 AM
 * To change this template use File | Settings | File Templates.
 */

package com.sysbliss.jira.workflow.Il8n {

    [Event(name="change",type="flash.events.Event")]
    public interface INiceResourceManager {
        [Bindable("change")]
        function getString(bundleName:String,resourceName:String,parameters:Array = null,locale:String = null):String
    }
}
