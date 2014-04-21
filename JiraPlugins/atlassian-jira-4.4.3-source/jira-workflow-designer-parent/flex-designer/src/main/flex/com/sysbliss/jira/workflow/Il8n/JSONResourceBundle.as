/**
 * Created by IntelliJ IDEA.
 * User: rsmart
 * Date: 24/02/11
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.jira.workflow.Il8n {
import com.adobe.serialization.json.JSON;

import mx.core.Application;
    import mx.resources.ResourceBundle;
    import flash.external.ExternalInterface;

    public class JSONResourceBundle extends ResourceBundle {
        private var translationsJSON:Object;

        public function JSONResourceBundle(translations:String,locale:String = null,bundleName:String = null) {
               super(locale,bundleName);
               translationsJSON = JSON.decode(translations,false);
        }

        public override function get content():Object  {
            return translationsJSON;
        }
    }
}
