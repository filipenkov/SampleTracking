
AJS.namespace("workflow.designer.swf");

(function() {
    workflow.designer.swf.totalOffsetTop = function() {
        var offset = AJS.$("#jwdFlex").offset().top - AJS.$(window).scrollTop();
        if(offset < 0) {
            return 0;
        }
        return offset;
    };

    workflow.designer.swf.write = function() {
        var newHeight = jQuery(window).height() - workflow.designer.swf.totalOffsetTop();
        var newWidth = jQuery("#jwd").width();

        var params = { allowscriptaccess: "always", wmode:"transparent" };
        var flashvars = null;

        if (AJS.params.workflowName != null) {
            flashvars = {translations:  AJS.params.translations, workflowName: AJS.params.workflowName, workflowIsDraft: AJS.params.workflowIsDraft};
        } else {
            flashvars = {translations: translations};
        }

        swfobject.embedSWF(AJS.params.swfUrl, "jwdFlex", ""+newWidth, ""+newHeight, "10.0.0",AJS.params.epxpressInstallUrl,flashvars,params);
        jQuery("#jwdFlex").css('zIndex', 1);
    };

    workflow.designer.swf.delayResize = function() {
        window.setTimeout(workflow.designer.swf.resize, 20);
    };

    workflow.designer.swf.resize = function() {
        var newHeight = jQuery(window).height() - workflow.designer.swf.totalOffsetTop();
        var newWidth = jQuery("#jwd").width();
        jQuery("#jwdFlex").width(newWidth);
        jQuery("#jwdFlex").height(newHeight);
    };
})();

AJS.$(function() {
    jQuery(window).resize(workflow.designer.swf.delayResize);

    new JIRA.FormDialog({
        id: "edit-workflow-dialog",
        trigger: "#edit-workflow-trigger"
    });

    new JIRA.FormDialog({
        id: "publish-workflow-dialog",
        trigger: "#publish-workflow-trigger"
    });

    workflow.designer.swf.write();
    
    //scrolltop calculation can be off at the start.
    setTimeout(workflow.designer.swf.resize, 1000);
});
