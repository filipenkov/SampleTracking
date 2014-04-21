/**
 * Creates a link issue dialog
 *
 * @param {String, jQuery} trigger - jQuery selector for the element/s that trigger dialog to open
 */
JIRA.Dialogs.createLinkIssueDialog = function () {

    /**
     * Adds isSelected property to linkType that should currently be selected
     *
     * @param {String} activeId
     * @param {Array} linkTypes
     * @return {Array}
     */
    function markSelectedLinkType(activeId, linkTypes) {

        var hasSelected = false;

        // If active link id equals link type set it as selected
        jQuery.each(linkTypes, function (i, linkType) {
            if (linkType.id === activeId) {
                linkType.isSelected = true;
                hasSelected = true;
                return false;
            }
        });

        // Otherwise assume it is the first one.
        if (!hasSelected) {
            linkTypes[0].isSelected = true;
        }

        return linkTypes;
    }

    /**
     * Goes to the server to get array of remote issue link types.
     *
     * @return jQuery.Promise<Array>
     */
    function getRemoteIssueLinks (issueId) {
        var deferred = jQuery.Deferred();
        JIRA.SmartAjax.makeRequest({
            url: contextPath + "/rest/viewIssue/1/remoteIssueLink/linkType?issueId=" + issueId,
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    deferred.resolve(smartAjaxResult.data);
                } else {
                    deferred.reject(JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult));
                }
            }
        });
        
        return deferred.promise();
    }

    /**
     * Loads tab content using the active trigger's (<a> clicked) href as the url to request
     *
     * @param {JIRA.Dialog} dialog
     * @return jQuery.Promise<String>
     */
    function getTabContent (dialog) {

        var deferred = jQuery.Deferred(),
            ajaxOptions = JIRA.Dialogs.getDefaultAjaxOptions.call(dialog);

        ajaxOptions.complete = function (xhr, textStatus, smartAjaxResult) {
            if (smartAjaxResult.successful) {
                deferred.resolve(smartAjaxResult.data);
            } else {
                deferred.reject(JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult));
            }
        };

        JIRA.SmartAjax.makeRequest(ajaxOptions);

        return deferred.promise();
    }


    /**
     * Gets id from active trigger
     *
     * @param {JIRA.Dialog} dialog
     * @return {String}
     */
    function getActiveTabId (dialog) {
        return dialog.$activeTrigger.attr("id");
    }

    return function (trigger) {

        return new JIRA.FormDialog({
            id: "link-issue-dialog",
            trigger: trigger,
            onSuccessfulSubmit : function (data, smartAjaxResponse) {
                if (smartAjaxResponse.getResponseHeader("X-Atlassian-Dialog-Msg-Type") !== "warning") {
                    JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit.apply(this, arguments);
                }
            },
            issueMsg : 'thanks_issue_linked',
            widthClass : 'large',
            stacked: true,
            content: function (ready) {

                var dialog = this,
                    issueId = JIRA.Issue.getIssueId() || JIRA.IssueNavigator.getSelectedIssueId();

                getTabContent(dialog).done(function (tabContent) {

                    getRemoteIssueLinks(issueId).done(function (linkTypes) {

                        linkTypes = markSelectedLinkType(getActiveTabId(dialog), linkTypes);

                        ready(JIRA.Templates.LinkIssue.dialog({
                            linkTypes: linkTypes,
                            tabContent: tabContent
                        }));

                        jQuery.each(linkTypes, function (i, linkType) {
                            var focusElementSelector;
                            if (linkType.isSelected === true) {
                                focusElementSelector = "[name='" + linkType.focusedFieldName + "']";
                                if (jQuery(focusElementSelector).length === 0) {
                                    focusElementSelector = "#" + linkType.id
                                }
                                dialog._focusFirstField(focusElementSelector);
                            }
                        });

                    }).fail(function (errorContent) {
                        ready(errorContent);
                    });

                }).fail(function (errorContent) {
                    ready(errorContent);
                });
            },
            // A work around because the submit response does not include the menu bar. So we push the submit response
            // into our current content.
            formatSubmitResponseContent: function (content) {
                    this.get$popupContent().find(".dialog-pane").html(content);
                    return this.get$popupContent().html();
            },
            onContentRefresh: function () {
                // make sure that whenever we click on of our other tabs they load in the dialog.
                this.bindAnchorsToDialog(this.get$popupContent().find(".dialog-menu-item "));
            }
        });
    };
}();
