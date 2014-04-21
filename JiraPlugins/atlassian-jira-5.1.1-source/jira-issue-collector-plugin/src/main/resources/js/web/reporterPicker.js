/**
 * A single-select list for selecting Reporters.
 *
 *  IMPORTANT! Please note this is a customized copy of AssagneePicker. See the original script at:
 *  (jira/jira-components/jira-webapp/src/main/webapp/includes/jira/field/AssigneePicker.js).
 *
 * @constructor JIRA.CollectorReporterPicker
 * @extends AJS.SingleSelect
 */
JIRA.CollectorReporterPicker = AJS.SingleSelect.extend({

    init: function (options) {

        var element = options.element;

        // Returns the data sent to the server for the AJAX search
        function data(query) {
            return {
                username: query,
                projectKeys: AJS.params.projectKeys,
                issueKey:AJS.params.assigneeEditIssueKey,
                actionDescriptorId:AJS.params.actionDescriptorId,
                maxResults:10
            };
        }

        function formatResponse(response) {

            var ret = [];

            if (response.length) {
                // Search results
                var groupDescriptor = new AJS.GroupDescriptor({
                    weight: 1,          // index of group in dropdown
                    id: "reporter-search",
                    uniqueItemScope: 'container',
                    replace: true,     // Allow subsequent calls to replace model items
                    label: AJS.I18n.getText("collector.plugin.admin.allowed.reporter")
                });

                for (var i = 0, len = response.length; i < len; i++) {
                    var user = response[i];

                    var username = user.name;
                    var displayName = user.displayName;
                    var emailAddress = user.emailAddress;
                    var label = displayName + ' - ' + emailAddress + ' (' + username + ')';

                    groupDescriptor.addItem(new AJS.ItemDescriptor({
                        value: username,
                        fieldText: displayName,
                        label: label,
                        allowDuplicate: false,
                        icon: user.avatarUrls['16x16']
                    }));
                }
                ret.push(groupDescriptor);
            }

            return ret;
        }

        AJS.$.extend(options, {
            submitInputVal: true,
            showDropdownButton: !!element.attr('data-show-dropdown-button'),
            errorMessage: AJS.I18n.getText("collector.plugin.admin.invalid.reporter"),
            localDataGroupId: 'reporter-group-suggested',
            serverDataGroupId: 'reporter-search',
            ajaxOptions: {
                url: function() {
                    return contextPath + "/rest/collectors/1.0/project/reporter/search";
                },
                query: true,                // keep going back to the server for each keystroke
                minQueryLength: 1,
                noQueryNoRequest: true,     // don't make a server request if no query string
                data: data,
                formatResponse: formatResponse
            }
        });

        this._super(options);
    },

    getAjaxOptions: function() {
        var ajaxOptions = this._super();
        if(typeof ajaxOptions.url === 'function') {
            //first time this runs lets evaluate the function and set the URL to be what the function returns.
            //doing so over and over would be expensive.
            this.options.ajaxOptions.url = ajaxOptions.url();
            ajaxOptions.url = this.options.ajaxOptions.url;
        }

        return ajaxOptions;
    },

    cleanUpModel: function () {}

});