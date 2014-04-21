jQuery.namespace("JIRA.Admin.Version.editRow");

/**
 * Renders and assigns controls to table row responsible for creating versions
 *
 */
JIRA.Admin.Version.EditVersionRow = AJS.RestfulTable.EditRow.extend({

    initialize: function () {

        var instance = this;

        // call super
        AJS.RestfulTable.EditRow.prototype.initialize.apply(this, arguments);

        this.bind(this._events.RENDER, function (el, data) {

            this.el.className = "project-config-versions-add-fields";

            Calendar.setup({
                singleClick: true,
                align: "Bl",
                firstDay: AJS.params.firstDay,
                button: this.$el.find("#project-config-versions-release-date-trigger")[0],
                inputField: this.$el.find("#project-config-version-release-date-field")[0],
                currentMillis: AJS.params.currentMillis,
                useISO8601WeekNumbers: AJS.params.useISO8601,
                ifFormat: AJS.params.dateFormat
            });
        });
    },

    /**
     * Renders errors with special handling for userReleaseDate as the property name that comes back from the servers
     * error collection does not match that of the input.
     *
     * @param errors
     */
    renderErrors: function (errors) {

        AJS.RestfulTable.EditRow.prototype.renderErrors.apply(this, arguments); // call super

        if (errors.releaseDate) {
            this.$(".project-config-version-release-date").append(this.renderError("userReleaseDate", errors.releaseDate));
        }

        return this;
    },

    renderStatus: function (self, all) {
        all.released = this.model.get("released");
        all.archived = this.model.get("archived");
        return JIRA.Templates.Versions.releaseStatus({
            version: all
        });
    },

    renderName: function (self, all) {
        all.project = jQuery("meta[name=projectKey]").attr("content");
        return JIRA.Templates.Versions.editName(all);
    },

    renderDescription: function (self, all) {
        return JIRA.Templates.Versions.editDescription(all);
    },

    renderReleaseDate: function (self, all) {
        return JIRA.Templates.Versions.editReleaseDate(all);
    }

});

