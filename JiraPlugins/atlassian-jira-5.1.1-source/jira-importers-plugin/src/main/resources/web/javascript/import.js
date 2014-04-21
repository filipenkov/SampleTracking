(function() {
    jQuery.namespace('importer');

    var EXISTING_CUSTOM_FIELD = 'existingCustomField';

	importer.displayCommentHelp = function() {
		var commentMapping = false;

		AJS.$("SELECT.importField").each(function() {
			if (AJS.$(this).val() == "comment") {
				commentMapping = true;
			}
		});

		AJS.$("#comment-help").toggle(commentMapping);
	};

	importer.displayExistingCustomFieldHelp = function() {
		var cfMapping = false;

		AJS.$("SELECT.importField").each(function() {
			if (AJS.$(this).val() == EXISTING_CUSTOM_FIELD) {
				cfMapping = true;
			}
		});

		AJS.$("#existing-custom-field-help").toggle(cfMapping);
	};

	// Toggles
    importer.toggle = new JIRA.ToggleBlock({
        blockSelector: ".toggle-wrap",
        triggerSelector: ".mod-header h3",
        cookieCollectionName: "jim-block-states",
        originalTargetIgnoreSelector: "a"
    });

	importer.NextButtonAwareSingleSelect = AJS.SingleSelect.extend({
		submitForm: function () {
			if (!this.suggestionsVisible) {
            	this.handleFreeInput();
				AJS.$("#nextButton:enabled").click();
        	}
		},

		init: function (options) {
			this._super(options);
			this.$field.select(function() {return false;}); // disable ugly text selection when item selected
		},

		updateOverlabel: function () {
			 if (this.$overlabel){
                 if(this.$field.val() && this.$field.val() !== "") {
                     // fix overlabel not working when using dropdown to select entry
                     this.$overlabel.addClass("hidden");
                 } else if (!this.$field.is(":focus")) {
                     this.$overlabel.removeClass("hidden");
                 }
			 }
		},

		enable: function () {
			if (this.$field.attr("disabled")) {
				this.$field.removeAttr("disabled");
				this._assignEvents("dropdownAndLoadingIcon", this.$dropDownIcon);
				this._super();
			}
		},

		disable: function () {
			if (!this.$field.attr("disabled")) {
				this.$field.attr("disabled", "disabled");
				this._unassignEvents("dropdownAndLoadingIcon", this.$dropDownIcon);
				this._super();
			}
		},

		selectByKey: function (key) {
			var descriptor = key && this.model.getDescriptor(key);
			if (descriptor) {
				this.setSelection(descriptor);
			} else {
				this.$field.val("");
				this.clearSelection();
			}
		}
	});

    AJS.$(document).ready(function() {
        // fix for https://jira.atlassian.com/browse/JRA-27131, studio specific code
        AJS.$("#menu_section_studio_import_export_section + ul > li > a[href='#']").each(function(idx, elem) {AJS.$(elem).css("cursor", "default").parent().css("margin-left", "33px");})
    });

}());
