package com.sysbliss.jira.workflow.service {
import com.sysbliss.jira.plugins.workflow.exception.FlexLoginException;
import com.sysbliss.jira.plugins.workflow.exception.FlexNoPermissionException;
import com.sysbliss.jira.plugins.workflow.exception.FlexNotLoggedInException;
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
import com.sysbliss.jira.workflow.ui.dialog.InvalidLicenseDialog;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.ui.dialog.LoginDialog;
import com.sysbliss.jira.workflow.ui.dialog.NoPermissionDialog;
import com.sysbliss.jira.workflow.ui.dialog.ServiceFaultDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

import mx.logging.ILogger;
import mx.logging.Log;
import mx.resources.ResourceManager;
import mx.rpc.events.FaultEvent;

import org.swizframework.Swiz;

public class DefaultFaultHandler {
    private static var log:ILogger = Log.getLogger("com.sysbliss.jira.workflow.service.DefaultFaultHandler");
    private static const _instance:DefaultFaultHandler = new DefaultFaultHandler(SingletonLock);
    private var _faultDialog:ServiceFaultDialog;
    private var _loginDialog:LoginDialog;
    private var _permissionDialog:NoPermissionDialog;
    private var _licenseDialog:InvalidLicenseDialog;

    private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

    public function DefaultFaultHandler(lock:Class) {
        if (lock != SingletonLock) {
            throw new Error(niceResourceManager.getString('json', 'workflow.designer.valid.singleton.access'));
        }
    }

    public static function handleFault(e:FaultEvent, ... args):void {

        if (!_instance._faultDialog) {
            _instance._faultDialog = new ServiceFaultDialog();
        }

        if (!_instance._loginDialog) {
            _instance._loginDialog = new LoginDialog();
        }

        if (!_instance._permissionDialog) {
            _instance._permissionDialog = new NoPermissionDialog();
        }

        if (!_instance._licenseDialog) {
            _instance._licenseDialog = new InvalidLicenseDialog();
        }

        if ((e.fault.rootCause is FlexNotLoggedInException)) {
            MDIDialogUtils.removeAllDialogs();
            MDIDialogUtils.popModalDialog(_instance._loginDialog);
        } else if ((e.fault.rootCause is FlexNoPermissionException)) {
            MDIDialogUtils.removeAllDialogs();
            MDIDialogUtils.popModalDialog(_instance._permissionDialog);

        } else if ((e.fault.rootCause is FlexLoginException)) {
            var pd:JiraProgressDialog = Swiz.getBean("jiraProgressDialog") as JiraProgressDialog;
            if (pd) {
                MDIDialogUtils.removeModalDialog(pd);
            }
            _instance._loginDialog.loginFailure();

        } else {
            MDIDialogUtils.removeAllDialogs();
            MDIDialogUtils.popModalDialog(_instance._faultDialog);
            var msg:String = ResourceManager.getInstance().getString('json', 'workflow.designer.error');
            if (e.fault.rootCause.hasOwnProperty("message")) {
                msg = e.fault.rootCause.message;
            } else {
                msg = e.fault.faultString;
            }
            _instance._faultDialog.info = {message:msg,detail:e.fault.faultDetail};
        }
    }
}
}
class SingletonLock {
}
;
