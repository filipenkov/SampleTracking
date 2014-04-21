/**
 * @namespace JIRA.ViewIssue
 * A module to encapsulate all view issue functionality
 */
JIRA.ViewIssue = (function () {


    function setFocusConfiguration() {
        // if the url has an anchor the same as the quick subtask create form, we will focus first field.
        if (parseUri(window.location.href).anchor !== "summary") {
            var triggerConfig = new JIRA.setFocus.FocusConfiguration();
            triggerConfig.excludeParentSelector = "#" + FORM_ID + ",.dont-default-focus";
            JIRA.setFocus.pushConfiguration(triggerConfig);
        } else {
            AJS.$("#summary").focus();
        }
    }

    function listenForEvents() {
        var subtaskTrigger;
        JIRA.bind("QuickCreateSubtask.sessionComplete", function (e, issues) {
            JIRA.Issue.getSubtaskModule().addClass("updating");
            JIRA.Issue.refreshSubtasks().done(function () {
                subtaskTrigger = document.getElementById("stqc_show");
                if (subtaskTrigger) {
                    // remove old form
                    subtaskTrigger.onclick = null;
                }
                JIRA.Issue.highlightSubtasks(issues);
                JIRA.Issue.getSubtaskModule().removeClass("updating");
            });
        });
        JIRA.bind("QuickEdit.sessionComplete", function () {
            JIRA.Issue.reload();
        });
    }


    var FORM_ID = "stqcform";

    var subtasks = {
        domReady: function () {
            // If we have not just created a subtask do not focus first field of form
            setFocusConfiguration();
        }
    };


    var stalker = {
        init: function () {
            // offsets perm links, and any anchor's, scroll position so they are offset under ops bar
            new JIRA.OffsetAnchors("#stalker.js-stalker, .stalker-placeholder");
        }
    };

    return {

        /**
         * Called whilst page is loading
         *
         * @method init
         */
        init: function () {
            stalker.init();
        },

        /**
         * Called when DOM is ready. Same as AJS.$(function() {...});
         *
         * @method domReady
         */
        domReady: function () {
            subtasks.domReady();
            listenForEvents();
        }
    };
})();

JIRA.ViewIssue.init();
AJS.$(JIRA.ViewIssue.domReady);

/** Preserve legacy namespace
 @deprecated jira.app.viewissue */
AJS.namespace("jira.app.viewissue", null, JIRA.ViewIssue);

/** todo: BELOW code seriously needs to refactored. Badly! If adding anything to this file, use module structure above. */

jQuery(function (){

    var openInNewWindow = function(e)
    {
        e.preventDefault();
        e.stopPropagation();
        var $this = jQuery(this);

        // close the link
        jQuery(document).click();

        new JIRA.ScreenshotDialog({
            trigger: $this
        }).openWindow();
    };


    AJS.$("#attach-screenshot").click(openInNewWindow);

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

    AJS.$("#toggle-vote-issue").click(function(e) {
        e.preventDefault();
        AJS.$("#vote-toggle").click();
    });

    AJS.$("#toggle-watch-issue").click(function(e) {
        e.preventDefault();
        toggleWatch();
    });

    var addI18nErrorCodes = function(i18n) {
        AJS.$("input[type=hidden][id|=error]").each(function(index, elem) {
            var i18n_id = elem.id.replace("error-", "");
            i18n[i18n_id] = elem.value;
        });
    };

    AJS.$("#vote-toggle").click(function(e) {
        e.preventDefault();
        var i18n = {titleOn:AJS.I18n.getText("issue.operations.simple.voting.alreadyvoted"), titleOff:AJS.I18n.getText("issue.operations.simple.voting.notvoted"),
            textOn:AJS.I18n.getText("issue.operations.simple.unvote"), textOff:AJS.I18n.getText("issue.operations.simple.vote"),
            actionTextOff:AJS.I18n.getText("common.concepts.vote"), actionTextOn:AJS.I18n.getText("common.concepts.voted")};
        addI18nErrorCodes(i18n);
        toggleVotingAndWatching(AJS.$(this), "icon-vote", AJS.$("#vote-data"), AJS.$("#toggle-vote-issue"), i18n);
    });

    AJS.$("#watching-toggle").click(function(e) {
        e.preventDefault();
        var i18n = { titleOn:AJS.I18n.getText("issue.operations.simple.stopwatching"), titleOff:AJS.I18n.getText("issue.operations.simple.startwatching"),
            textOn:AJS.I18n.getText("issue.operations.unwatch"), textOff:AJS.I18n.getText("issue.operations.watch"),
            actionTextOff:AJS.I18n.getText("common.concepts.watch"), actionTextOn:AJS.I18n.getText("common.concepts.watching") };
        addI18nErrorCodes(i18n);
        toggleVotingAndWatching(AJS.$(this), "icon-watch",AJS.$("#watcher-data"), AJS.$("#toggle-watch-issue"), i18n);
    });


    AJS.moveInProgress = false;
    AJS.$(document).bind("moveToStarted", function() {
        AJS.moveInProgress = true;
    }).bind("moveToFinished", function() {
                AJS.moveInProgress = false;
            });

});


