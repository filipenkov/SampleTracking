// Initialisation of our versions table
jQuery(function () {

    var versionsTable = jQuery("#project-config-versions-table");


    JIRA.Admin.versionTable = new AJS.RestfulTable({
        autoFocus: true,
        el: versionsTable,
        reverseOrder: true,
        model: JIRA.VersionModel,
        noEntriesMsg: AJS.I18n.getText("admin.project.versions.none"),
        loadingMsg: AJS.I18n.getText('admin.project.versions.loading'),
        reorderable: true,
        resources: {
            all: JIRA.REST_BASE_URL + "/project/" + JIRA.ProjectConfig.getKey() +"/versions?expand=operations",
            self: JIRA.REST_BASE_URL + "/version"
        },
        columns: [
            {
                id: "status",
                styleClass: "project-config-release-status",
                header: "",
                editable: false
            },
            {
                id: "name",
                styleClass: "project-config-version-name",
                header: AJS.I18n.getText("common.words.name")
            },
            {
                id: "description",
                styleClass: "project-config-version-description",
                header: AJS.I18n.getText("common.words.description"),
                emptyText: AJS.I18n.getText('admin.project.add.description')
            },
            {
                id: "releaseDate",
                styleClass: "project-config-version-release-date",
                header: AJS.I18n.getText("version.releasedate"),
                fieldName: "userReleaseDate",
                emptyText: AJS.I18n.getText("admin.project.add.release.date")
            }
        ],
        views: {
            editRow: JIRA.Admin.Version.EditVersionRow,
            row: JIRA.Admin.Version.VersionRow
        }
    });

    versionsTable.closest("form").addClass("ajs-dirty-warning-exempt"); // Stop dirty form warnings from firing on table

    var $mergeLink = jQuery('<a id="project-config-operations-merge" href="#"></a>');

    jQuery('<li>')
        .append($mergeLink.text(AJS.I18n.getText('admin.manageversions.merge')).hide())
        .appendTo("#project-config-panel-versions ul.operation-menu");

    var dialogForm = new JIRA.Admin.Version.MergeForm({
        collection: JIRA.Admin.versionTable.getModels()
    });

    var dialog = new JIRA.FormDialog({
        id: "versionsMergeDialog",
        trigger: $mergeLink,
        content: function (callback) {
            dialogForm.render(function (el) {
                callback(el);
            });
        },
        submitHandler: function (e) {
            dialogForm.submit(this.$form.serializeObject(), function () {
                dialog.hide();
            });
            e.preventDefault();
        }
    });

    dialog.onContentReady(function () {
        var el = jQuery("#versionsMergeDialog select[multiple]");
        new AJS.MultiSelect({
            element: el,
            itemAttrDisplayed: "label"
        });
    });

    AJS.$(JIRA.Admin.versionTable).bind(AJS.RestfulTable.Events.ROW_ADDED, function (e, table) {
        if (this.getModels().length > 1) {
            $mergeLink.show();
        }
    }).bind(AJS.RestfulTable.Events.INITIALIZED, function () {
        if (this.getModels().length > 1) {
            $mergeLink.show();
        }
    }).bind(AJS.RestfulTable.Events.ROW_REMOVED, function (e, table) {
        if (this.getModels().length < 2) {
            $mergeLink.hide();
        }
    });
});