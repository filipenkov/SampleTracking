AJS.$(function () {

    function initGroupPicker(el) {
        AJS.$(el || document.body).find('div.aui-field-grouppicker').add('tr.aui-field-grouppicker').add('td.aui-field-grouppicker').each(function () {
            var $container = AJS.$(this),
                trigger = $container.find('a.grouppicker-trigger'),
                url = trigger.attr('href');

            function openGroupPickerWindow(e) {
                e.preventDefault();
                window.open(url, 'GroupPicker', 'status=yes,resizable=yes,top=100,left=200,width=580,height=750,scrollbars=yes');
            }

            trigger.click(openGroupPickerWindow);
        });
    }

    // Init the control
    initGroupPicker();

    // Bind the init function so it runs within the dialogs
    AJS.$(document).bind("dialogContentReady", function (e, dialog) {
        initGroupPicker(dialog.get$popupContent());
    });

});