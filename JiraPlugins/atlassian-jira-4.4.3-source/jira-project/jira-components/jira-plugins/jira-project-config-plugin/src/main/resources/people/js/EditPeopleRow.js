jQuery.namespace("JIRA.Admin.People.editRow");

/**
 * Renders and assigns controls to table row responsible for editing roles on the people tab
 *
 */
JIRA.Admin.People.EditPeopleRow = JIRA.RestfulTable.EditRow.extend({

     /**
     * Some special mapping of data sent to server. For the ProjectRoleResource we need to send all the changed categorisedActors.
     * Not just the ones that have changed
     *
     * @override
     * @param {Object} params - serialized form data
     * @return data sent to server
     */
    mapSubmitParams: function (params) {

        var current = this.model.toJSON(),
            currentUserRole = current.categorisedActors["atlassian-user-role-actor"],
            currentGroupRole = current.categorisedActors["atlassian-group-role-actor"],
            data = {categorisedActors: {}};

        if (params.users) {
            if (currentUserRole && !_.isEqual(currentUserRole, params.users)) {
                data.categorisedActors["atlassian-user-role-actor"] = params.users;
            } else if (!currentUserRole && params.users.length > 0) {
                data.categorisedActors["atlassian-user-role-actor"] = params.users;
            }
        }

        if (params.groups) {
            if (currentGroupRole && !_.isEqual(currentGroupRole, params.groups)) {
                data.categorisedActors["atlassian-group-role-actor"] = params.groups;
            } else if (!currentGroupRole && params.groups.length > 0) {
                data.categorisedActors["atlassian-group-role-actor"] = params.groups;
            }
        }

        if (data.categorisedActors["atlassian-group-role-actor"]) {
            data.categorisedActors["atlassian-user-role-actor"] = params.users;
        }

        if (data.categorisedActors["atlassian-user-role-actor"]) {
            data.categorisedActors["atlassian-group-role-actor"] = params.groups;
        }


        if (!_.isEmpty(data.categorisedActors)) {
            return data;
        }
    },

    /**
     * Handles all the rendering of the create version row. This includes handling validation errors if there is any
     *
     * @param {Object} data
     * ... {Object} errors - Errors returned from the server on validation
     * ... {Object} vales - Values of fields
     *
     */
    render: function (data) {

        this.el.className = "project-config-people-add-fields";

        this.$el.html(JIRA.Templates.People.editPeopleRow({
            role: data.values
        }));

        new AJS.MultiSelect({
            element: this.$("#project-config-people-users-select"),
            itemAttrDisplayed: "label",
            showDropdownButton: false,
            removeOnUnSelect: true,
            width: this.$(".project-config-role-users").width(),
            ajaxOptions: {
                url: contextPath + "/rest/api/1.0/users/picker",
                query: true, // keep going back to the sever for each keystroke
                data: { showAvatar: true },
                formatResponse: function (response) {

                    var ret = [];

                    AJS.$(response).each(function(i, suggestions) {

                        var groupDescriptor = new AJS.GroupDescriptor({
                            weight: i, // order or groups in suggestions dropdown
                            label: suggestions.footer // Heading of group
                        });

                        AJS.$(suggestions.users).each(function(){
                            groupDescriptor.addItem(new AJS.ItemDescriptor({
                                value: this.name, // value of item added to select
                                label: this.displayName, // title of lozenge
                                html: this.html,
                                icon: this.avatarUrl,
                                allowDuplicate: false
                            }));
                        });

                        ret.push(groupDescriptor);
                    });

                    return ret;
                }
            }
        });

        new AJS.MultiSelect({
            element: this.$el.find("#project-config-people-groups-select")[0],
            itemAttrDisplayed: "label",
            showDropdownButton: false,
            width: this.$(".project-config-role-groups").width(),
            ajaxOptions: {
                url: JIRA.REST_BASE_URL + "/groups/picker",
                query: true, // keep going back to the sever for each keystroke
                formatResponse: function (response) {

                    var ret = [];

                    AJS.$(response).each(function(i, suggestions) {

                        var groupDescriptor = new AJS.GroupDescriptor({
                            weight: i, // order or groups in suggestions dropdown
                            label: suggestions.header // Heading of group
                        });

                        AJS.$(suggestions.groups).each(function(){
                            groupDescriptor.addItem(new AJS.ItemDescriptor({
                                value: this.name, // value of item added to select
                                label: this.name, // title of lozenge
                                html: this.html
                            }));
                        });

                        ret.push(groupDescriptor);
                    });

                    return ret;
                }
            }
        });

        return this;
    }
});

