(function() {
    function createPicker($fieldset) {
        var opts = JIRA.parseOptionsFromFieldset($fieldset),
            $select = AJS.$('#' + opts.id),
            issueId = opts.issueId,
            data = {};

        if (/customfield_\d/.test(opts.id)) {
            data.customFieldId = parseInt(opts.id.replace('customfield_', ''), 10);
        }

        new JIRA.LabelPicker({
            element: $select,
            ajaxOptions: {
                url: contextPath + '/rest/api/1.0/labels' + (issueId ? '/' + issueId : '') + '/suggest',
                data: data
            }
        });
    }

    function locateFieldset(parent) {
        var $parent = AJS.$(parent),
            $fieldset;

        if ($parent.is(FIELDSET_SELECTOR)) {
            $fieldset = $parent;
        } else {
            $fieldset = $parent.find(FIELDSET_SELECTOR);
        }

        return $fieldset;
    }

    var DEFAULT_SELECTORS = [
        "div.aui-field-labelpicker", // aui forms
        "td.aui-field-labelpicker", // convert to subtask and move
        "tr.aui-field-labelpicker" // bulk edit
    ];

    var FIELDSET_SELECTOR = "fieldset.labelpicker-params";

    function findLabelsFieldsetAndConvertToPicker(context, selector) {
        selector = selector || DEFAULT_SELECTORS.join(", ");

        AJS.$(selector, context).each(function () {
            var $fieldset = locateFieldset(this);

            if ($fieldset.length > 0) {
                createPicker($fieldset);
            }
        });
    }

    AJS.$(function() {
        findLabelsFieldsetAndConvertToPicker();
    });

    AJS.$(document).bind("dialogContentReady", function(e, dialog) {
        findLabelsFieldsetAndConvertToPicker(dialog.get$popupContent());
    });
})();
