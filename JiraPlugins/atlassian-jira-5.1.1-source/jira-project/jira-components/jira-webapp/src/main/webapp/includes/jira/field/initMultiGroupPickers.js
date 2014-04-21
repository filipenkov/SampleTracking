(function () {

    function initMultiGroupPickers(ctx) {
        ctx.find('.js-default-multi-group-picker').each(function () {
            new AJS.MultiSelect({
                element: this,
                itemAttrDisplayed: "label",
                showDropdownButton: false,
                ajaxOptions:  {
                    data: function (query) {
                        return {
                            query: query,
                            exclude: this.model.getSelectedValues()
                        }
                    },
                    url: JIRA.REST_BASE_URL + "/groups/picker",
                    query: true, // keep going back to the sever for each keystroke
                    formatResponse: JIRA.GroupPickerUtil.formatSuggestions
                }
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            initMultiGroupPickers(context);
        }
    });

})();
