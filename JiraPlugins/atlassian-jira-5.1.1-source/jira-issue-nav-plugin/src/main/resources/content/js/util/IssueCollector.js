(function() {
    // JIRADEV-11614, JRADEV-12356 - Sadly, this approach for retrieving issue collector resources won't work cleanly in IE7 or IE8.
    var itWontCauseAnInsecureContentWarning = !(AJS.$.browser.msie && parseInt(AJS.$.browser.version, 10) < 9);
    if (itWontCauseAnInsecureContentWarning) {
        AJS.$.ajax({
            url: "https://jira.atlassian.com/s/en_UKr2b6v9/713/4/1.0.20-beta/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=bc1a3f9b",
            type: "get",
            cache: true,
            dataType: "script",
            timeout:5000
        });
    }

    AJS.$(function() {
        if(AJS.Meta.get("isAtlassianStaff")) {
            function generatePopup(contents, trigger, doShowPopup) {
                $contents = contents;
                if ($contents.children().length) {
                    // Dialog already opened once and not reset - just reuse it
                    doShowPopup();
                    return;
                }

                $contents.html(JIRA.Templates.Issues.Util.exitPopup());
                var $textarea = $contents.find("textarea");
                $textarea.click(function(e) {
                    e.stopPropagation();
                });
                $contents.find("form").submit(function(e) {
                    e.preventDefault();
                    var feedback = AJS.$.trim($textarea.val());
                    if(feedback === "") {
                        alert("Try again. I'm not going to let you leave until you tell me why you want to leave!");
                    } else {
                        AJS.$.ajax({
                            type: "POST",
                            contentType: "application/json",
                            dataType: "json",
                            url: contextPath + "/rest/issueNav/1/optoutfeedback",
                            data: JSON.stringify({note:$textarea.val()})
                        });
                        //follow the link after submitting the feedback.
                        window.location.href = trigger.href;
                    }
                });
                doShowPopup();
            }

            var dialogOptions = {
                width: 273,
                offsetY: 17,
                offsetX: -100,
                hideDelay: 36e5,         // needed for debugging! Sit for an hour.
                useLiveEvents: true
            };

            var exitKickass = AJS.InlineDialog(AJS.$("#exit-beta-navigator a"), "exit-kickass-popup", generatePopup, dialogOptions);
        }
    });
})();