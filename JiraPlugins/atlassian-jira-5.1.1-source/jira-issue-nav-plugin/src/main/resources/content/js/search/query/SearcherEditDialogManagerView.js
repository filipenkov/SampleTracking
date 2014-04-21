/**
 * Mixin for views that control a
 */
JIRA.Issues.SearcherEditDialogManagerView = {

    initSearcherEditDialog: function(editView, dialog, searcherCollection) {

        editView.bindFilterSelected(function(id) {
            // A searcher has been submitted. Create a clause and add it to the clause collection
            var promise = searcherCollection.createOrUpdateClauseWithQueryString(id);

            promise.done(function() {
                // Check for an "error" class in the searcher's editHtml.
                // If so, leave the dialog open and rerender to the the updated editHtml.
                // Otherwise close the dialog.
                var searcherModel = searcherCollection.get(id);
                if (searcherModel.hasErrorInEditHtml()) {
                    editView.render();
                }
                else {
                    dialog.hide();
                }
            });
        });

        editView.bindHideRequested(function() {
            dialog.hide();
        });
    }
};
