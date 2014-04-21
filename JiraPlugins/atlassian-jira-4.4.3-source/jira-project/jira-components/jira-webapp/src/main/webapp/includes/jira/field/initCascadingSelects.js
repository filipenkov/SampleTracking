AJS.$(function () {

    function initCascadingSelect(el) {
        AJS.$(el || document.body).find('div.aui-field-cascadingselect').add('tr.aui-field-cascadingselect').add('td.aui-field-cascadingselect').each(function () {
            var $container = AJS.$(this),
                parent = $container.find('.cascadingselect-parent'),
                parentOptions = parent.find('option'),
                oldClass = "",
                child = $container.find('.cascadingselect-child'),
                childOptions = child.find('option'),
                selectedChild = child.find(':selected');

            function update() {
                var currentClass = parent.find('option:selected').attr('class');
                // Compare so we're not redrawing the child dropdown when changing between the options with the class "default-option"
                if (currentClass !== oldClass) {
                    // Hide all the options other than ones relating to the selected parent
                    child.find('option').remove();
                    childOptions.filter('.'+parent.find('option:selected').attr('class')).appendTo(child);
                    // Select the option which is to be selected on page load - default to the first one if none specified.
                    if (selectedChild.hasClass(parent.find('option:selected').attr('class'))) {
                        child.val(selectedChild.val());
                    } else {
                        child.val(child.find('option:first').val());
                    }
                    oldClass = currentClass;
                }
            }
            parent.bind('cascadingSelectChanged', update)
                  .change(function(){
                        AJS.$(this).trigger('cascadingSelectChanged');
                  })
                  .trigger('cascadingSelectChanged');
        });
    }

    // Init the control
    initCascadingSelect();

    // Bind the init function so it runs within the dialogs
    AJS.$(document).bind("dialogContentReady", function (e, dialog) {
        initCascadingSelect(dialog.get$popupContent());
    });

});