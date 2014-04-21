/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 6/10/11
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.data {
public class DefaultAnnotation implements Annotation {

    private var _id:String;
    private var _data:Object;

    public function DefaultAnnotation(id:String) {
        this._id = id;
    }


    public function get id():String {
        return _id;
    }

    public function set id(value:String):void {
        _id = value;
    }

    public function get data():Object {
        return _data;
    }

    public function set data(value:Object):void {
        _data = value;
    }
}
}
