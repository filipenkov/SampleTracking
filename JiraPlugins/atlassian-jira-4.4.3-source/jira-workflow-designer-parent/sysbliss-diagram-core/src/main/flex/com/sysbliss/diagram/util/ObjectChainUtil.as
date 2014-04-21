/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 5/4/11
 * Time: 12:01 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.util {
import mx.utils.object_proxy;

public class ObjectChainUtil {
    public function ObjectChainUtil() {
    }

    public static function ancestorsContainType(obj:Object, type:Class):Boolean {
        if(obj is type) {
            return true;
        } else if (obj.parent == null || obj.parent == undefined) {
            return false;
        } else {
            return ancestorsContainType(obj.parent, type);
        }

    }

    public static function ancestorsContainInstance(obj:Object, instance:Object):Boolean {
        if(obj == instance) {
            return true;
        } else if (obj.parent == null || obj.parent == undefined) {
            return false;
        } else {
            return ancestorsContainInstance(obj.parent, instance);
        }

    }
}
}
