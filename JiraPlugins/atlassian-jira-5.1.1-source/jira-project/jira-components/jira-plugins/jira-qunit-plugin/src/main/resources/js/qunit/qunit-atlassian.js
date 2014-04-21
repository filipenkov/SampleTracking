/**
 * Atlassian extensions to core QUnit functionality
 */
AJS.toInit(function ($) {

    $.extend(QUnit, {
        listenersForDoneEvent: [],
        /**
         * Override done method.
         * @param data { failed, passed, total, runtime }
         */
        done: function (data) {
            for (var i = 0, l = this.listenersForDoneEvent.length; i < l; i++) {
                this.listenersForDoneEvent[i](data);
            }
        }
    });

    $("#qunit-filter-pass").change(function () {
        var checked = $(this).is(":checked");
        sessionStorage && sessionStorage.setItem("atlassian.qunit.hide.passed", checked);
        $("#qunit-tests .pass")[checked ? "hide" : "show"]();
    });

    QUnit.listenersForDoneEvent.push(function (failures, total) {
        if (sessionStorage && sessionStorage.getItem("atlassian.qunit.hide.passed") == "true") {
            $("#qunit-tests .pass").hide();
            $("#qunit-filter-pass").attr("checked", "checked");
        }
    });

});
