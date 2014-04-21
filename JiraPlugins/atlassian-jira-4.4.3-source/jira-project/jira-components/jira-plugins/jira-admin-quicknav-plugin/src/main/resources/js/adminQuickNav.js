/**
 * Admin quickseach autocompletes.
 * - Exports instance of adminQuickNavDialog, which shows autocomplete in a dialog. This is used in conjuction with
 * keyboard shortcut "g then a"
 * - Attaches autocomplete to the header "Administration" menu when it is opened
 */
jQuery(function () {

    function flattenSections(group, section) {

        if (section && section.items) {
            jQuery.each(section.items, function () {
                 group.addItem(new AJS.ItemDescriptor({
                    href: this.linkUrl,
                    label: this.label,
                    keywords: this.keywords
                }));
            });
        }

        if (section && section.sections) {
            jQuery.each(section.sections, function (i, section) {
                flattenSections(group, section);
            });
        }
    }

    // Generic options for admin autocomplete
    function getAdminQuickNavOptions(element) {

        return {

            element: element, // suggestions container
            id: "admin-quick-nav",
            ajaxOptions: {
                dataType: "json",
                url: contextPath + "/rest/webfragments/1.0/fragments/system.admin.top.navigation.bar",
                formatResponse: function (suggestions) {

                    var ret = [];

                     AJS.$.each(suggestions.sections, function(name, topLevelSection) {

                        var groupDescriptor = new AJS.GroupDescriptor({
                            label: topLevelSection.label // Heading of group
                        });

                        flattenSections(groupDescriptor, topLevelSection);
                        ret.push(groupDescriptor);
                    });

                    return ret;
                }

            },
            showDropdownButton: true
        };
    }


    // export dialog, this method is called using keyboard shortcut. See system-keyboard-shortcuts-plugin.xml
    jira.app.adminQuickNavDialog = new JIRA.Dialog({

        id: "admin-quicknav-dialog",

        // call soy template for dialog contents. Contains header cancel button etc.
        content: function (callback) {
            callback(JIRA.Templates.adminQuickNavDialog());
        },

        // every time we refresh the dialog contents we recreate the control
        onContentRefresh: function () {

            var suggestionsContainer = jQuery("#administration-suggestions", this.$content),
                autocompleteOptions = getAdminQuickNavOptions(suggestionsContainer),
                autocomplete, instance = this;

            autocompleteOptions.loadOnInit = true; // make request for suggestion on construction
            autocomplete = new AJS.QueryableDropdownSelect(autocompleteOptions);

            autocomplete._handleServerError = function(smartAjaxResult) {
                var errMsg = JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult);
                var errorClass = smartAjaxResult.status === 401?'warning':'error';

                AJS.$("#admin-quicknav-dialog-form", this.$content).html(AJS.$('<div class="ajaxerror"><div class="aui-message ' + errorClass+'"><p>' + errMsg + '</p></div></div>'), false);
            }

            autocomplete.$field.focus();

            //need to hookup the 'close' link to close the dialog.
            $cancel = AJS.$(".cancel", this.$content);
            $cancel.click(function (e) {
                if (instance.xhr)
                {
                    instance.xhr.abort();
                }
                instance.xhr = null;
                instance.cancelled = true;
                instance.hide();
                e.preventDefault();
            });
        },

        widthClass: "small"
    });

    // Add Quicksearch to header (only visible when in administration)
    var suggestionsContainer = jQuery("#header-administration-suggestions"),
        autocompleteOptions;


    if (suggestionsContainer.length === 1) {
        autocompleteOptions = getAdminQuickNavOptions(suggestionsContainer);
        autocompleteOptions.width = 300;
        autocompleteOptions.overlabel = AJS.I18n.getText("admin.keyboard.shortcut.admin.quicksearch.desc");
        new AJS.QueryableDropdownSelect(autocompleteOptions);
    }
});




