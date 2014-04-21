
// Initialisation of our versions table
jQuery(function () {

    var versionsTable = jQuery("#project-config-versions-table");

    if(!(versionsTable.length > 0)) {
        return;
    }

    function getResourceURL () {
        return JIRA.REST_BASE_URL + "/project/" + JIRA.ProjectConfig.getKey() +"/versions";
    }

    function getVersions (callback) {
        JIRA.SmartAjax.makeRequest({
            url: getResourceURL(),
            data: {expand: "operations"},
            complete: function (xhr, status, response) {
                if (response.successful) {
                    callback(response.data.reverse())
                } else {
                    versionsTable.trigger("serverError",
                            [JIRA.SmartAjax.buildSimpleErrorContent(response)]);
                }
            }
        });
    }

    function focusFirstField () {
        versionsTable.find(":input:text:first").focus(); // set focus to first field
    }

    getVersions(function (versions) {


        JIRA.Admin.VersionTable = new JIRA.RestfulTable({
            editable: true,
            reorderable: true,
            el: versionsTable,
            url: contextPath + "/rest/api/" + JIRA.REST_VERSION + "/version",
            entries: versions,
            model: JIRA.VersionModel,
            noEntriesMsg: AJS.I18n.getText("admin.project.versions.none"), // AJS.params.noVersions,
            views: {
                editRow: JIRA.Admin.Version.EditVersionRow,
                row: JIRA.Admin.Version.VersionRow
            }
        });

        jQuery(".jira-restfultable-init").remove();

        var $mergeLink = jQuery('<a id="project-config-operations-merge" href="#"></a>');

        jQuery('<li>')
            .append($mergeLink.text(AJS.I18n.getText('admin.manageversions.merge')).hide())
            .appendTo("#project-config-panel-versions ul.operation-menu");

        var dialogForm = new JIRA.Admin.Version.MergeForm({
            collection: JIRA.Admin.VersionTable.getModels()
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
                dialogForm.submit(this.$form.serializeToObject(), function () {
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

        versionsTable.bind("addRow", function (e, table) {
            if (table._models.length > 1) {
                $mergeLink.show();
            }
        });

        versionsTable.bind("removeRow", function (e, table) {
            if (table._models.length < 2) {
                $mergeLink.hide();
            }
        });

        focusFirstField();

        if (versions.length > 1) {
            $mergeLink.show();
        }
    })
});
