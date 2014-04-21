jQuery.namespace("JIRA.Admin.People.editRow");

/**
 * Renders and assigns controls to table row responsible for editing roles on the people tab
 *
 */
JIRA.Admin.People.EditPeopleRow = AJS.RestfulTable.EditRow.extend({

     /**
     * Some special mapping of data sent to server. For the ProjectRoleResource we need to send all the changed categorisedActors.
     * Not just the ones that have changed
     *
     * @override
     * @param {Object} params - serialized form data
     * @return data sent to server
     */
    mapSubmitParams: function (params) {
        return this.model.mapSubmitParams(params);
    },

    renderUsers: function (self, all) {
        return JIRA.Templates.People.editUsers({
            role: all
        });
    },

    renderGroups: function (self, all) {
        return JIRA.Templates.People.editGroups({
            role: all
        });
    }
});

