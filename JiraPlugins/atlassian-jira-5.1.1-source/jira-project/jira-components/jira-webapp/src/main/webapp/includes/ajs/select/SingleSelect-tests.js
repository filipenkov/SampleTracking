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
});

test("JRA-26756: Customware issue", function () {

    var html = "<select id=\"tempo-report-search-picker\" class=\"tempo-single-search-picker\" name=\"search_filter\" data-container-class=\"medium\" data-input-text=\"\">\n"
            + "    <optgroup id=\"tempo-report-search-suggested\"\n"
            + "              data-weight=\"0\">\n"
            + "        <option value=\"10320\"\n"
            + "                selected=\"selected\"\n"
            + "                data-field-text=\"Hermione&#39;s Ž private filter\"\n"
            + "                data-field-label=\"Hermione&#39;s Ž private filter\">Hermione&#39;s Ž private filter\n"
            + "        </option>\n"
            + "    </optgroup>\n"
            + "</select>";

    var oldAjax = jQuery.ajax;

    jQuery.ajax = function (options) {
        options.success({}, "success", {status: 200});
        options.complete({status: 200}, "success", {status: 200});
    };

    var invalidDescriptor = new AJS.ItemDescriptor({
        value: "-2" , // value of item added to select
        label: "Invalid Worklogs", // title of lozenge
        allowDuplicate: false
    });

    var $select = jQuery(html).appendTo("body"),
        mySelect = new AJS.SingleSelect({
            element: $select,
            showDropdownButton: true,
            removeOnUnSelect: true,
            submitInputVal: true,
            noQueryNoRequest: false,
            width: 200,
            ajaxOptions: {
                query: true,
                url: "blah",
                formatResponse: function () {
                    var optgroup1 = new AJS.GroupDescriptor({
                        weight: 0, // order or groups in suggestions dropdown
                        id: "tempo-report-project-search-0",
                        replace: true,
                        label: "Standard"
                    });

                    optgroup1.addItem(invalidDescriptor);

                    var optgroup2 = new AJS.GroupDescriptor({
                        weight: 1, // order or groups in suggestions dropdown
                        id: "tempo-report-project-search-1",
                        replace: true,
                        label: "Filters"
                    });

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10021",
                        label: "%3a%2f%3f%23%5b%5d%40%21%24%26%27%28%29%2a%2b%2c%3b%3d",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10012",
                        label: "&",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10020",
                        label: "<SCRIPT SRC=http://ha.ckers.org/xss.js></SCRIPT>",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10022",
                        label: "\x3cscript src=http://www.example.com/malicious-code.js\x3e\x3c/script\x3e",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10016",
                        label: "Bugs",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10321",
                        label: "bulk rapid",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10320",
                        label: "Hermione's Ž private filter",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10121",
                        label: "Hermione_filter",
                        allowDuplicate: false
                    }));

                    return [optgroup1, optgroup2];
                }
            }
        });

    mySelect.$field.focus();
    mySelect.$dropDownIcon.click();
    mySelect.setSelection(invalidDescriptor); // Will blow up here if issue still exists
    equals(invalidDescriptor, mySelect.getSelectedDescriptor());

    jQuery.ajax = oldAjax;
});


