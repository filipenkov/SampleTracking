/**
 * Provides a menu specifically for the comment security level.
 * @constructor
 */
AJS.SecurityLevelSelect = AJS.DropdownSelect.extend({

    _createFurniture: function () {
        AJS.populateParameters();

        this._super();
    },

    _selectionHandler: function (selected) {

        var descriptor = selected.data("descriptor");

        if(descriptor && !descriptor.value()) {
            //selected all users
            this.$trigger.find("span:first").removeClass("icon-locked").addClass("icon-unlocked");
            this.$container.parent().find(".current-level").text(AJS.I18n.getText("security.level.viewable.by.all"));
        } else {
            //selected specific security level
            this.$trigger.find("span:first").removeClass("icon-unlocked").addClass("icon-locked");
            var htmlEscapedLabel = AJS.$("<div/>").text(descriptor.label()).html();
            this.$container.parent().find(".current-level").html(
            AJS.format(AJS.I18n.getText("security.level.restricted.to"), htmlEscapedLabel));
        }

        this._super(selected);
    },

    _handleDownKey: function(e) {
        //if the dropdown isn't open yet, pressing down should open it!
        if(e.keyCode === jQuery.ui.keyCode.DOWN && !this.dropdownController.isVisible()) {
            e.preventDefault();
            e.stopPropagation();
            this.show();
        }
    },

    _events: {
        trigger:  {
            keydown: function (e) {
                this._handleDownKey(e);
            },
            keypress: function (e) {
                this._handleDownKey(e);
            }
        }
    }
});

/** Preserve legacy namespace
    @deprecated AJS.SecurityLevel*/
AJS.namespace("AJS.SecurityLevel", null, AJS.SecurityLevelSelect);