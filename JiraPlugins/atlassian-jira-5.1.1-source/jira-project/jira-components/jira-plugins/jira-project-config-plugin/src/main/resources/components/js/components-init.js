


jQuery(function () {
    var $componentsTable = jQuery("#project-config-components-table");



    var restfultable = new AJS.RestfulTable({
        el: $componentsTable,
        autoFocus: true,
        resources: {
            all: JIRA.REST_BASE_URL + "/project/" + JIRA.ProjectConfig.getKey() + "/components",
            self: contextPath + "/rest/api/" + JIRA.REST_VERSION + "/component"
        },
        columns: [
            {
                id: "icon",
                header: "",
                styleClass: "project-config-component-icon",
                editable: false
            },
            {
                id: "name",
                header: AJS.I18n.getText("common.words.name"),
                styleClass: "project-config-component-name"
            },
            {
                id: "description",
                header: AJS.I18n.getText("common.words.description"),
                emptyText: AJS.I18n.getText('admin.project.add.description'),
                styleClass: "project-config-component-description"
            },
            {
                id: "realAssignee",
                styleClass: "project-config-component-lead",
                header: AJS.I18n.getText("admin.projects.component.lead"),
                emptyText: AJS.I18n.getText("admin.project.components.lead"),
                fieldName: "leadUserName-field"
            },
            {
                id: 'realAssigneeType',
                header: AJS.I18n.getText("admin.projects.default.assignee"),
                emptyText: AJS.I18n.getText("admin.project.components.lead"),
                fieldName: "assigneeType",
                styleClass: "project-config-component-assignee"
            }
        ],
        editable: true,
        noEntriesMsg: AJS.I18n.getText("admin.project.components.none"),
        model: JIRA.Admin.Component.ComponentModel,
        views: {
            row: JIRA.Admin.Component.ComponentRow,
            editRow: JIRA.Admin.Component.EditComponentRow
        }
    });

    $componentsTable.one(AJS.RestfulTable.Events.INITIALIZED, function () {
        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$componentsTable]);
    });

    $componentsTable.closest("form").addClass("ajs-dirty-warning-exempt"); // Stop dirty form warnings from firing on table
});
