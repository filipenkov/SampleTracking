AJS.test = {
    results : {
        modules : []
        /*
            Extended with:

            tests : []              - Added in createModule
                assertions : []     - Added in createTest
        */
    }
};

AJS.toInit(function ($) {

    function createModule(name) {
        return {
            name: name,
            tests: []
        };
    }

    function createTest(name) {
        return {
            name: name,
            assertions: []
        };
    }

    function getCurrentModule() {
        AJS.test.results.modules.length || AJS.test.results.modules.push(createModule('Default Starting Module'));
        return AJS.test.results.modules[AJS.test.results.modules.length -1];
    }

    function getCurrentTest() {
        var module = getCurrentModule();
        return module.tests[module.tests.length - 1];
    }

    // Where data = { result, actual, expected, message }
    QUnit.log = function (data) {
        var test = getCurrentTest();
        data = $.extend({}, data);   // don't change the original data object

        // Java is not dynamically typed. To successfully read in actual and expected they must be strings.
        if (typeof data.actual !== 'undefined') {
            data.actualStr = JSON.stringify(data.actual);
        }
        if (typeof data.expected !== 'undefined') {
            data.expectedStr = JSON.stringify(data.expected);
        }

        test.assertions.push(data);
    };

    // When the test starts push its 'model' to the current module's test array.
    QUnit.testStart = function (data) {
        var module = getCurrentModule();
        module.tests.push(createTest(data.name));
    }

    // Where data = { name, failed, passed, total }
    QUnit.testDone = function(data) {
        var test = getCurrentTest();
        $.extend(test, data);
    }

    // When the module starts push its 'model' to the results array.
    QUnit.moduleStart = function (data) {
        AJS.test.results.modules.push(createModule(data.name));
    }

    // Where data = { name, failed, passed, total }
    QUnit.moduleDone = function (data) {
        var module = getCurrentModule();
        $.extend(module, data);
    }

    QUnit.listenersForDoneEvent.push(function (failures, total) {
        AJS.test.results.allDone = true;
    });

    /**
     * Override default reset function (as it reverts the state of container identified by "#main" css selector)
     */
    QUnit.reset = function () {
        // do nothing
    };
});
