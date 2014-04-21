// Initialises the application properties table on the advanced configuration page

jQuery(function(){
    var propRest = contextPath + "/rest/api/" + JIRA.REST_VERSION + "/application-properties";
    var appPropertyTable = jQuery("#application-properties-table");

    function getProperties(callback) {
        JIRA.SmartAjax.makeRequest({
            url: propRest,
            complete: function (xhr, status, response) {
                if (response.successful) {
                    callback(response.data)
                } else {
                    appPropertyTable.trigger("serverError",
                            [JIRA.SmartAjax.buildSimpleErrorContent(response)]);
                }
            }
        });
    }
    getProperties(function(props){
        new JIRA.RestfulTable({
            el: appPropertyTable, // table to add entries to. Entries are appended to the tables <tbody> element
            url: propRest, //rest resource for collection
            entries: props,
            editable: true,
            createDisabled: true,
            views: {
                editRow: JIRA.Admin.AppProperty.EditRow,
                row: JIRA.Admin.AppProperty.Row
            }
        });

    });


});
