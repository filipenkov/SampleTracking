AJS.$(function () {

    function initDatePicker(el) {
        AJS.$(el || document.body).find('div.aui-field-datepicker').add('tr.aui-field-datepicker').add('td.aui-field-datepicker').each(function () {
            var $container = AJS.$(this),
                field = $container.find('input:text'),
                defaultCheckbox = $container.find('#useCurrentDate'),
                params = JIRA.parseOptionsFromFieldset($container.find('fieldset.datepicker-params'));

            params.context = el;

            Calendar.setup(params);

            function toggleField() {
                field.attr('disabled',defaultCheckbox.is(':checked'));
            }

            if (defaultCheckbox.length) {
                toggleField();
                defaultCheckbox.click(toggleField);
            }
        });
    }

    // Init the control
    initDatePicker();

    // Bind the init function so it runs within the dialogs
    AJS.$(document).bind("dialogContentReady", function (e, dialog) {
        initDatePicker(dialog.get$popupContent());
    });
});
