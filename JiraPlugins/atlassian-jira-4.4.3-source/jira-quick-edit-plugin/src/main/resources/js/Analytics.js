// google analytics tracking code - provided by google

var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-20272869-3']);
_gaq.push(['_setDomainName', "none"]);
_gaq.push(['_setAllowLinker', true]);
_gaq.push(['_trackPageview']);

(function() {
    var ga = document.createElement('script');
    ga.type = 'text/javascript';
    ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(ga, s);
})();

// events we are going to send to google analytics
(function () {

    var quickEditStartTime, fullEditStartTime;

    jQuery(AJS).bind("QuickEdit.dialogShown", function (e) {
        console.log("dialogShown");
        var issueKey = JIRA.Issue.getIssueKey(), projectKey = "";
        if(issueKey !== undefined) {
            projectKey = issueKey.substring(0, issueKey.indexOf("-"));
        }
        _gaq.push(["_trackEvent", "QuickEditIssue", "openQuickEditDialog", projectKey]);
    });

    jQuery(AJS).bind("QuickEdit.configurableFormRendered", function (e) {
        console.log("configurableFormRendered");
        fullEditStartTime = undefined;
        quickEditStartTime = new Date().getTime();
    });

    jQuery(AJS).bind("QuickEdit.unconfigurableFormRendered", function (e) {
        console.log("unconfigurableFormRendered");
        quickEditStartTime = undefined;
        fullEditStartTime = new Date().getTime();
    });

    jQuery(AJS).bind("QuickEdit.configurableFormSubmitted", function () {
            var duration = Math.round((new Date().getTime() - quickEditStartTime) / 1000);
            console.log("configurableFormSubmitted:" + duration);
            _gaq.push("_trackEvent", "QuickEditIssue", "quickEditDuration", "duration", duration);
    });

    jQuery(AJS).bind("QuickEdit.unconfigurableFormSubmitted", function () {
        var duration = Math.round((new Date().getTime() - fullEditStartTime) / 1000);
        console.log("unconfigurableFormSubmitted:" + duration);
        _gaq.push("_trackEvent", "QuickEditIssue", "fullEditDuration", "duration", Math.round((new Date().getTime() - fullEditStartTime) / 1000));
    });

    jQuery(AJS).bind("QuickEdit.fieldAdded", function (e, field) {
        console.log("fieldAdded: " + field.getId())
        _gaq.push(["_trackEvent", "QuickEditIssue", "addField", field.getId()]);
    });

    jQuery(AJS).bind("QuickEdit.fieldRemoved", function (e, field) {
        console.log("fieldRemoved: " + field.getId());
        _gaq.push(["_trackEvent", "QuickEditIssue", "removeField", field.getId()]);
    });

    jQuery(AJS).bind("QuickEdit.fieldReordered", function (e, field) {
        console.log("fieldReordered: " + field.getId());
        _gaq.push(["_trackEvent", "QuickEditIssue", "reorderField", field.getId()]);
    });

    jQuery(AJS).bind("QuickEdit.switchedToUnconfigurableForm", function (e) {
        console.log("switchedToUnconfigurableForm");
        _gaq.push(["_trackEvent", "QuickEditIssue", "switchToFullForm"]);
    });

    jQuery(AJS).bind("QuickEdit.switchedToConfigurableForm", function (e) {
        console.log("switchedToConfigurableForm");
        _gaq.push(["_trackEvent", "QuickEditIssue", "switchToQuickEditForm"]);
    });

    jQuery(AJS).bind("QuickEdit.dialogHidden", function (e) {
        console.log("dialogHidden");
        _gaq.push(["_trackEvent", "QuickEditIssue", "dialogClosed"]);
    });
})();