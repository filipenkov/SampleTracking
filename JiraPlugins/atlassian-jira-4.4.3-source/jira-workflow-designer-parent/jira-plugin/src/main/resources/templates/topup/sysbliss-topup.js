if (typeof JWD == "undefined")
{
    var JWD = new Object();
}

TopUp.images_path = contextPath + "/download/resources/com.atlassian.jira.plugins.jira-workflow-designer:topupImages/images/top_up/";

JWD.showTransitionDialog = function(workflowMode, workflowName, workflowTransition, workflowStep, tabId) {
    var myUrl = contextPath + "/secure/admin/workflows/ViewWorkflowTransition.jspa?workflowMode=" + encodeURI(workflowMode) + "&workflowName=" + encodeURI(workflowName) + "&workflowTransition=" + encodeURI(workflowTransition) + "&descriptorTab=" + encodeURI(tabId);
    if (workflowTransition != 1) {
        myUrl = myUrl + "&workflowStep=" + encodeURI(workflowStep);
    }
    var decorator = "&decorator=inline&wfDesigner=true";

    myUrl = myUrl + decorator;

    jQuery("#topupLink").attr("href",myUrl);

    var dialogHeight = 400, dialogWidth = 600,
            yPosition = (jQuery(window).height() - dialogHeight) / 2 + jQuery(window).scrollTop(),
            xPosition = (jQuery(window).width() - dialogWidth) / 2 + jQuery(window).scrollLeft();

    TopUp.display (myUrl, {
        title:AJS.params.editTransition,
        layout:"dashboard",
        effect:"appear",
        type:"iframe",
        resizable:0,
        shaded:1,
        overlayClose:1,
        modal:1,
        width:dialogWidth,
        height:dialogHeight,
        x:xPosition,
        y:yPosition
    });


};