
JIRA.Issues.UnhandledSaveErrorView = JIRA.Issues.BaseView.extend({

    render: function (options) {
        if (!options.response || !options.response.errorCollection) return;

        var issueId = options.issueEntity.id;
        var errorCollection = options.response.errorCollection;
        var errors = errorCollection.errorMessages.concat(_.values(errorCollection.errors));
        var template = this._getTemplate(options);
        var html;

        html = JIRA.Templates.ViewIssue.Fields[template]({
            errors: errors,
            issueKey: options.issueEntity.key,
            isAccessible: this._isAccessible(options),
            isCurrentIssue: options.isCurrentIssue
        });

        var $msg = JIRA.Messages.showErrorMsg(html, {
            closeable: true
        });

        $msg.find("#fix").click(function (e) {
            options.viewIssueLoader.replaySaveError(options.issueEntity, options.attemptedSavedIds, options.response);
            e.preventDefault();
            $msg.remove();
        });
        $msg.find(".ignore").click(function (e) {
            $msg.remove();
            e.preventDefault();
        });
    },
    _isAccessible: function (options) {
        return !!(options.response.fields && options.response.fields.length);
    },
    _getTemplate: function (options) {
        var isResumable = _.any(options.response.fields, function (field) {
            return field.id === options.attemptedSavedIds[0] && field.editHtml;
        });
        return isResumable ? "resumableSaveErrorMessage" : "saveErrorMessage";
    }

});