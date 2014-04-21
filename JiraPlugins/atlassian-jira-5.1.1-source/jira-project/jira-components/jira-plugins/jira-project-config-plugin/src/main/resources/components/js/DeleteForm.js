jQuery.namespace("JIRA.Admin.Component.DeleteForm");

/**
 * Renders and handles submission of delete form used in dialog
 */
JIRA.Admin.Component.DeleteForm = Backbone.View.extend({

    /**
     * Destorys model on server
     *
     * @param {Object} values
     * @param complete
     * @return {JIRA.Admin.Version.DeleteForm}
     */
    submit: function (values, row, complete) {

        this.$(".throbber").addClass("loading");

        if (values.component !== "swap") {
            delete values.moveIssuesTo;
        }

        this.model.destroy({
            data: values,
            success: function () {
                complete();
            },
            error: function () {
                complete();
            }
        });

        return this;
    },

    /**
     * Renders delete form. This differs from standard render methods, as it requires async request/s to the server.
     * As a result when this method is calle the first argument is a function that is called when the content has been
     * rendered.
     *
     * @param {function} ready - callback to declare content is ready
     * @return {JIRA.Admin.Version.DeleteForm}
     */
    render: function (ready) {

        var instance = this;

        this.model.getRelatedIssueCount({
            success: function (relatedIssueCount) {
                instance.el.innerHTML = JIRA.Templates.Component.deleteForm({
                    relatedIssueCount: relatedIssueCount,
                    component: instance.model.toJSON(),
                    swapComponents: instance.model.getSwapComponentsJSON(),
                    projectId: jQuery("meta[name=projectId]").attr("content")
                });

                ready.call(instance, instance.el);
            }
        });

        return this;
    }
});