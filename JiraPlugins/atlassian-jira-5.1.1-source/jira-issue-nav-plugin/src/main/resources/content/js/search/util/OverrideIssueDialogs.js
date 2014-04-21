
(function() {

    var viewIssueController;

    JIRA.Issues.overrideIssueDialogs = function(_viewIssueController) {

        viewIssueController = _viewIssueController;

        // Monkey patch quick edit
        JIRA.unbind("QuickEdit.sessionComplete");
        JIRA.bind("QuickEdit.sessionComplete", updateSelectedIssue);

        // Monkey patch quick create subtask
        JIRA.unbind("QuickCreateSubtask.sessionComplete");
        JIRA.bind("QuickCreateSubtask.sessionComplete", updateSelectedIssue);

        // Store the issue we opened the dialog on so we can compare if we are on the same issue (hence refresh) when
        // we submit dialog.
        JIRA.bind("Dialog.show", function (e, $el, instance) {
            if (instance) {
                instance.issueId = getSelectedIdFromModel();
            }
        });

        // Monkey patch issue transitions
        // atl_token is not written into the template onto the server for some reason, so ensure it exists on render
        JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function(e, $el) {
            $el.find(".issueaction-workflow-transition").each(function() {
                var $this = AJS.$(this);
                var href = $this.attr("href");
                href = href.replace(/atl_token=[^&]*/, "atl_token=" + atl_token());
                $this.attr("href", href);
            });
        });

        // Monkey patch the "Log In" link to make it redirect correctly. Internally, the API generates this link using
        // whatever URL was requested: in this case, that's the API and the user is redirected to a page-full of JSON.
        JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function(e, $el) {
            var $loginLink = AJS.$("#ops-login-lnk");
            if ($loginLink.length == 0) {
                return;
            }

            var location = window.location;
            var uriComponents = parseUri($loginLink.attr("href"));
            uriComponents.queryKey.os_destination = location.pathname.slice(contextPath.length) + location.search;
            $loginLink.attr("href", uriComponents.path + "?" + AJS.$.param(uriComponents.queryKey));
        });
    };

    // Monkey patch JIRA.FormDialog.prototype.getDefaultOptions to override its onDialogFinished member,
    // which is a callback on successful dialog submission that hides the dialog and reloads the page.
    var oldJiraFormDialogGetDefaultOptions = JIRA.FormDialog.prototype._getDefaultOptions;
    JIRA.FormDialog.prototype._getDefaultOptions = function() {
        var options = oldJiraFormDialogGetDefaultOptions.apply(this, arguments);
        options.onDialogFinished = onDialogFinished;

        return options;
    };

    // Monkey patch JIRA.FormDialog.prototype._performRedirect to not actually redirect at all
    var oldJiraFormDialogPerformRedirect = JIRA.FormDialog.prototype._performRedirect;
    JIRA.FormDialog.prototype._performRedirect = onDialogFinished;

    /**
     * Gets the id from the model currently selected in the viewIssueController, if it exists
     */
    function getSelectedIdFromModel() {
        var model = viewIssueController.getSelectedIssueModel();
        return model && model.getEntity().id;
    }

    function onDialogFinished() {
        // JRADEV-11573
        if (this.options.id === "clone-issue-dialog") {
            // This works for standalone only, JRADEV-11618 to fix for splitview
            oldJiraFormDialogPerformRedirect.apply(this, arguments);
        }
        // JRADEV-10933
        else if (this.options.id === "delete-issue-dialog") {
            var redirectUrl = this._getTargetUrlValue();
            if (redirectUrl && redirectUrl !== "") {
                AJS.reloadViaWindowLocation(redirectUrl);
            } else if (AJS.$(".page-navigation #next-issue").length > 0) {
                AJS.reloadViaWindowLocation(AJS.$(".page-navigation #next-issue").attr("href"));
            } else if (AJS.$(".page-navigation #return-to-search").length > 0) {
                AJS.reloadViaWindowLocation(AJS.$(".page-navigation #return-to-search").attr("href"));
            } else {
                AJS.reloadViaWindowLocation(AJS.$("#find_link").attr("href"));
            }
        } else {
            var instance = this;
            this.showFooterLoadingIndicator();
            updateIssue(this.issueId).always(function() {
                instance.hideFooterLoadingIndicator();
                instance.hide();
            });
        }
    }

    function updateSelectedIssue() {
        // TODO: this is crap, should have a getId() on issueModel
        var issueId = getSelectedIdFromModel();
        if (!issueId) {
            console.log("Unexpected: no selected issue");
            return;
        }
        updateIssue(issueId);
    }

    function updateIssue(issueId) {
        var deferred = AJS.$.Deferred();
        if (issueId === viewIssueController.getSelectedIssueModel().getEntity().id) {
            viewIssueController.getSelectedIssueModel().getIssueEventBus().triggerRefreshIssue({
                success: function() { deferred.resolve(arguments); },
                error: function() { deferred.reject(arguments); }
            });
        }
        else {
            deferred.resolve();
        }
        return deferred.promise();
    }
})();
