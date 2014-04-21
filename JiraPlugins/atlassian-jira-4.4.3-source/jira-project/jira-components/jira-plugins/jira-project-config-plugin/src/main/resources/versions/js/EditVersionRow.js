jQuery.namespace("JIRA.Admin.Version.editRow");

/**
 * Renders and assigns controls to table row responsible for creating versions
 *
 */
JIRA.Admin.Version.EditVersionRow = JIRA.RestfulTable.EditRow.extend({

    /**
     * Renders errors with special handling for userReleaseDate as the property name that comes back from the servers
     * error collection does not match that of the input.
     *
     * @param errors
     */
    renderErrors: function (errors) {

        JIRA.RestfulTable.EditRow.prototype.renderErrors.apply(this, arguments); // call super

        if (errors.releaseDate) {
            this.$(".project-config-version-release-date").append(this.renderError(errors.releaseDate));
        }


        return this;
    },

    /**
     * Handles all the rendering of the create version row.
     *
     * @param {Object} renderData
     * ... {Object} values - Values of fields
     *
     */
    render: function (data) {

        data.released = this.model.get("released");
        data.archived = this.model.get("archived");
        data.project = jQuery("meta[name=projectKey]").attr("content");

        this.el.className = "project-config-versions-add-fields";

        this.$el.html(JIRA.Templates.Versions.editVersionRow(data));

        Calendar.setup({
            singleClick: true,
            align: "Bl",
            firstDay: AJS.params.firstDay,
            button: this.$el.find("#project-config-versions-release-date-trigger")[0],
            inputField: this.$el.find("#project-config-version-release-date-field")[0],
            currentMillis: AJS.params.currentMillis,
            useISO8061: AJS.params.useISO8061,
            ifFormat: AJS.params.dateFormat
        });

        return this;
    }
});

