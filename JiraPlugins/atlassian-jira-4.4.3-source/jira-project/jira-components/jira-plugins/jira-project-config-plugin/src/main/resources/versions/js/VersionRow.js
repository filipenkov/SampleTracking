jQuery.namespace("JIRA.Admin.Version.VersionRow");

/**
 * Handles rendering, interaction and updating (delegating to model) of a single version
 */
JIRA.Admin.Version.VersionRow = JIRA.RestfulTable.Row.extend({

    /**
     * Resets and renders version row in table. This should be called whenever the model changes.
     */
    render: function () {

        var instance = this,
            id = this.model.get("id"),
            $el = jQuery(this.el);

        $el.attr("className", "project-config-version"); // reset all classNames

        $el.attr("id", "version-" + id + "-row").attr("data-id", id);

        $el.html(JIRA.Templates.Versions.versionRow({
            version: this.model.toJSON()
        }));

        var dropdown = new AJS.Dropdown({
            trigger: $el.find(".project-config-operations-trigger"),
            content: $el.find(".project-config-operations-list")
        });

        jQuery(dropdown).bind("showLayer", function () {
            instance.$el.addClass("jira-restfultable-active");
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

        return this;
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
        this._openDialog(JIRA.Admin.Version.ReleaseForm, "version-release-dialog");
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
        this._openDialog(JIRA.Admin.Version.DeleteForm, "version-" + this.model.get("id") + "-delete-dialog");
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
