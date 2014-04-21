// Initialisation of our versions table

jQuery(function () {
    var $componentsTable = jQuery("#project-config-components-table");

    if(!($componentsTable.length > 0)) {
        return;
    }
    
    function getResourceURL () {
        return JIRA.REST_BASE_URL + "/project/" + JIRA.ProjectConfig.getKey() + "/components";
    }

    function getComponents (callback) {
        JIRA.SmartAjax.makeRequest({
            url: getResourceURL(),
            complete: function (xhr, status, response) {
                if (response.successful) {
                    callback(response.data)
                } else {
                    $componentsTable.trigger("serverError",
                            [JIRA.SmartAjax.buildSimpleErrorContent(response)]);
                }
            }
        });
    }

    getComponents(function (components) {

        new JIRA.RestfulTable({
            el: $componentsTable,
            url: contextPath + "/rest/api/" + JIRA.REST_VERSION + "/component",
            editable: true,
            entries: components,
            noEntriesMsg: AJS.I18n.getText("admin.project.components.none"),
            model: JIRA.Admin.Component.ComponentModel,
            views: {
                row: JIRA.Admin.Component.ComponentRow,
                editRow: JIRA.Admin.Component.EditComponentRow
            }
        });

        jQuery(".jira-restfultable-init").remove();

        JIRA.userhover($componentsTable);
    });
});
