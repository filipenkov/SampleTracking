/**
 *
 * PLEASE READ!!!!!!
 *
 * These are not 'Standard Unit Tests'. They apply black box testing at a method level (http://en.wikipedia.org/wiki/Black-box_testing).
 *
 * Basically we are testing calls to the methods of this class, and asserting their output is correct. This is different
 * to conventional unit testing as we are NOT mocking out all the methods of this class.
 */


AJS.test.require("jira.webresources:select-pickers");


function getSelect () {
    return jQuery("<select><option value='1'>One</option><option selected='selected' value='2'>Two</option></select>")
            .appendTo("body");
}

test("constructor", function () {


    var initializedFired = false,
        $select = getSelect(),
        mySelect;

    $select.bind("initialized", function () {
        initializedFired = true;
    });

    mySelect = new AJS.SingleSelect({
        element: $select,
        width: 200
    });

    // events
    ok(initializedFired, "Expected initialized event to fire");

    // dom
    ok(!$select.is(":visible"), "Expected <select> to be hidden");
    ok(!mySelect.$errorMessage.is(":visible"), "Expected error message to be hidden");
    ok(mySelect.$dropDownIcon.hasClass("drop-menu"), "Expected dropdown menu");
    equals(mySelect.$container.width(), 200, "expected container width to be 200 as specified");
    equals(mySelect.$field.val(), "Two");

});


test("getSelectedDescriptor", function () {

    var $select = getSelect(),
        mySelect = new AJS.SingleSelect({
        element: $select,
        width: 200
    });

    equals(mySelect.getSelectedDescriptor().value(), "2", "Expected selected descriptor to have a value of 2");
});


test("getDisplayVal", function () {

    var $select = getSelect(),
        mySelect = new AJS.SingleSelect({
            element: $select,
            itemAttrDisplayed: "value",
            width: 200
        }),
        descriptor = new AJS.ItemDescriptor({
            value: "Scott",
            label: "Scott's label"
        });


    equals(mySelect.getDisplayVal(descriptor), "Scott", "Expected the value to be display value");
});


test("getQueryVal", function () {

     var $select = getSelect(),
        mySelect = new AJS.SingleSelect({
            element: $select,
            itemAttrDisplayed: "value",
            width: 200
        });

    mySelect.$field.val("test");

    equals(mySelect.getQueryVal(), "", "Expected query value to be '' when not in editing mode"); // not in editing mode

    mySelect._setEditingMode();

    equals(mySelect.getQueryVal(), "test", "Expected query value to be 'test' when in editing mode");
});


test("setSelection", function () {

    var selectedEventFired = false,
        $select = getSelect(),
        mySelect = new AJS.SingleSelect({
            element: $select,
            itemAttrDisplayed: "value",
            width: 200
        }),
        descriptor = $select.find(":selected").data("descriptor");

    equals($select.find(":selected").data("descriptor"), descriptor);

    descriptor = $select.find("option:first").data("descriptor");

    $select.bind("selected", function () {
        selectedEventFired = true;
    });

    mySelect.setSelection(descriptor);

    ok(!mySelect.$container.hasClass("aui-ss-editing"), "expected field to be in read mode (not editing)");

    ok(selectedEventFired, 'Expected [select] event to fire');
    equals($select.find(":selected").data("descriptor"), descriptor, "Selected descriptor did not change");
});


test("clearSelection", function () {

    var unselectEventFired = false,
        $select = getSelect(),
        mySelect = new AJS.SingleSelect({
            element: $select,
            itemAttrDisplayed: "value",
            width: 200
        });


    $select.bind("unselect", function () {
        unselectEventFired  = true;
    });


    mySelect.clearSelection();

    equals($select.find(":selected").length, 0, "Expected no selected <option>s");

    ok(unselectEventFired, "Expected unselect event to fire");
});


test("_handleEdit", function () {

    var $select = getSelect(),
        mySelect = new AJS.SingleSelect({
        element: $select,
        itemAttrDisplayed: "value",
        width: 200
    });

    mySelect.onEdit({});
    mySelect.$field.trigger("keyup");

    equals($select.find(":selected").length, 0, "Expected any editing to unselect selected");
});

test("handleFreeInput", function () {

    var $select = jQuery("<select><option value='fred'>Fred</option><option value='fred2'>Fred</option></select>")
            .appendTo("body"),
        mySelect = new AJS.SingleSelect({
        element: $select,
        itemAttrDisplayed: "value",
        width: 200
    });


    mySelect.$container.addClass("aui-ss-editing");
    mySelect.$field.val("Fred");

    mySelect.handleFreeInput();


    equals($select.val(), "fred");


    $select = jQuery("<select><option value='fred'>Fred</option><option value='fred2' selected='selected'>Fred</option></select>")
            .appendTo("body");

    mySelect = new AJS.SingleSelect({
        element: $select,
        itemAttrDisplayed: "value",
        width: 200
    });

    mySelect.handleFreeInput();

    equals($select.val(), "fred2");
})
