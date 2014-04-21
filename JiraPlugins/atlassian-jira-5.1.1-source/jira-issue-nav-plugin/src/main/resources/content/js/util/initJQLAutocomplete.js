(function() {

    var advancedSearchTextareaSelector = ".advanced-search";

    AJS.$(document).delegate(advancedSearchTextareaSelector, "focus", initJQLAutoComplete);

    function initJQLAutoComplete() {
        // The renderer may destroy the old <textarea> and insert a new one, so we'll need to
        // init JQL autocompletion anytime this property is not set.
        if (!AJS.params.autocompleteEnabled || AJS.$(this).data("JQLAutoComplete_init")) {
            return;
        }
        AJS.$(this).data("JQLAutoComplete_init", true);

        var $advSearch = AJS.$(this);
        var jqlFieldNames = JSON.parse(jQuery("#jqlFieldz").text());
        var jqlFunctionNames = JSON.parse(jQuery("#jqlFunctionNamez").text());
        var jqlReservedWords = JSON.parse(jQuery("#jqlReservedWordz").text());
        var jqlAutoComplete = JIRA.JQLAutoComplete({
            fieldID: this.id,
            parser: JIRA.JQLAutoComplete.MyParser(jqlReservedWords),
            queryDelay: .65,
            jqlFieldNames: jqlFieldNames,
            jqlFunctionNames: jqlFunctionNames,
            minQueryLength: 0,
            allowArrowCarousel: true,
            autoSelectFirst: false,
            errorID: 'jqlerrormsg'
        });

        jqlAutoComplete.buildResponseContainer();
        jqlAutoComplete.parse($advSearch.text());
        jqlAutoComplete.updateColumnLineCount();

        $advSearch.keypress(function(event) {
            if (jqlAutoComplete.dropdownController === null || !jqlAutoComplete.dropdownController.displayed || jqlAutoComplete.selectedIndex < 0) {
                if (event.keyCode == 13 && !event.ctrlKey && !event.shiftKey) {
                    event.preventDefault();
                    jQuery(this.form).submit();
                }
            }
        });

        $advSearch.click(function() {
            jqlAutoComplete.dropdownController.hideDropdown();
        });
    }
})();
