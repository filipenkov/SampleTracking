/**
 * JIRA.Issue.editIssueTypeScheme
 * @author Scott Harwood
 *
 * Functionality for Issue Types Scheme page
 * - handles drag and drop sortable
 * - serialising form action
 */
(function() {
    /**
     * Serialises selected issue types and their order into a valid POST string
     * @method {private} getSelectedOptions
     * @returns {String}
     */
    var getSelectedOptions = function() {
         var s = jQuery("#selectedOptions").sortable("serialize")
        .replace(/[\[\]]/g,"").replace(/availableOptions/g,"selectedOptions");
         if (s === '') {
            return 'selectedOptions=';
         } else {
             return s;
         }
     };

    /**
     * Sets form action to include serialised selected issue types
     * @method {private} submitForm
     * @returns {Boolean} lets form continue with submission
     */
     var submitForm = function (eButton)  {
         eButton.form.action = 'ConfigureOptionSchemes.jspa?' + getSelectedOptions();
         return true;
     };

    /**
     * Sets form action to include serialised selected issue types
     * @method {private} submitAddConstantForm
     * @param {HTMLElement} eButton - submit button
     * @returns {Boolean} lets form continue with submission
     */
    var submitAddConstantForm = function (eButton) {
        var s = 'ConfigureOptionSchemes!addConstant.jspa?' + getSelectedOptions();
        s = s + getEscapedFieldValues(['name', 'description', 'fieldId', 'schemeId']);
        eButton.form.action = s;
        return true;
    };

    /**
     * Updates default issue type select box options
     * @method {private} restrictOptions
     */
    var restrictOptions = function () {

        var queryString = getSelectedOptions().replace(/selectedOptions=/g,"");
        var selectedOptions = queryString.split('&');
        var sel2 = document.getElementById("defaultOption_select");

        for (var i = 0; i < sel2.options.length; i++) {

            if (sel2.options[i].value === "" || arrayContains(selectedOptions, sel2.options[i].value)) {
                jQuery(sel2.options[i])
                .css({display: "", color: "#000", textDecoration: "none"}).removeAttr("disabled");
            } else {

                if (sel2.options[i].selected) {
                    sel2.options[i].selected = false;
                    sel2.options[0].selected = true;
                }

                jQuery(sel2.options[i])
                .css({display: "none", color: "#ffcccc", textDecoration: "line-through"}).attr("disabled","true");

            }
        }
    };

    /**
     * Will move all list nodes from one list to another
     * @method {private} moveAll
     * @param {String} fromList - id of list to move all child list nodes from
     * @param {String} toList - id of list to move all child list nodes to
     */
    var moveAll = function (fromList, toList) {
        jQuery("#" + fromList).find("li").appendTo(document.getElementById(toList));
        restrictOptions();
    };

    AJS.namespace("JIRA.Issue.editIssueTypeScheme", null, function() {

        // add handler to remove all buttons
        jQuery("#selectedOptionsRemoveAll").click(function(e){
            moveAll("selectedOptions", "availableOptions");
            // don't follow link
            e.preventDefault();
        });

        // add handler to add all buttons
        jQuery("#selectedOptionsAddAll").click(function(e){
            moveAll("availableOptions", "selectedOptions");
            e.preventDefault();
        });

        // add sortable behaviour
        jQuery("#selectedOptions").sortable({
            update: restrictOptions,
            opacity: 0.7,
            // allow dragging between ul#availableOptions also
            connectWith: [document.getElementById("availableOptions")]
        });

        // add sortable behaviour
        if (JIRA.Issue.editIssueTypeScheme.allowEditOptions) {
            jQuery("#availableOptions").sortable({
                update: restrictOptions,
                opacity: 0.7,
                // allow dragging between ul#selectedOptions also
                connectWith: [document.getElementById("selectedOptions")]
            });
        }

        jQuery("#submitSave").click(function(){
            if (/standard/.test(this.className)) {
                submitForm(this);
            }
            //e.preventDefault();
        });

        jQuery("#submitAdd").click(function(){
           if (/constant/.test(this.className)) {
                submitAddConstantForm(this);
            }
        });

        restrictOptions();
    });

    // initialise onload to be sure that all html nodes are available
    jQuery(document).ready(JIRA.Issue.editIssueTypeScheme);
})();

/** Preserve legacy namespace
    @deprecated jira.app.editIssueTypeScheme */
AJS.namespace("jira.app.editIssueTypeScheme", null, JIRA.Issue.editIssueTypeScheme);
