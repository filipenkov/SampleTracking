AJS.$(function() {
    var buildInfo = AJS.Meta.get("version-number");
    var disableCollector = AJS.Meta.get("disable-issue-collector");
    var itWontCauseAnInsecureContentWarning = !(AJS.$.browser.msie && parseInt(AJS.$.browser.version, 10) < 9);
    var showTrigger = function() {
        // JIRADEV-11614, JRADEV-12356 - Sadly, jQuery#ajax will cause IE7 / IE8 to warn about insecure content.
        if (!itWontCauseAnInsecureContentWarning) {
            return false;
        }
        //check if this is the sandbox
        if(AJS.params.baseURL && AJS.params.baseURL.indexOf("sandbox.onjira.com") >= 0) {
            return true;
        }
        //check if we are running a m,beta,rc or SNAPSHOT release
        if(/.*v[0-9](\.[0-9])+\-(rc[0-9]+)|(SNAPSHOT)|(beta[0-9]+)|(m[0-9]+).*/.test(buildInfo)) {
            return true;
        }
        //check if this is an evaluation license
        if(AJS.$(".licensemessagered:contains('JIRA evaluation license')").length > 0) {
            return true;
        }

        return false;
    };

    if(!disableCollector && showTrigger()) {
        //some additional env info!
        window.ATL_JQ_PAGE_PROPS = AJS.$.extend(window.ATL_JQ_PAGE_PROPS, {
            "d3de7fb5": {
                "JIRA Version": buildInfo,
                "JIRA Footer Version": AJS.$("#footer-build-information").text()
            }
        });

        AJS.$.ajax({
            url: "https://jira.atlassian.com/s/en_UK9dpx5o/713/4/1.0.20-beta/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=d3de7fb5",
            type: "get",
            cache: true,
            dataType: "script",
            timeout:5000,
            success: function() {
                var $feedbackLink = AJS.$("<a id=\"jira-feedback-lnk\" class=\"feedback-link jira-icon18-charlie spch-bub-inside\" href=\"#\">" +
                        "<span class=\"point\"></span>" +
                        "<em>" + AJS.I18n.getText("feedback.form.trigger.text") + "</em></a>");

                AJS.$("#header .global .secondary").append($feedbackLink);
            }
        });
    }
});