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
	}

	importer.displayExistingCustomFieldHelp = function() {
		var cfMapping = false;

		AJS.$("SELECT.importField").each(function() {
			if (AJS.$(this).val() == EXISTING_CUSTOM_FIELD) {
				cfMapping = true;
			}
		});

		AJS.$("#existing-custom-field-help").toggle(cfMapping);
	}

	// Toggles
    importer.toggle = new JIRA.ToggleBlock({
        blockSelector: ".toggle-wrap",
        triggerSelector: ".mod-header h3",
        cookieCollectionName: "jim-block-states",
        originalTargetIgnoreSelector: "a"
    });

}());
