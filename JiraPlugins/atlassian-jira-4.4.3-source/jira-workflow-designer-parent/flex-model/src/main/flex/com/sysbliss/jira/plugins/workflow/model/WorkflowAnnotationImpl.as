/**
 * Generated by Gas3 v2.1.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package com.sysbliss.jira.plugins.workflow.model {
import mx.utils.UIDUtil;

[Bindable]
    [RemoteClass(alias="com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotationImpl")]
    public class WorkflowAnnotationImpl extends WorkflowAnnotationImplBase implements WorkflowAnnotation {

        public function WorkflowAnnotationImpl() {
            this._id = UIDUtil.createUID();
            this._description = "";
        }
    }
}