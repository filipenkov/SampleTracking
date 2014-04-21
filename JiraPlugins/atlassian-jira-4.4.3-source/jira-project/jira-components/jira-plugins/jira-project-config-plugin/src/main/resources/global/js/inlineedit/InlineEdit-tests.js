AJS.test.require("com.atlassian.jira.jira-project-config-plugin:project-config-global");

module("fields");

test("Text: showLoading", function () {

    var textControl = new JIRA.InlineEdit.Text(),
        input = textControl.$field;

    input.appendTo("body");

    textControl.showLoading();

    equals(textControl.$throbber.prev()[0], input[0],
            "Expected throbber to be appended after input");
});

test("Text: hideLoading", function () {

    var textControl = new JIRA.InlineEdit.Text(),
        throbber = textControl.$throbber.appendTo("body");

    textControl.hideLoading();

    equals(throbber.is(":visible"), false,
            "Expected throbber Not to be visible")

});

test("Text: activate", function () {

    var textControl = new JIRA.InlineEdit.Text(),
        eventsAssigned,
        field = textControl.$field.appendTo("body"),
        refElement = jQuery("<strong />").css({
            fontSize: "26px",
            fontFamily: "Arial"
        })
        .appendTo("body");

    textControl._assignEvents = function () {
        eventsAssigned = true;
    };

    textControl.activate("Some text", refElement);

    ok(eventsAssigned, "Expected events to be assigned");

    equals("Some text", field.val(),
            "Expected field value to be set");

    equals(field[0], document.activeElement,
            "Expected field to be focused");

    equals(field.height(), 26,
            "Expected field height to be same as reference element font size");

    equals(field.css("fontSize"), refElement.css("fontSize"),
            "Expected font size to be same as reference element");

    equals(field.css("fontFamily"), refElement.css("fontFamily"),
            "Expected font family to be same as reference element");

});

test("Text: deactivate", function () {

    var eventsUnassigned,
        deactivateEventFired,
        textControl = new JIRA.InlineEdit.Text();

    textControl._unassignEvents = function () {
        eventsUnassigned = true;
    };

    jQuery(textControl).bind("deactivate", function () {
        deactivateEventFired = true;
    });

    textControl.deactivate();

    ok(eventsUnassigned, "Expected events to be unassigned");
    ok(deactivateEventFired, "Expected [deactivate] event to be fired");

});

test("Text: getMeasure", function () {

    var textControl = new JIRA.InlineEdit.Text(),
        $field = textControl.$field,
        $measure = textControl.getMeasure();

    $field.css({
        fontSize: "26px",
        fontFamily: "Arial"
    });

    notEqual($measure[0], textControl.getMeasure(),
            "There should only be one measure element");

    equals($field.css("fontSize"), $measure.css("fontSize"),
            "Expected font size to be same as reference element");

    equals($field.css("fontFamily"), $measure.css("fontFamily"),
            "Expected font family to be same as reference element");

});


test("Text: getWidth", function () {

    var newWidth,
        width,
        textControl = new JIRA.InlineEdit.Text();

    textControl.$field.appendTo("body");

    textControl.$field.val("test");

    width = textControl.getWidth();

    textControl.$field.val("really really really really long");

    newWidth = textControl.getWidth();

    ok(newWidth > width,
            "Expected width to reflect size of input");

    width = newWidth;

    textControl.$field.val("really short");

    newWidth = textControl.getWidth();

    ok(newWidth < width,
            "Expected width to reflect size of input");
});

test("Text: getLabel, getValue", function () {

    var textControl = new JIRA.InlineEdit.Text();
        textControl.$field.val("test");

    equals("test", textControl.getLabel());
    equals("test", textControl.getValue());
});

test("Text: valueHasChanged", function () {

    var textControl = new JIRA.InlineEdit.Text();

    textControl.activate("test", jQuery("<div />"));

    ok(!textControl.valueHasChanged(), "Expected no change");

    textControl.$field.val("another test")

    ok(textControl.valueHasChanged(), "Expected change");
});


test("Text: input", function () {

    var deactivateCalled,
        errorHideCalled,
        blurCalled,
        event = new jQuery.Event("keydown"),
        textControl = new JIRA.InlineEdit.Text();

    textControl.$field.appendTo("body");


    textControl.$field.bind("blur", function () {
        blurCalled = true;
    });

    textControl.keys["Return"].call(textControl, event);

    ok(blurCalled, "Expected blur event to be called when pressing return");

    textControl.error = {
        hide: function () {
            errorHideCalled = true;
        }
    }

    textControl.deactivate = function () {
        deactivateCalled = true;
    }

    textControl.keys["Esc"].call(textControl, event);

    ok(errorHideCalled, "Expected error to be hidden when Esc pressed");
    ok(deactivateCalled, "Expected deactivate method to be called when Esc pressed");

})


test("SingleSelect: getSingleSelect", function () {

    var inlineField = new JIRA.InlineEdit.SingleSelect({
        element: jQuery("<select />")
    });

    equals(inlineField.getSingleSelect(), inlineField.getSingleSelect(),
            "Expected single select to be lazy reference");


});

test("SingleSelect: setNullValue", function () {

    var element = jQuery("<select><option value='blah'>sfdg</option><option value='-1'>none</option></select>"),
        inlineField = new JIRA.InlineEdit.SingleSelect({
            element: element
        });


    inlineField.handleConfirm = function () {};
    inlineField.setNullValue();
    equals(element.val(), -1, "Expected null value to be set");
});