/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 6/10/11
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.data {
public interface Annotation {
    function get id():String;

    function get data():Object;

    function set data(value:Object):void;
}
}
