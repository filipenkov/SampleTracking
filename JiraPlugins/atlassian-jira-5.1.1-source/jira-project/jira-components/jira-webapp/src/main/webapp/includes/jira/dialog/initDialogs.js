
AJS.$(function () {

    JIRA.Dialogs.keyboardShortcuts = new JIRA.FormDialog({
        id: "keyboard-shortcuts-dialog",
        trigger: "#keyshortscuthelp",
        widthClass: "large",
        onContentRefresh: function () {
            var context = this.get$popupContent();
            AJS.$("a.submit-link", context).click(function(e){
                e.preventDefault();
                AJS.$("form", context).submit();
            });
        },
        onSuccessfulSubmit: function() {
            AJS.keyboardShortcutsDisabled = !AJS.keyboardShortcutsDisabled;
        }
    });

    JIRA.Dialogs.deleteIssueLink = new JIRA.FormDialog({
        type: "ajax",
        id: "delete-issue-link-dialog",
        trigger: "#linkingmodule a.icon-delete",
        ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions
    });

    if (document.getElementById("dashboard")) {

        JIRA.Dialogs.deleteDashboard = new JIRA.FormDialog({
            type: "ajax"
        });

        AJS.$(document).delegate("#delete_dashboard", "click", function(e) {
            e.stopPropagation();
            e.preventDefault();

            JIRA.Dialogs.deleteDashboard.$activeTrigger = AJS.$("#delete_dashboard");
            JIRA.Dialogs.deleteDashboard.init({
                type: "ajax",
                id: "delete-dshboard",
                ajaxOptions: {
                    url: JIRA.Dialogs.deleteDashboard.$activeTrigger.attr("href")
                },
                targetUrl: "input[name=targetUrl]"
            });
            JIRA.Dialogs.deleteDashboard.show();
        });

    } else {

        // Issue-related dialogs should not be active on the dashboard.

        JIRA.Dialogs.linkIssue = JIRA.Dialogs.createLinkIssueDialog("a.issueaction-link-issue");

        JIRA.Dialogs.deleteIssue = new JIRA.FormDialog({
            id: "delete-issue-dialog",
            trigger: "a.issueaction-delete-issue",
            targetUrl: "#delete-issue-return-url",
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_deleted',
            delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.cloneIssue = new JIRA.FormDialog({
            id: "clone-issue-dialog",
            trigger: "a.issueaction-clone-issue",
            handleRedirect:true,
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_cloned',
            delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.assignIssue = new JIRA.FormDialog({
            id: "assign-dialog",
            trigger: "a.issueaction-assign-issue",
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_assigned',
            delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.assignIssue = new JIRA.FormDialog({
            id: "assign-dialog",
            trigger: "a.issueaction-assign-to-me",
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_assigned',
            delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.logWork = new JIRA.FormDialog({
            id: "log-work-dialog",
            trigger: "a.issueaction-log-work",
            handleRedirect:true,
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_worklogged',
            delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
                var context = this.get$popupContent();
            }
        });

        JIRA.Dialogs.attachFile = new JIRA.FormDialog({
            id: "attach-file-dialog",
            trigger: "a.issueaction-attach-file",
            handleRedirect: true,
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_attached',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.attachScreenshot = new JIRA.ScreenshotDialog({
            id: "attach-screenshot-window",
            trigger: "a.issueaction-attach-screenshot"
        });

        JIRA.Dialogs.manageAttachment = new JIRA.FormDialog({
            id: 'manage-attachment-dialog',
            trigger: '#manage-attachment-link',
            stacked: true,
            reloadOnPop: true
        });

        JIRA.Dialogs.comment = new JIRA.FormDialog({
            id: "comment-add-dialog",
            trigger: "a.issueaction-comment-issue:not(.inline-comment)",
            handleRedirect: true,
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_commented',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.editLabels = new JIRA.LabelsDialog({
            id: "edit-labels-dialog",
            trigger: "a.issueaction-edit-labels,a.edit-labels",
            autoClose: true,
            ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
            onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_labelled',
            delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
            labelsProvider: labelsProvider,
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        new JIRA.FormDialog({
            type: "ajax",
            id: "delete-attachment-dialog",
            trigger: "#attachmentmodule .attachment-delete a, #issue-attachments-table a.delete"
        });

        JIRA.Dialogs.issueActions = new JIRA.IssueActionsDialog;

        /** Preserve legacy namespace
            @deprecated jira.app.issueActionsPopup */
        AJS.namespace("jira.app.issueActionsPopup", null, JIRA.Dialogs.issueActions);

        // Workflow transition dialogs
        AJS.$(document).delegate("a.issueaction-workflow-transition", "click", function(event) {
            event.preventDefault();
            var action = parseUri(AJS.$(event.target).attr('href')).queryKey.action;
            if (action) {
                var id = "workflow-transition-" + action + "-dialog";
                var $trigger = AJS.$(this);
                if (!JIRA.Dialogs[id]) {
                    // we don't pass "url" below as it would break JIRA.Dialogs.getDefaultAjaxOptions which has to
                    // get URL dynamically from triggering DOM element (<a>)
                    JIRA.Dialogs[id] = new JIRA.FormDialog({
                        id: id,
                        trigger: 'a[href*="action=' + action + '"].issueaction-workflow-transition',
                        widthClass: "large",
                        handleRedirect: true,
                        ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
                        onSuccessfulSubmit : JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit,
                        delayShowUntil: JIRA.Dialogs.waitForSavesToComplete,
                        issueMsg : 'thanks_issue_transitioned',
                        onContentRefresh: function () {
                            // initialise AJS tabs for the workflow dialogs
                            AJS.tabs.setup();
                        }
                    });
                    JIRA.Dialogs[id].$activeTrigger = $trigger; // that's necessary for the first run only
                    // as later on AJS will set it when triggered automatically
                    JIRA.Dialogs[id].show();
                }
            }
        });

        // Dialogs should only show up if there's an issue to work on!
        AJS.$.each(JIRA.Dialogs, function (name, dialog) {
            if (dialog instanceof JIRA.Dialog) {
                AJS.$(dialog).bind("beforeShow", function () {
                    // For all issue-related dialogs, check if we have a valid issue to work on!
                    if (name !== "keyboardShortcuts") {
                        return JIRA.IssueNavigator.isRowSelected() || JIRA.Issue.getIssueId() !== undefined;
                    }
                });
            }
        });
    }

    //Dont' assign to JIRA.Dialogs because it not an issue related dialog.
    new JIRA.FormDialog({
        type: "ajax",
        id: "create-project-dialog",
        trigger: ".add-project-trigger",
        autoClose: true,
        widthClass: AJS.DarkFeatures.isEnabled("addproject.project.sample") ? "large" : "medium"
    });


    new JIRA.FormDialog({
        id: "credits-dialog",
        trigger: "#view_credits",
        widthClass: "creditsContainer",
        onContentRefresh: function () {
            if (!jQuery("html").hasClass("safari")){
                function center () {
                    jQuery.each(arguments, function () {
                        this.show()
                                .css({
                                    marginLeft: -this.outerWidth() / 2,
                                    marginTop: -this.outerHeight() / 2
                                })
                                .hide();
                    });
                }
            }
        }
    });


    jQuery("a.trigger-dialog").each(function () {
        new JIRA.FormDialog({
            trigger: this,
            id: this.id + "-dialog",
            ajaxOptions: {
                url: this.href,
                data: {
                    decorator: "dialog",
                    inline: "true"
                }
            }
        });
    });

    function labelsProvider(labelsPopup) {
        var $trigger = labelsPopup.$activeTrigger,
            $labelsContainer = $trigger.closest(".labels-wrap"),
            isSubtaskForm = $trigger.parents("#view-subtasks").length !== 0;

        if(isSubtaskForm) {
            //if we clicked the subtask form, only look within the current row for a labels wrap!
            $labelsContainer = $trigger.parents("tr").find(".labels-wrap");
        } else if ($trigger.hasClass("issueaction-edit-labels")) {
            // we clicked the issueaction which should only update the system field labels!
            if (JIRA.IssueNavigator.isNavigator()) {
                $labelsContainer = jQuery("#issuetable tr.issuerow.focused td.labels .labels-wrap");
            } else {
                $labelsContainer = jQuery("#wrap-labels .labels-wrap");
            }
        }

        if($labelsContainer.length > 0) {
            return $labelsContainer;
        }
        return false;
    }
});
