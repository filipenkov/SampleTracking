jQuery.namespace("JIRA.Admin.Version.VersionRow");

/**
 * Handles rendering, interaction and updating (delegating to model) of a single version
 */
JIRA.Admin.Version.VersionRow = AJS.RestfulTable.Row.extend({

    initialize: function () {

        var instance = this;

        // call super
        AJS.RestfulTable.Row.prototype.initialize.apply(this, arguments);

        this.bind(this._events.RENDER, function (el, data) {

            var $el = jQuery(this.el);

            $el.removeClass("project-config-version-released project-config-version-overdue project-config-version-archived")
                .addClass("project-config-version")
                .attr("id", "version-" + data.id + "-row")
                .attr("data-id", data.id);

            var dropdown = new AJS.Dropdown({
                trigger: $el.find(".project-config-operations-trigger"),
                content: $el.find(".project-config-operations-list")
            });

            jQuery(dropdown).bind("showLayer", function () {
                instance.$el.addClass("aui-restfultable-active");
                instance.trigger("modal");
            })
            .bind("hideLayer", function () {
                instance.trigger("modeless");
            });

            if (this.model.get("released")) {
                $el.addClass("project-config-version-released")
            }

            if (this.model.get("overdue") && !this.model.get("archived") && !this.model.get("released")) {
                $el.addClass("project-config-version-overdue")
            }

            if (this.model.get("archived")) {
                $el.addClass("project-config-version-archived")
            }

            this._assignDropdownEvents();
        });

    },

    renderStatus: function (self, all) {
        return JIRA.Templates.Versions.releaseStatus({
            version: all
        });
    },

    renderName: function (self, all) {
        return JIRA.Templates.Versions.readName({
            version: all
        });
    },

    renderDescription: function (self, all) {
        return JIRA.Templates.Versions.readDescription({
            version: all
        });
    },

    renderReleaseDate: function (self, all) {
         return JIRA.Templates.Versions.readReleaseDate({
             version: all
         });
    },

    renderOperations: function (self, all) {
        return JIRA.Templates.Versions.operations({
            version: all
        });
    },

    _assignDropdownEvents: function () {

        var instance = this;

        this.$(".project-config-operations-release").click(function (e) {
            instance.openReleaseDialog();
            e.preventDefault();
        });

        this.$(".project-config-operations-unrelease").click(function (e) {
            instance.unrelease();
            e.preventDefault();
        });

        this.$(".project-config-operations-archive").click(function (e) {
            instance.archive();
            e.preventDefault();
        });

        this.$(".project-config-operations-unarchive").click(function (e) {
            instance.unarchive();
            e.preventDefault();
        });

        this.$(".project-config-operations-delete").click(function (e) {
            instance.openDeleteDialog();
            e.preventDefault();
        });
    },

    /**
     * Opens Release Dialog
     */
    openReleaseDialog: function () {
        AJS.openDialogForRow(JIRA.Admin.Version.ReleaseForm, this, "version-release-dialog");
    },

    /**
     * Opens UnRelease Dialog
     */
    unrelease: function () {
        this.sync({
            released : false
        });
    },

    /**
     * Opens Delete Dialog
     */
    openDeleteDialog: function () {
        AJS.openDialogForRow(JIRA.Admin.Version.DeleteForm, this, "version-" + this.model.get("id") + "-delete-dialog");
    },

    /**
     * Archives version, updates operations and view accordingly
     */
    archive: function () {
        this.sync({
            archived : true
        });
    },

    /**
     * UnArchives version, updates operations and view accordingly
     */
    unarchive: function () {
        this.sync({
            archived : false
        });
    }

});
