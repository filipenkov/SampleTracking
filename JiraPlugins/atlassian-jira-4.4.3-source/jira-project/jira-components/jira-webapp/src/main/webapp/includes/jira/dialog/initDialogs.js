/**
 * @namespace JIRA.Dialogs
 */
JIRA.Dialogs = {};

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
        }
    });

    JIRA.Dialogs.deleteIssueLink = new JIRA.FormDialog({
        type: "ajax",
        id: "delete-issue-link-dialog",
        trigger: "#linkingmodule a.icon-delete",
        ajaxOptions: getAjaxOptions
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

        JIRA.Dialogs.linkIssue = new JIRA.FormDialog({
            id: "link-issue-dialog",
            trigger: "a.issueaction-link-issue",
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_linked',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.deleteIssue = new JIRA.FormDialog({
            id: "delete-issue-dialog",
            trigger: "a.issueaction-delete-issue",
            targetUrl: "#delete-issue-return-url",
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_deleted',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.cloneIssue = new JIRA.FormDialog({
            id: "clone-issue-dialog",
            trigger: "a.issueaction-clone-issue",
            handleRedirect:true,
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_cloned',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.assignIssue = new JIRA.FormDialog({
            id: "assign-dialog",
            trigger: "a.issueaction-assign-issue",
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_assigned',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
                var context = this.get$popupContent();
                JIRA.Issue.wireAssignToMeLink(context);
            }
        });

        JIRA.Dialogs.logWork = new JIRA.FormDialog({
            id: "log-work-dialog",
            trigger: "a.issueaction-log-work",
            handleRedirect:true,
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_worklogged',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
                var context = this.get$popupContent();
                applyLogworkControls(context);
            }
        });

        JIRA.Dialogs.attachFile = new JIRA.FormDialog({
            id: "attach-file-dialog",
            trigger: "a.issueaction-attach-file",
            handleRedirect: true,
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_attached',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.attachScreenshot = new JIRA.ScreenshotDialog({
            id: "attach-screenshot-window",
            trigger: "a.issueaction-attach-screenshot"
        });

        JIRA.Dialogs.comment = new JIRA.FormDialog({
            id: "comment-add-dialog",
            trigger: ":not(.ops) > * > a.issueaction-comment-issue",
            handleRedirect: true,
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_commented',
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.editLabels = new JIRA.LabelsDialog({
            id: "edit-labels-dialog",
            trigger: "a.issueaction-edit-labels,a.edit-labels",
            autoClose: true,
            ajaxOptions: getAjaxOptions,
            onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
            issueMsg : 'thanks_issue_labelled',
            labelsProvider: labelsProvider,
            onContentRefresh: function () {
                jQuery(".overflow-ellipsis").textOverflow();
            }
        });

        JIRA.Dialogs.issueActions = new JIRA.IssueActionsDialog;

        /** Preserve legacy namespace
            @deprecated jira.app.issueActionsPopup */
        AJS.namespace("jira.app.issueActionsPopup", null, JIRA.Dialogs.issueActions);

        // Workflow transition dialogs
        AJS.$(document).delegate("a.issueaction-workflow-transition", "click", function(event) {
            event.preventDefault();
            var action = /action=(\d+)/.exec(this.href.slice(this.href.indexOf("?")));
            if (action) {
                var id = "workflow-transition-" + action[1] + "-dialog";
                var $trigger = AJS.$(this);
                if (!JIRA.Dialogs[id]) {
                    JIRA.Dialogs[id] = new JIRA.FormDialog({
                        id: id,
                        url: $trigger.attr("href"),
                        trigger: 'a[href*="' + action[0] + '"].issueaction-workflow-transition',
                        widthClass: "large",
                        handleRedirect: true,
                        ajaxOptions: getAjaxOptions,
                        onSuccessfulSubmit : storeCurrentIssueIdOnSucessfulSubmit,
                        issueMsg : 'thanks_issue_transitioned',
                        onContentRefresh: function () {
                            var context = this.get$popupContent();
                            // initialise AJS tabs for the workflow dialogs
                            AJS.tabs.setup();
                            JIRA.Issue.wireAssignToMeLink(context);
                            applyLogworkControls(context);
                        }
                    });
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
        autoClose: true
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
                jQuery(function () {
                    var $intro = jQuery("#intro");
                    var $credits = jQuery("#credits");
                    var $jiralogo = jQuery("#jiraLogo");

                    center($intro, $credits, $jiralogo);

                    window.setTimeout(function() {
                        $intro.fadeIn("slow", function () {
                            window.setTimeout(function () {
                                $intro.fadeOut("slow", function () {
                                    window.setTimeout(function () {
                                        $jiralogo.show();
                                        window.setTimeout(function () {
                                            $jiralogo.animate({
                                                top:30,
                                                marginTop: 10,
                                                width: 0,
                                                height: 0,
                                                marginLeft: 0
                                            }, 2000);
                                            window.setTimeout(function () {
                                                $credits.show().css({
                                                    top: "auto",
                                                    bottom: -$credits.outerHeight()
                                                })
                                                .animate({
                                                    bottom: $credits.outerHeight()
                                                 }, 50000)
                                            }, 1000);
                                        })
                                    }, 500);
                                });
                            }, 2000);
                        });
                    }, 500);
                })
            }
        }
    });

    function getAjaxOptions () {
        var $focusRow = JIRA.IssueNavigator.get$focusedRow();
        var linkIssueURI = this.options.url || this.$activeTrigger.attr("href");

        if (/id=\{0\}/.test(linkIssueURI)) {
            if (!$focusRow.length) {
                return false;
            } else {
                linkIssueURI = linkIssueURI.replace(/(id=\{0\})/, "id=" + $focusRow.attr("rel"));
            }
        }

        if (JIRA.IssueNavigator.isNavigator()) {
            var result = /[?&]id=([0-9]+)/.exec(linkIssueURI);
            this.issueId = result && result.length == 2 ? result[1] : null;
            if(this.issueId !== $focusRow.attr("rel")) {
                //if the issue id doesn't match the focused row's issue id then reassign focus and get the
                //issuekey from the newly focused row! This can happen when clicking the pencil for the
                //labels picker.
                JIRA.IssueNavigator.Shortcuts.focusRow(this.issueId);
                $focusRow = JIRA.IssueNavigator.get$focusedRow();
            }
            this.issueKey = JIRA.IssueNavigator.getSelectedIssueKey();
        }

        return {
            data: {decorator: "dialog", inline: "true"},
            url: linkIssueURI
        };
    }

    /**
     * Stores the current issue id into session storage if the dialogs submits successfully
     */
    function storeCurrentIssueIdOnSucessfulSubmit() {

        if (JIRA.IssueNavigator.isNavigator()) {
            var issueId = this.issueId;
            var issueKey = this.issueKey;
            if (! issueId) {
                issueId = JIRA.IssueNavigator.getSelectedIssueId();
                issueKey = JIRA.IssueNavigator.getSelectedIssueKey();
            }
            if (issueId)
            {
                var sessionStorge = JIRA.SessionStorage;
                sessionStorge.setItem('selectedIssueId', issueId);
                sessionStorge.setItem('selectedIssueKey', issueKey);
                sessionStorge.setItem('selectedIssueMsg', this.options.issueMsg);
            }
        }
        this.issueId = null;
        this.issueKey = null;
    }

    function applyLogworkControls(context){
        jQuery('#log-work-adjust-estimate-new-value, #log-work-adjust-estimate-manual-value', context).attr('disabled','disabled');
        jQuery('#log-work-adjust-estimate-'+jQuery('input[name=worklog_adjustEstimate]:checked,input[name=adjustEstimate]:checked', context).val()+'-value', context).removeAttr('disabled');
        jQuery('input[name=worklog_adjustEstimate],input[name=adjustEstimate]', context).change(function(){
            jQuery('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value', context).attr('disabled','disabled');
            jQuery('#log-work-adjust-estimate-'+jQuery(this).val()+'-value', context).removeAttr('disabled');
        });

        AJS.$(function () {
            AJS.$(context).find("#log-work-activate").change(function() {
                AJS.$(context).find("#worklog-logworkcontainer").toggleClass("hidden");
                if (AJS.$(context).find("#worklog-timetrackingcontainer").size() > 0) {
                    AJS.$(context).find("#worklog-timetrackingcontainer").toggleClass("hidden");
                }
            });
        });
    }

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