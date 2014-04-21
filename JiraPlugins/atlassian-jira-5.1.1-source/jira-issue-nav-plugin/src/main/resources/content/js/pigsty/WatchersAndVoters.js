AJS.$(function() {

    /**
     * Copied from ViewIssue.js to get voting and watching links working.
     * Listeners are slightly modified from ViewIssue.js to delegate instead of assuming that elements exist on page load
     */

    var toggleVotingAndWatching = function(trigger, className, resultContainer, issueOpTrigger, i18n) {
        var classNameOn = className + "-on",
            classNameOff = className + "-off",
            icon = trigger.find('.icon'),
            restPath = "/voters",
            data,
            method = "POST";

        if(icon.hasClass(classNameOn)) {
            method = "DELETE";
        }

        if(className.indexOf("watch") !== -1) {
            restPath = "/watchers";
        }
        icon.removeClass(classNameOn).removeClass(classNameOff);

        if (method === "POST") {
            // If we are a post we want to include dummy data to prevent JRA-20675 BUT we cannot have data for DELETE
            // otherwise we introduce JRA-23257
            data = {
                dummy: true
            }
        }

        AJS.$(JIRA.SmartAjax.makeRequest({
            url:contextPath + "/rest/api/1.0/issues/" + trigger.attr("rel") + restPath,
            type: method,
            dataType: "json",
            data: data,
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    if(method === "POST") {
                        icon.addClass(classNameOn);
                        trigger.attr("title", i18n.titleOn).find('.action-text').text(i18n.actionTextOn);
                        issueOpTrigger.attr("title", i18n.titleOn).text(i18n.textOn);
                    } else {
                        icon.addClass(classNameOff);
                        trigger.attr("title", i18n.titleOff).find('.action-text').text(i18n.actionTextOff);
                        issueOpTrigger.attr("title", i18n.titleOff).text(i18n.textOff);
                    }

                    resultContainer.text(smartAjaxResult.data.count);
                } else {
                    /* [alert] */
                    alert(JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult,{ alert : true }));
                    /* [alert] end */
                    if(method === "POST") {
                        icon.addClass(classNameOff);
                        trigger.attr("title", i18n.titleOff).find('.action-text').text(i18n.actionTextOff);
                        issueOpTrigger.attr("title", i18n.titleOff).text(i18n.textOff);
                    } else {
                        icon.addClass(classNameOn);
                        trigger.attr("title", i18n.titleOn).find('.action-text').text(i18n.actionTextOn);
                        issueOpTrigger.attr("title", i18n.titleOn).text(i18n.textOn);
                    }
                }
            }
        })).throbber({target: icon});
    };

    function toggleWatch() {
        AJS.$("#watching-toggle").click();
    }

    AJS.$(document).delegate("#toggle-vote-issue", "click", function(e) {
        e.preventDefault();
        AJS.$("#vote-toggle").click();
    });

    AJS.$(document).delegate("#toggle-watch-issue", "click", function(e) {
        e.preventDefault();
        toggleWatch();
    });

    var addI18nErrorCodes = function(i18n) {
        AJS.$("input[type=hidden][id|=error]").each(function(index, elem) {
            var i18n_id = elem.id.replace("error-", "");
            i18n[i18n_id] = elem.value;
        });
    };

    AJS.$(document).delegate("#vote-toggle", "click", function(e) {
        e.preventDefault();
        var i18n = {titleOn:AJS.I18n.getText("issue.operations.simple.voting.alreadyvoted"), titleOff:AJS.I18n.getText("issue.operations.simple.voting.notvoted"),
            textOn:AJS.I18n.getText("issue.operations.simple.unvote"), textOff:AJS.I18n.getText("issue.operations.simple.vote"),
            actionTextOff:AJS.I18n.getText("common.concepts.vote"), actionTextOn:AJS.I18n.getText("common.concepts.voted")};
        addI18nErrorCodes(i18n);
        toggleVotingAndWatching(AJS.$(this), "icon-vote", AJS.$("#vote-data"), AJS.$("#toggle-vote-issue"), i18n);
    });

    AJS.$(document).delegate("#watching-toggle", "click", function(e) {
        e.preventDefault();
        var i18n = { titleOn:AJS.I18n.getText("issue.operations.simple.stopwatching"), titleOff:AJS.I18n.getText("issue.operations.simple.startwatching"),
            textOn:AJS.I18n.getText("issue.operations.unwatch"), textOff:AJS.I18n.getText("issue.operations.watch"),
            actionTextOff:AJS.I18n.getText("common.concepts.watch"), actionTextOn:AJS.I18n.getText("common.concepts.watching") };
        addI18nErrorCodes(i18n);
        toggleVotingAndWatching(AJS.$(this), "icon-watch",AJS.$("#watcher-data"), AJS.$("#toggle-watch-issue"), i18n);
    });
});
