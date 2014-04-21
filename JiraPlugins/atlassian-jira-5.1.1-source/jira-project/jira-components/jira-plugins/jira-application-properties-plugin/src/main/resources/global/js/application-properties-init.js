// Initialises the application properties table on the advanced configuration page

jQuery(function(){
    var propRest = contextPath + "/rest/api/" + JIRA.REST_VERSION + "/application-properties";
    var appPropertyTable = jQuery("#application-properties-table");

        new AJS.RestfulTable({
            el: appPropertyTable, // table to add entries to. Entries are appended to the tables <tbody> element
            resources: {
                all: propRest,
                self: propRest
            },
            columns: [
                {
                    id: "key",
                    header: "Key",
                    styleClass: "application-property-key-col",
                    editable: false
                },
                {
                    id: "value",
                    header: "Value",
                    styleClass: "application-property-value-col",
                    emptyText: AJS.I18n.getText("admin.advancedconfiguration.setvalue")
                }
            ],
            editable: true,
            createDisabled: true,
            views: {
                editRow: JIRA.Admin.AppProperty.EditRow,
                row: JIRA.Admin.AppProperty.Row
            }
        });
});
